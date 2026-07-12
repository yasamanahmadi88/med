import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { DATE_FORMAT } from 'app/config/input.constants';
import { IInstance } from '../instance.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../instance.test-samples';

import { InstanceService, RestInstance } from './instance.service';

const requireRestSample: RestInstance = {
  ...sampleWithRequiredData,
  lastUpdateDate: sampleWithRequiredData.lastUpdateDate?.format(DATE_FORMAT),
};

describe('Instance Service', () => {
  let service: InstanceService;
  let httpMock: HttpTestingController;
  let expectedResult: IInstance | IInstance[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(InstanceService);
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

    it('should create a Instance', () => {
      const instance = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(instance).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Instance', () => {
      const instance = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(instance).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Instance', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Instance', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Instance', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addInstanceToCollectionIfMissing', () => {
      it('should add a Instance to an empty array', () => {
        const instance: IInstance = sampleWithRequiredData;
        expectedResult = service.addInstanceToCollectionIfMissing([], instance);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(instance);
      });

      it('should not add a Instance to an array that contains it', () => {
        const instance: IInstance = sampleWithRequiredData;
        const instanceCollection: IInstance[] = [
          {
            ...instance,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addInstanceToCollectionIfMissing(instanceCollection, instance);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Instance to an array that doesn't contain it", () => {
        const instance: IInstance = sampleWithRequiredData;
        const instanceCollection: IInstance[] = [sampleWithPartialData];
        expectedResult = service.addInstanceToCollectionIfMissing(instanceCollection, instance);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(instance);
      });

      it('should add only unique Instance to an array', () => {
        const instanceArray: IInstance[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const instanceCollection: IInstance[] = [sampleWithRequiredData];
        expectedResult = service.addInstanceToCollectionIfMissing(instanceCollection, ...instanceArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const instance: IInstance = sampleWithRequiredData;
        const instance2: IInstance = sampleWithPartialData;
        expectedResult = service.addInstanceToCollectionIfMissing([], instance, instance2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(instance);
        expect(expectedResult).toContain(instance2);
      });

      it('should accept null and undefined values', () => {
        const instance: IInstance = sampleWithRequiredData;
        expectedResult = service.addInstanceToCollectionIfMissing([], null, instance, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(instance);
      });

      it('should return initial array if no Instance is added', () => {
        const instanceCollection: IInstance[] = [sampleWithRequiredData];
        expectedResult = service.addInstanceToCollectionIfMissing(instanceCollection, undefined, null);
        expect(expectedResult).toEqual(instanceCollection);
      });
    });

    describe('compareInstance', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareInstance(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareInstance(entity1, entity2);
        const compareResult2 = service.compareInstance(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareInstance(entity1, entity2);
        const compareResult2 = service.compareInstance(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareInstance(entity1, entity2);
        const compareResult2 = service.compareInstance(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
