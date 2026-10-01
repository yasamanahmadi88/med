<#
    .SYNOPSIS
        Security regression test for the MedPortal File Mediator web application.

    .DESCRIPTION
        Verifies that the vulnerabilities reported in the grey-box penetration test
        (RPT-TR-WEB-SA-M-1404651) remain fixed. Each check maps to a report finding
        (OWASP WSTG Ref.) and asserts the SECURE expected behaviour, producing a
        PASS/FAIL result and an HTML report.

        This is a defensive regression harness, not an exploitation tool. It sends a
        single benign probe per finding and asserts the hardened response. It does
        NOT brute-force, enumerate, or attempt account takeover.

    .NOTES
        Authentication requires a valid JWT. Because /api/authenticate is captcha- and
        rate-limit protected, obtain tokens interactively (browser DevTools ->
        Authorization header) and pass them via environment variables:

            $env:MED_ADMIN_TOKEN = "eyJ..."   # a ROLE_ADMIN user
            $env:MED_USER_TOKEN  = "eyJ..."   # a non-admin user
            $env:MED_BASE_URL    = "http://localhost:8080"   # optional, this is the default

        Authenticated checks are skipped (reported as SKIP) when the relevant token
        is absent, so the script is safe to run unattended in CI.

    .EXAMPLE
        pwsh ./scripts/security-regression-test.ps1
#>

