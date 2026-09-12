/**
 * Field validators for the integration-module properties.
 *
 * Four fields carried client-side validation in the Vue editor, each written inline in its own
 * component: `fileTransmitter.ip`, `fileTransmitter.port`, `httpTransmitter.authUrl` and
 * `merger.expireTimeOfDay`. Those four are here, with the same patterns and the same messages.
 *
 * Every one of them treats an empty value as valid — none of these properties is required, and
 * the Vue components cleared the error before testing the pattern. A schema field opts in by
 * naming a validator; nothing validates by default.
 */

/** Returns the message to show, or `undefined` when the value is acceptable. */
export type FieldValidator = (value: string) => string | undefined;

const IPV4 = /^(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)){3}$/;
const TIME_OF_DAY = /^([0-1]?\d|2[0-3]):([0-5]\d):([0-5]\d)$/;
/**
 * A URL: either an `http`/`https` scheme with any host, or a dotted host on its own.
 *
 * The Vue pattern made the scheme optional but always demanded a dot in the host, so
 * `http://localhost:8080` and `http://auth-service:8080` — a service name inside a compose
 * network or a cluster, which is what these fields usually hold — were rejected, and with the
 * Save gate that meant the flow could not be saved at all. This accepts everything Vue's pattern
 * did and adds the schemed dotless host; a bare `foo` with neither scheme nor dot is still not a
 * URL. Widening only: no value that was valid before becomes invalid now.
 */
const URL_PATTERN = /^(https?:\/\/(?:[\w-]+\.)*[\w-]+|(?:[\w-]+\.)+[\w-]+)(:\d+)?(\/[\w\-./?%&=@#!$%^*()+-]*)?$/;
/** Digits only. See `port` below for why `Number()` on its own is not enough. */
const DIGITS = /^\d+$/;

const MAX_PORT = 65535;

function ipv4(value: string): string | undefined {
  return IPV4.test(value.trim()) ? undefined : 'Invalid IPv4 format (e.g., 192.168.1.1)';
}

/**
 * A TCP port: a plain decimal integer in `0`–`65535`.
 *
 * Stricter than the Vue check, which ran `Number(value)` and asked whether the result was an
 * integer in range — so `1e2`, `0x10`, ` 12 ` and `12.0` all passed and were written to the
 * diagram as-is. None of those is a port number; a digits-only test is what the field means.
 */
function port(value: string): string | undefined {
  const trimmed = value.trim();
  if (!DIGITS.test(trimmed) || Number(trimmed) > MAX_PORT) {
    return `Port must be an integer between 0 and ${MAX_PORT}`;
  }
  return undefined;
}

/**
 * A URL, by the Vue editor's own pattern.
 *
 * The messages are Vue's, with one reordering: a value holding a space is reported as such even
 * when it also lacks a scheme. Vue tested the scheme first, so `http://a b.com` — where the space
 * is the actual problem — was reported as missing the `http://` it plainly has.
 */
function url(value: string): string | undefined {
  const trimmed = value.trim();
  if (URL_PATTERN.test(trimmed)) {
    return undefined;
  }
  if (trimmed.includes(' ')) {
    return 'URL cannot contain spaces';
  }
  if (!trimmed.startsWith('http://') && !trimmed.startsWith('https://')) {
    return 'URL should start with http:// or https:// or end with a domain';
  }
  return 'Invalid URL format';
}

function timeOfDay(value: string): string | undefined {
  return TIME_OF_DAY.test(value.trim()) ? undefined : 'Time must be in HH:mm:ss format';
}

const VALIDATORS = { ipv4, port, url, timeOfDay } satisfies Record<string, FieldValidator>;

/** The validators a schema field may name. */
export type ValidatorName = keyof typeof VALIDATORS;

/**
 * Runs the named validator, treating a blank value as acceptable.
 *
 * The empty check lives here rather than in each validator so the rule cannot drift between
 * them — it is the one behaviour all four shared, and the one a new validator would forget.
 */
export function validateField(name: ValidatorName, value: unknown): string | undefined {
  // moddle gives back a string for every attribute it read from the XML, and the number entry
  // hands back a number; anything else is not a value these fields can hold, and there is
  // nothing useful to say about it.
  const text = typeof value === 'string' ? value : typeof value === 'number' ? String(value) : '';
  return text.trim() === '' ? undefined : VALIDATORS[name](text);
}
