import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IVersion } from '../version.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../version.test-samples';

import { VersionService } from './version.service';

const requireRestSample: IVersion = {
  ...sampleWithRequiredData,
};

describe('Version Service', () => {
  let service: VersionService;
  let httpMock: HttpTestingController;
  let expectedResult: IVersion | IVersion[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(VersionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Version', () => {
      const version = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(version).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Version', () => {
      const version = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(version).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Version', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Version', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Version', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addVersionToCollectionIfMissing', () => {
      it('should add a Version to an empty array', () => {
        const version: IVersion = sampleWithRequiredData;
        expectedResult = service.addVersionToCollectionIfMissing([], version);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(version);
      });

      it('should not add a Version to an array that contains it', () => {
        const version: IVersion = sampleWithRequiredData;
        const versionCollection: IVersion[] = [
          {
            ...version,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addVersionToCollectionIfMissing(versionCollection, version);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Version to an array that doesn't contain it", () => {
        const version: IVersion = sampleWithRequiredData;
        const versionCollection: IVersion[] = [sampleWithPartialData];
        expectedResult = service.addVersionToCollectionIfMissing(versionCollection, version);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(version);
      });

      it('should add only unique Version to an array', () => {
        const versionArray: IVersion[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const versionCollection: IVersion[] = [sampleWithRequiredData];
        expectedResult = service.addVersionToCollectionIfMissing(versionCollection, ...versionArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const version: IVersion = sampleWithRequiredData;
        const version2: IVersion = sampleWithPartialData;
        expectedResult = service.addVersionToCollectionIfMissing([], version, version2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(version);
        expect(expectedResult).toContain(version2);
      });

      it('should accept null and undefined values', () => {
        const version: IVersion = sampleWithRequiredData;
        expectedResult = service.addVersionToCollectionIfMissing([], null, version, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(version);
      });

      it('should return initial array if no Version is added', () => {
        const versionCollection: IVersion[] = [sampleWithRequiredData];
        expectedResult = service.addVersionToCollectionIfMissing(versionCollection, undefined, null);
        expect(expectedResult).toEqual(versionCollection);
      });
    });

    describe('compareVersion', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareVersion(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareVersion(entity1, entity2);
        const compareResult2 = service.compareVersion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareVersion(entity1, entity2);
        const compareResult2 = service.compareVersion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareVersion(entity1, entity2);
        const compareResult2 = service.compareVersion(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