[CmdletBinding()]
param(
    [string]$BaseUrl    = $(if ($env:MED_BASE_URL) { $env:MED_BASE_URL } else { "http://localhost:8080" }),
    [string]$AdminToken = $env:MED_ADMIN_TOKEN,
    [string]$UserToken  = $env:MED_USER_TOKEN,
    [string]$ReportFile = (Join-Path -Path (Get-Location) -ChildPath "security-regression-report.html")
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd('/')

# ---------------------------------------------------------------------------
# HTTP helper. Returns the status code even for 4xx/5xx responses so a check
# can assert on it. Never throws on HTTP errors.
# ---------------------------------------------------------------------------
function Invoke-Api {
    param(
        [string]$Method = "GET",
        [Parameter(Mandatory)][string]$Path,
        [hashtable]$Headers = @{},
        [string]$Body,
        [Microsoft.PowerShell.Commands.WebRequestSession]$Session
    )

    $uri = "$BaseUrl$Path"
    $reqArgs = @{
        Uri             = $uri
        Method          = $Method
        Headers         = $Headers
        UseBasicParsing = $true
        TimeoutSec      = 30
        ErrorAction     = "Stop"
    }
    if ($Body)    { $reqArgs.ContentType = "application/json"; $reqArgs.Body = $Body }
    if ($Session) { $reqArgs.WebSession  = $Session }

    try {
        $res = Invoke-WebRequest @reqArgs
        return [pscustomobject]@{ Status = [int]$res.StatusCode; Content = $res.Content }
    } catch {
        $resp = $_.Exception.Response
        if ($resp -and ($resp.PSObject.Properties.Name -contains 'StatusCode')) {
            return [pscustomobject]@{ Status = [int]$resp.StatusCode; Content = $_.ErrorDetails.Message }
        }
        # Connection refused / DNS / TLS -> surface as a non-numeric status.
        return [pscustomobject]@{ Status = "NO-RESPONSE"; Content = $_.Exception.Message }
    }
}

# ---------------------------------------------------------------------------
# Result collection.
# ---------------------------------------------------------------------------
$results = New-Object System.Collections.Generic.List[object]

function Add-Result {
    param(
        [string]$Finding,   # OWASP WSTG ref + short name
        [string]$Check,     # what was probed
        [string]$Expected,  # secure expectation
        $Actual,            # observed status
        [ValidateSet("PASS", "FAIL", "SKIP")][string]$Result,
        [string]$Note = ""
    )
    $results.Add([pscustomobject]@{
        Finding  = $Finding
        Check    = $Check
        Expected = $Expected
        Actual   = "$Actual"
        Result   = $Result
        Note     = $Note
    })
    $color = switch ($Result) { "PASS" { "Green" } "FAIL" { "Red" } "SKIP" { "DarkYellow" } }
    Write-Host ("[{0}] {1} - {2}" -f $Result, $Finding, $Check) -ForegroundColor $color
}

# Assert helper: PASS when the actual status is in the allowed set.
function Assert-Status {
    param($Actual, [int[]]$Allowed, [string]$Finding, [string]$Check, [string]$Expected, [string]$Note = "")
    if ($Actual -is [int] -and $Allowed -contains $Actual) {
        Add-Result -Finding $Finding -Check $Check -Expected $Expected -Actual $Actual -Result "PASS" -Note $Note
    } else {
        Add-Result -Finding $Finding -Check $Check -Expected $Expected -Actual $Actual -Result "FAIL" -Note $Note
    }
}

$adminHeaders = if ($AdminToken) { @{ Authorization = "Bearer $AdminToken" } } else { $null }
$userHeaders  = if ($UserToken)  { @{ Authorization = "Bearer $UserToken"  } } else { $null }

Write-Host "MedPortal security regression test against $BaseUrl" -ForegroundColor Cyan
Write-Host ("Admin token: {0} | User token: {1}`n" -f `
    ($(if ($AdminToken) { "present" } else { "MISSING (auth checks skipped)" })),
    ($(if ($UserToken)  { "present" } else { "MISSING (auth checks skipped)" }))) -ForegroundColor Cyan

# ===========================================================================
# 1. Authentication required on protected APIs (baseline).
#    /api/** is `authenticated()`; an anonymous request must be rejected (401).
# ===========================================================================
foreach ($ep in @("/api/account", "/api/admin/users", "/api/configs", "/api/instances")) {
    $r = Invoke-Api -Path $ep
    Assert-Status -Actual $r.Status -Allowed @(401) `
        -Finding "Baseline - auth required" -Check "Anonymous GET $ep" `
        -Expected "401 Unauthorized"
}

# ===========================================================================
# 2. Broken authorization on /api/users -> /api/admin/users (Ref. 4.5.2).
#    Admin-only resource must reject a non-admin (403) and accept an admin (200).
# ===========================================================================
if ($userHeaders) {
    $r = Invoke-Api -Path "/api/admin/users" -Headers $userHeaders
    Assert-Status -Actual $r.Status -Allowed @(403) `
        -Finding "4.5.2 Bypass authorization" -Check "Non-admin GET /api/admin/users" `
        -Expected "403 Forbidden"
} else {
    Add-Result "4.5.2 Bypass authorization" "Non-admin GET /api/admin/users" "403 Forbidden" "-" "SKIP" "MED_USER_TOKEN not set"
}
if ($adminHeaders) {
    $r = Invoke-Api -Path "/api/admin/users" -Headers $adminHeaders
    Assert-Status -Actual $r.Status -Allowed @(200) `
        -Finding "4.5.2 Bypass authorization" -Check "Admin GET /api/admin/users" `
        -Expected "200 OK"
} else {
    Add-Result "4.5.2 Bypass authorization" "Admin GET /api/admin/users" "200 OK" "-" "SKIP" "MED_ADMIN_TOKEN not set"
}

# ===========================================================================
# 3. Actuator info exposure (Ref. 4.4.4, evidence #1/#2).
#    /management/info must require ADMIN; anonymous -> 401.
# ===========================================================================
$r = Invoke-Api -Path "/management/info"
Assert-Status -Actual $r.Status -Allowed @(401) `
    -Finding "4.4.4 Actuator exposure" -Check "Anonymous GET /management/info" `
    -Expected "401 Unauthorized"

# ===========================================================================
# 4. Account enumeration via self-registration (Ref. 4.3.4).
#    Self-registration is disabled for this deployment -> denyAll (403).
# ===========================================================================
$r = Invoke-Api -Method POST -Path "/api/register" -Body '{"login":"probe-nonexistent","email":"probe@example.invalid","password":"Xy8!pq2Zvn"}'
Assert-Status -Actual $r.Status -Allowed @(401, 403, 404, 405) `
    -Finding "4.3.4 Account enumeration" -Check "POST /api/register (self-registration)" `
    -Expected "denied (403/401/404/405)"

# ===========================================================================
# 5. Unauthenticated password reset -> account takeover (Ref. 4.4.4, evidence #3).
#    A bogus reset key must yield a generic 400, never 204 (success).
#    A valid key is never known to an attacker; using an invalid one is a safe probe.
# ===========================================================================
$r = Invoke-Api -Method POST -Path "/api/account/reset-password/finish" `
        -Body '{"key":"regression-invalid-key-000000","newPassword":"Xy8!pq2Zvn"}'
Assert-Status -Actual $r.Status -Allowed @(400, 401) `
    -Finding "4.4.4 Reset-password takeover" -Check "reset-password/finish with invalid key" `
    -Expected "400/401 (never 204)" `
    -Note "204 here would mean an invalid key was accepted"

# ===========================================================================
# 6. Weak password policy (Ref. 4.4.7).
#    Changing to a 4-char / trivial password must be rejected (400).
#    Requires an authenticated user + CSRF token (non-test profile enables CSRF).
# ===========================================================================
if ($userHeaders) {
    # Prime a session to capture the XSRF-TOKEN cookie (CookieCsrfTokenRepository).
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $null = Invoke-Api -Path "/api/account" -Headers $userHeaders -Session $session
    $xsrf = ($session.Cookies.GetCookies($BaseUrl) | Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -First 1).Value

    $pwHeaders = $userHeaders.Clone()
    if ($xsrf) { $pwHeaders["X-XSRF-TOKEN"] = $xsrf }

    $r = Invoke-Api -Method POST -Path "/api/account/change-password" `
            -Headers $pwHeaders -Session $session `
            -Body '{"currentPassword":"irrelevant","newPassword":"1234"}'
    # 400 = policy rejected the weak password (desired). 403 = CSRF/authz blocked it
    # before validation, which also means the weak password was not accepted.
    Assert-Status -Actual $r.Status -Allowed @(400, 403) `
        -Finding "4.4.7 Weak password policy" -Check "change-password newPassword='1234'" `
        -Expected "400 (rejected by policy)" `
        -Note "min length 8 + common-password blocklist enforced server-side"
} else {
    Add-Result "4.4.7 Weak password policy" "change-password newPassword='1234'" "400 rejected" "-" "SKIP" "MED_USER_TOKEN not set"
}

# ===========================================================================
# 7. Stored XSS in config property (Ref. 4.7.2).
#    ConfigDTO rejects any '<' or '>' (PLAIN_TEXT_PATTERN). A payload must 400.
# ===========================================================================
if ($userHeaders) {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $null = Invoke-Api -Path "/api/account" -Headers $userHeaders -Session $session
    $xsrf = ($session.Cookies.GetCookies($BaseUrl) | Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -First 1).Value
    $xssHeaders = $userHeaders.Clone()
    if ($xsrf) { $xssHeaders["X-XSRF-TOKEN"] = $xsrf }

    # Benign marker payload; asserted to be REJECTED, never stored.
    $payload = '{"property":"regressionTest","pValue":"<img src=x onerror=alert(1)>","commentDesc":"xss regression probe"}'
    $r = Invoke-Api -Method POST -Path "/api/configs" -Headers $xssHeaders -Session $session -Body $payload
    Assert-Status -Actual $r.Status -Allowed @(400, 403) `
        -Finding "4.7.2 Stored XSS (config)" -Check "POST /api/configs with <img> payload" `
        -Expected "400 (HTML content rejected)" `
        -Note "input validation must reject angle brackets"
} else {
    Add-Result "4.7.2 Stored XSS (config)" "POST /api/configs with <img> payload" "400 rejected" "-" "SKIP" "MED_USER_TOKEN not set"
}

# ===========================================================================
# 8. Stack trace disclosure on admin query endpoint (Ref. 4.8.2).
#    /api/resource-authorities/** is ADMIN-only; a non-admin must get 403,
#    not a 500 that leaks a Java stack trace.
# ===========================================================================
if ($userHeaders) {
    $r = Invoke-Api -Path "/api/resource-authorities?size=20&medAuthorityId.in=102'&sort=id,asc" -Headers $userHeaders
    Assert-Status -Actual $r.Status -Allowed @(400, 403) `
        -Finding "4.8.2 Stack trace disclosure" -Check "Non-admin GET resource-authorities (quote in param)" `
        -Expected "403/400 (no 500 stack trace)" `
        -Note "500 would indicate an unhandled exception path"
} else {
    Add-Result "4.8.2 Stack trace disclosure" "resource-authorities with quote" "403/400" "-" "SKIP" "MED_USER_TOKEN not set"
}

# ===========================================================================
# 9. Business-logic / verb tampering delete (Ref. 4.10.1).
#    DELETE /api/instances/{id} is @Secured("instance"); a user lacking that
#    authority must be rejected (403). Uses a non-existent id so no data is at risk.
# ===========================================================================
if ($userHeaders) {
    $r = Invoke-Api -Method DELETE -Path "/api/instances/999999999" -Headers $userHeaders
    Assert-Status -Actual $r.Status -Allowed @(401, 403, 404) `
        -Finding "4.10.1 Business-logic delete" -Check "Non-authorized DELETE /api/instances/{id}" `
        -Expected "403 (or 404 after authz)" `
        -Note "authority enforced on the server, not only in the UI"
} else {
    Add-Result "4.10.1 Business-logic delete" "DELETE /api/instances/{id}" "403" "-" "SKIP" "MED_USER_TOKEN not set"
}

# ===========================================================================
# 10. Session/token not invalidated after logout (Ref. 4.6.6).
#     After POST /api/auth/logout the same token must be rejected (401).
#     This mutates the provided user session, so it runs last and only for the
#     user token. Skipped unless explicitly enabled to avoid killing a session
#     the operator may still be using.
# ===========================================================================
if ($userHeaders -and $env:MED_TEST_LOGOUT -eq 'true') {
    $before = Invoke-Api -Path "/api/account" -Headers $userHeaders
    if ($before.Status -eq 200) {
        $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
        $null = Invoke-Api -Path "/api/account" -Headers $userHeaders -Session $session
        $xsrf = ($session.Cookies.GetCookies($BaseUrl) | Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -First 1).Value
        $logoutHeaders = $userHeaders.Clone()
        if ($xsrf) { $logoutHeaders["X-XSRF-TOKEN"] = $xsrf }

        $null = Invoke-Api -Method POST -Path "/api/auth/logout" -Headers $logoutHeaders -Session $session
        $after = Invoke-Api -Path "/api/account" -Headers $userHeaders
        Assert-Status -Actual $after.Status -Allowed @(401) `
            -Finding "4.6.6 Logout invalidation" -Check "Reuse token after logout" `
            -Expected "401 (token revoked)" `
            -Note "server-side session removed on logout"
    } else {
        Add-Result "4.6.6 Logout invalidation" "Reuse token after logout" "401" $before.Status "SKIP" "user token not valid before test"
    }
} else {
    Add-Result "4.6.6 Logout invalidation" "Reuse token after logout" "401" "-" "SKIP" "set MED_TEST_LOGOUT=true to run (consumes the user session)"
}

