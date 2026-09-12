import { validateField } from './validators';

/**
 * These four validators are the only thing standing between a typo and a flow the engine cannot
 * run, and they gate the Save button, so both directions matter: a value they wrongly reject is
 * a flow the user cannot save at all.
 */
describe('module property validators', () => {
  describe('every validator', () => {
    it('accepts a blank value', () => {
      // None of these properties is required, and the Vue components cleared the error before
      // testing the pattern. A field the user has not filled in must not block Save.
      for (const name of ['ipv4', 'port', 'url', 'timeOfDay'] as const) {
        expect(validateField(name, ''), name).toBeUndefined();
        expect(validateField(name, '   '), name).toBeUndefined();
        expect(validateField(name, undefined), name).toBeUndefined();
        expect(validateField(name, null), name).toBeUndefined();
      }
    });

    it('tolerates a non-string value from the diagram', () => {
      // moddle hands back whatever the XML held; a port read from an imported file arrives as a
      // string, but nothing guarantees it.
      expect(validateField('port', 8080)).toBeUndefined();
      expect(validateField('port', 99999)).toBeDefined();
    });
  });

  describe('ipv4', () => {
    it('accepts a dotted quad', () => {
      for (const value of ['192.168.1.1', '0.0.0.0', '255.255.255.255', '10.0.0.7']) {
        expect(validateField('ipv4', value), value).toBeUndefined();
      }
    });

    it('rejects anything else', () => {
      for (const value of ['256.1.1.1', '192.168.1', '192.168.1.1.1', 'localhost', '1.2.3.4.', '::1']) {
        expect(validateField('ipv4', value), value).toBe('Invalid IPv4 format (e.g., 192.168.1.1)');
      }
    });

    it('ignores surrounding whitespace', () => {
      expect(validateField('ipv4', '  10.0.0.1  ')).toBeUndefined();
    });
  });

  describe('port', () => {
    it('accepts the whole port range', () => {
      for (const value of ['0', '1', '8080', '65535']) {
        expect(validateField('port', value), value).toBeUndefined();
      }
    });

    it('rejects a port above the range', () => {
      expect(validateField('port', '65536')).toBe('Port must be an integer between 0 and 65535');
    });

    it('rejects what is not a plain decimal integer', () => {
      // The Vue check ran `Number(value)` and asked whether the result was an integer in range,
      // so all of these passed and were written to the diagram verbatim. None is a port.
      for (const value of ['1e2', '0x10', '12.0', '-1', '80abc', ' ']) {
        if (value.trim() === '') {
          continue;
        }
        expect(validateField('port', value), value).toBeDefined();
      }
    });
  });

  describe('url', () => {
    it('accepts the shapes these fields hold', () => {
      for (const value of [
        'http://example.com',
        'https://a.b.co/path?x=1&y=2',
        'example.com',
        '10.0.0.1:9000/path',
        'https://example.com:8443',
      ]) {
        expect(validateField('url', value), value).toBeUndefined();
      }
    });

    it('accepts a schemed host with no dot in it', () => {
      // The Vue pattern demanded a dot in the host, so a service name inside a compose network or
      // a cluster — which is what these fields usually hold — could not be saved at all.
      expect(validateField('url', 'http://localhost:8080')).toBeUndefined();
      expect(validateField('url', 'http://auth-service:8080/token')).toBeUndefined();
    });

    it('still rejects a bare word with neither scheme nor domain', () => {
      expect(validateField('url', 'foo')).toBe('URL should start with http:// or https:// or end with a domain');
    });

    it('reports a space as a space', () => {
      // Vue tested the scheme first, so this was reported as missing the `http://` it plainly has.
      expect(validateField('url', 'http://a b.com')).toBe('URL cannot contain spaces');
    });

    it('rejects a scheme it does not handle', () => {
      expect(validateField('url', 'ftp://example.com')).toBeDefined();
    });
  });

  describe('timeOfDay', () => {
    it('accepts HH:mm:ss', () => {
      for (const value of ['00:00:00', '9:30:00', '09:30:00', '23:59:59']) {
        expect(validateField('timeOfDay', value), value).toBeUndefined();
      }
    });

    it('rejects an out-of-range or malformed time', () => {
      for (const value of ['24:00:00', '12:60:00', '12:00:60', '12:00', '12-00-00', 'noon']) {
        expect(validateField('timeOfDay', value), value).toBe('Time must be in HH:mm:ss format');
      }
    });
  });
});
