import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IMedAuthority } from '../med-authority.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../med-authority.test-samples';

import { MedAuthorityService } from './med-authority.service';

const requireRestSample: IMedAuthority = {
  ...sampleWithRequiredData,
};

describe('MedAuthority Service', () => {
  let service: MedAuthorityService;
  let httpMock: HttpTestingController;
  let expectedResult: IMedAuthority | IMedAuthority[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(MedAuthorityService);
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

    it('should create a MedAuthority', () => {
      const medAuthority = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(medAuthority).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a MedAuthority', () => {
      const medAuthority = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(medAuthority).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a MedAuthority', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of MedAuthority', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a MedAuthority', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addMedAuthorityToCollectionIfMissing', () => {
      it('should add a MedAuthority to an empty array', () => {
        const medAuthority: IMedAuthority = sampleWithRequiredData;
        expectedResult = service.addMedAuthorityToCollectionIfMissing([], medAuthority);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(medAuthority);
      });

      it('should not add a MedAuthority to an array that contains it', () => {
        const medAuthority: IMedAuthority = sampleWithRequiredData;
        const medAuthorityCollection: IMedAuthority[] = [
          {
            ...medAuthority,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addMedAuthorityToCollectionIfMissing(medAuthorityCollection, medAuthority);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a MedAuthority to an array that doesn't contain it", () => {
        const medAuthority: IMedAuthority = sampleWithRequiredData;
        const medAuthorityCollection: IMedAuthority[] = [sampleWithPartialData];
        expectedResult = service.addMedAuthorityToCollectionIfMissing(medAuthorityCollection, medAuthority);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(medAuthority);
      });

      it('should add only unique MedAuthority to an array', () => {
        const medAuthorityArray: IMedAuthority[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const medAuthorityCollection: IMedAuthority[] = [sampleWithRequiredData];
        expectedResult = service.addMedAuthorityToCollectionIfMissing(medAuthorityCollection, ...medAuthorityArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const medAuthority: IMedAuthority = sampleWithRequiredData;
        const medAuthority2: IMedAuthority = sampleWithPartialData;
        expectedResult = service.addMedAuthorityToCollectionIfMissing([], medAuthority, medAuthority2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(medAuthority);
        expect(expectedResult).toContain(medAuthority2);
      });

      it('should accept null and undefined values', () => {
        const medAuthority: IMedAuthority = sampleWithRequiredData;
        expectedResult = service.addMedAuthorityToCollectionIfMissing([], null, medAuthority, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(medAuthority);
      });

      it('should return initial array if no MedAuthority is added', () => {
        const medAuthorityCollection: IMedAuthority[] = [sampleWithRequiredData];
        expectedResult = service.addMedAuthorityToCollectionIfMissing(medAuthorityCollection, undefined, null);
        expect(expectedResult).toEqual(medAuthorityCollection);
      });
    });

    describe('compareMedAuthority', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareMedAuthority(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareMedAuthority(entity1, entity2);
        const compareResult2 = service.compareMedAuthority(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareMedAuthority(entity1, entity2);
        const compareResult2 = service.compareMedAuthority(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareMedAuthority(entity1, entity2);
        const compareResult2 = service.compareMedAuthority(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
