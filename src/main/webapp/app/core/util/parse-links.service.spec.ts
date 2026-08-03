import { vi } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { ParseLinks } from './parse-links.service';

describe('Parse links service test', () => {
  describe('Parse Links Service Test', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [ParseLinks],
      });
    });

    it('should throw an error when passed an empty string', () => {
      const service = TestBed.inject(ParseLinks);

      expect(function () {
        service.parse('');
      }).toThrow(new Error('input must not be of zero length'));
    });

    it('should throw an error when passed without comma', () => {
      const service = TestBed.inject(ParseLinks);

      expect(function () {
        service.parse('test');
      }).toThrow(new Error('section could not be split on ";"'));
    });

    it('should throw an error when passed without semicolon', () => {
      const service = TestBed.inject(ParseLinks);

      expect(function () {
        service.parse('test,test2');
      }).toThrow(new Error('section could not be split on ";"'));
    });

    it('should return links when headers are passed', () => {
      const service = TestBed.inject(ParseLinks);

      const links = { last: 0, first: 0 };
      expect(service.parse(' </api/audits?page=0&size=20>; rel="last",</api/audits?page=0&size=20>; rel="first"')).toEqual(links);
    });
  });
});
