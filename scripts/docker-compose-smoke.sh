#!/usr/bin/env bash
# Docker Compose smoke test for MedPortal upgrade verification.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f Dockerfile ]]; then
  echo "FAIL: Dockerfile is required and missing"
  exit 1
fi
if [[ ! -f docker-compose.yml ]]; then
  echo "FAIL: docker-compose.yml is required and missing"
  exit 1
fi

export JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET="${JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET:-bXktc2VjcmV0LWtleS13aGljaC1zaG91bGQtYmUtYXQtbGVhc3QtNjQtY2hhcmFjdGVycy1sb25nLWZvci1ocy01MTI=}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-MEDIATION}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-MEDIATION}"
export ORACLE_PASSWORD="${ORACLE_PASSWORD:-MedPortal_Oracle_ChangeMe}"
# Smoke-only: include test Liquibase context for liquibaseit account; disable captcha for non-interactive login.
export SPRING_LIQUIBASE_CONTEXTS="${SPRING_LIQUIBASE_CONTEXTS:-prod,test}"
export CAPTCHA_ENABLED="${CAPTCHA_ENABLED:-false}"

cleanup() {
  docker compose down -v --remove-orphans || true
}
trap cleanup EXIT

echo "==> docker compose config"
docker compose config >/tmp/medportal-compose.validated.yml

echo "==> docker compose build"
docker compose build

echo "==> docker compose up -d"
docker compose up -d

echo "==> wait for healthy services"
deadline=$((SECONDS + 900))
while (( SECONDS < deadline )); do
  app_id="$(docker compose ps -q medportal 2>/dev/null || true)"
  if [[ -z "$app_id" ]]; then
    echo "medportal container not created yet"
    sleep 10
    continue
  fi
  app_health="$(docker inspect --format='{{.State.Health.Status}}' "$app_id" 2>/dev/null || echo starting)"
  echo "medportal health=$app_health"
  if [[ "$app_health" == "healthy" ]]; then
    break
  fi
  if [[ "$app_health" == "unhealthy" ]]; then
    docker compose logs --no-color medportal | tail -200
    echo "FAIL: medportal became unhealthy"
    exit 1
  fi
  sleep 10
done

app_id="$(docker compose ps -q medportal)"
app_health="$(docker inspect --format='{{.State.Health.Status}}' "$app_id" 2>/dev/null || echo missing)"
if [[ "$app_health" != "healthy" ]]; then
  docker compose logs --no-color | tail -300
  echo "FAIL: timed out waiting for healthy medportal (status=$app_health)"
  exit 1
fi

echo "==> health endpoint"
code="$(curl -sS -o /tmp/medportal-health.txt -w "%{http_code}" http://127.0.0.1:8080/management/health || true)"
echo "health HTTP $code"
if [[ "$code" != "200" && "$code" != "401" && "$code" != "503" ]]; then
  echo "FAIL: unexpected health status $code"
  cat /tmp/medportal-health.txt || true
  exit 1
fi

echo "==> home / index"
home_code="$(curl -sS -o /tmp/medportal-home.html -w "%{http_code}" http://127.0.0.1:8080/ || true)"
index_code="$(curl -sS -o /tmp/medportal-index.html -w "%{http_code}" http://127.0.0.1:8080/index.html || true)"
echo "home HTTP $home_code index HTTP $index_code"
if [[ "$home_code" != "200" && "$index_code" != "200" ]]; then
  echo "FAIL: home/index not reachable (home=$home_code index=$index_code)"
  docker compose logs --no-color medportal | tail -100
  exit 1
fi

echo "==> protected API without token must be 401"
unauth_code="$(curl -sS -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/api/account || true)"
if [[ "$unauth_code" != "401" ]]; then
  echo "FAIL: expected 401 for /api/account without token, got $unauth_code"
  exit 1
fi

echo "==> login + JWT protected API"
login_body='{"username":"liquibaseit","password":"user","rememberMe":false,"captchaId":"smoke","captchaToken":"smoke"}'
login_code="$(curl -sS -o /tmp/medportal-login.json -w "%{http_code}" \
  -H 'Content-Type: application/json' \
  -d "$login_body" \
  http://127.0.0.1:8080/api/authenticate || true)"
echo "login HTTP $login_code"
if [[ "$login_code" != "200" ]]; then
  cat /tmp/medportal-login.json || true
  docker compose logs --no-color medportal | tail -100
  echo "FAIL: login failed"
  exit 1
fi

token="$(python3 -c 'import json; print(json.load(open("/tmp/medportal-login.json"))["id_token"])')"
acct_code="$(curl -sS -o /tmp/medportal-account.json -w "%{http_code}" \
  -H "Authorization: Bearer $token" \
  http://127.0.0.1:8080/api/account || true)"
echo "account HTTP $acct_code"
if [[ "$acct_code" != "200" ]]; then
  cat /tmp/medportal-account.json || true
  echo "FAIL: protected API with JWT failed"
  exit 1
fi

echo "==> startup exception scan"
if docker compose logs medportal | grep -E "APPLICATION FAILED TO START|Error creating bean|LiquibaseException" | head -20 | grep .; then
  echo "FAIL: startup exceptions detected in logs"
  exit 1
fi

echo "Docker Compose smoke test PASSED"
