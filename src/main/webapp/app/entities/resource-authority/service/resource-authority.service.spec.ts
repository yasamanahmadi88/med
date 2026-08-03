import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IResourceAuthority } from '../resource-authority.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../resource-authority.test-samples';

import { ResourceAuthorityService } from './resource-authority.service';

const requireRestSample: IResourceAuthority = {
  ...sampleWithRequiredData,
};

describe('ResourceAuthority Service', () => {
  let service: ResourceAuthorityService;
  let httpMock: HttpTestingController;
  let expectedResult: IResourceAuthority | IResourceAuthority[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(ResourceAuthorityService);
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

    it('should create a ResourceAuthority', () => {
      const resourceAuthority = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(resourceAuthority).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ResourceAuthority', () => {
      const resourceAuthority = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(resourceAuthority).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ResourceAuthority', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ResourceAuthority', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ResourceAuthority', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addResourceAuthorityToCollectionIfMissing', () => {
      it('should add a ResourceAuthority to an empty array', () => {
        const resourceAuthority: IResourceAuthority = sampleWithRequiredData;
        expectedResult = service.addResourceAuthorityToCollectionIfMissing([], resourceAuthority);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(resourceAuthority);
      });

      it('should not add a ResourceAuthority to an array that contains it', () => {
        const resourceAuthority: IResourceAuthority = sampleWithRequiredData;
        const resourceAuthorityCollection: IResourceAuthority[] = [
          {
            ...resourceAuthority,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addResourceAuthorityToCollectionIfMissing(resourceAuthorityCollection, resourceAuthority);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ResourceAuthority to an array that doesn't contain it", () => {
        const resourceAuthority: IResourceAuthority = sampleWithRequiredData;
        const resourceAuthorityCollection: IResourceAuthority[] = [sampleWithPartialData];
        expectedResult = service.addResourceAuthorityToCollectionIfMissing(resourceAuthorityCollection, resourceAuthority);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(resourceAuthority);
      });

      it('should add only unique ResourceAuthority to an array', () => {
        const resourceAuthorityArray: IResourceAuthority[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const resourceAuthorityCollection: IResourceAuthority[] = [sampleWithRequiredData];
        expectedResult = service.addResourceAuthorityToCollectionIfMissing(resourceAuthorityCollection, ...resourceAuthorityArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const resourceAuthority: IResourceAuthority = sampleWithRequiredData;
        const resourceAuthority2: IResourceAuthority = sampleWithPartialData;
        expectedResult = service.addResourceAuthorityToCollectionIfMissing([], resourceAuthority, resourceAuthority2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(resourceAuthority);
        expect(expectedResult).toContain(resourceAuthority2);
      });

      it('should accept null and undefined values', () => {
        const resourceAuthority: IResourceAuthority = sampleWithRequiredData;
        expectedResult = service.addResourceAuthorityToCollectionIfMissing([], null, resourceAuthority, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(resourceAuthority);
      });

      it('should return initial array if no ResourceAuthority is added', () => {
        const resourceAuthorityCollection: IResourceAuthority[] = [sampleWithRequiredData];
        expectedResult = service.addResourceAuthorityToCollectionIfMissing(resourceAuthorityCollection, undefined, null);
        expect(expectedResult).toEqual(resourceAuthorityCollection);
      });
    });

    describe('compareResourceAuthority', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareResourceAuthority(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareResourceAuthority(entity1, entity2);
        const compareResult2 = service.compareResourceAuthority(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareResourceAuthority(entity1, entity2);
        const compareResult2 = service.compareResourceAuthority(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareResourceAuthority(entity1, entity2);
        const compareResult2 = service.compareResourceAuthority(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