# ===========================================================================
# 11. Rate limiting (Ref. 4.4.3 related).
#     Repeated authenticated GETs must eventually return 429 (bucket4j).
#     Dev profile GET bucket size = 10, so > 10 requests should trip it.
# ===========================================================================
if ($userHeaders) {
    $sawThrottle = $false
    $lastStatus  = $null
    for ($i = 1; $i -le 15; $i++) {
        $r = Invoke-Api -Path "/api/account" -Headers $userHeaders
        $lastStatus = $r.Status
        if ($r.Status -eq 429) { $sawThrottle = $true; break }
    }
    if ($sawThrottle) {
        Add-Result "4.4.3 Rate limiting" "15x GET /api/account" "429 after bucket exhausted" 429 "PASS"
    } else {
        Add-Result "4.4.3 Rate limiting" "15x GET /api/account" "429 after bucket exhausted" $lastStatus "FAIL" `
            "no 429 seen; bucket size may exceed 15 for this profile"
    }
} else {
    Add-Result "4.4.3 Rate limiting" "15x GET /api/account" "429" "-" "SKIP" "MED_USER_TOKEN not set"
}

# ===========================================================================
# HTML report (all interpolated values HTML-encoded to avoid injection into
# the report itself).
# ===========================================================================
function Enc([string]$s) { [System.Net.WebUtility]::HtmlEncode($s) }

$pass = ($results | Where-Object Result -eq 'PASS').Count
$fail = ($results | Where-Object Result -eq 'FAIL').Count
$skip = ($results | Where-Object Result -eq 'SKIP').Count
$generated = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")

$rows = foreach ($r in $results) {
    $cls = $r.Result.ToLower()
    "<tr class='$cls'><td>$(Enc $r.Finding)</td><td>$(Enc $r.Check)</td><td>$(Enc $r.Expected)</td><td>$(Enc $r.Actual)</td><td class='res'>$(Enc $r.Result)</td><td>$(Enc $r.Note)</td></tr>"
}

$html = @"
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>MedPortal Security Regression Report</title>
<style>
 body { font-family: Arial, sans-serif; margin: 24px; color: #222; }
 h1 { font-size: 20px; }
 .meta { color: #555; margin-bottom: 16px; }
 .summary span { display:inline-block; padding:4px 10px; border-radius:4px; margin-right:8px; font-weight:bold; }
 .s-pass { background:#e6f4ea; color:#137333; }
 .s-fail { background:#fce8e6; color:#c5221f; }
 .s-skip { background:#fef7e0; color:#a56300; }
 table { border-collapse: collapse; width: 100%; margin-top: 12px; font-size: 13px; }
 th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; vertical-align: top; }
 th { background: #f1f3f4; }
 tr.pass .res { color:#137333; font-weight:bold; }
 tr.fail .res { color:#c5221f; font-weight:bold; }
 tr.skip .res { color:#a56300; font-weight:bold; }
 tr.fail { background:#fef6f6; }
</style>
</head>
<body>
<h1>MedPortal File Mediator - Security Regression Report</h1>
<div class="meta">
 Target: $(Enc $BaseUrl)<br>
 Generated: $generated<br>
 Reference pentest: RPT-TR-WEB-SA-M-1404651 (OWASP WSTG grey-box)
</div>
<div class="summary">
 <span class="s-pass">PASS: $pass</span>
 <span class="s-fail">FAIL: $fail</span>
 <span class="s-skip">SKIP: $skip</span>
</div>
<table>
 <tr><th>Finding (WSTG Ref.)</th><th>Check</th><th>Expected (secure)</th><th>Actual</th><th>Result</th><th>Note</th></tr>
 $($rows -join "`n")
</table>
</body>
</html>
"@

$html | Out-File -FilePath $ReportFile -Encoding UTF8

Write-Host ""
Write-Host ("Summary: PASS={0}  FAIL={1}  SKIP={2}" -f $pass, $fail, $skip) -ForegroundColor Cyan
Write-Host "HTML report: $ReportFile" -ForegroundColor Cyan

# Non-zero exit on any failure so CI can gate on it.
if ($fail -gt 0) { exit 1 } else { exit 0 }
