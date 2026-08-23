import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IConfig } from '../config.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../config.test-samples';

import { ConfigService } from './config.service';

const requireRestSample: IConfig = {
  ...sampleWithRequiredData,
};

describe('Config Service', () => {
  let service: ConfigService;
  let httpMock: HttpTestingController;
  let expectedResult: IConfig | IConfig[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(ConfigService);
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

    it('should create a Config', () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const config = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(config).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Config', () => {
      const config = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(config).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Config', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Config', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Config', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addConfigToCollectionIfMissing', () => {
      it('should add a Config to an empty array', () => {
        const config: IConfig = sampleWithRequiredData;
        expectedResult = service.addConfigToCollectionIfMissing([], config);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(config);
      });

      it('should not add a Config to an array that contains it', () => {
        const config: IConfig = sampleWithRequiredData;
        const configCollection: IConfig[] = [
          {
            ...config,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addConfigToCollectionIfMissing(configCollection, config);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Config to an array that doesn't contain it", () => {
        const config: IConfig = sampleWithRequiredData;
        const configCollection: IConfig[] = [sampleWithPartialData];
        expectedResult = service.addConfigToCollectionIfMissing(configCollection, config);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(config);
      });

      it('should add only unique Config to an array', () => {
        const configArray: IConfig[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const configCollection: IConfig[] = [sampleWithRequiredData];
        expectedResult = service.addConfigToCollectionIfMissing(configCollection, ...configArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const config: IConfig = sampleWithRequiredData;
        const config2: IConfig = sampleWithPartialData;
        expectedResult = service.addConfigToCollectionIfMissing([], config, config2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(config);
        expect(expectedResult).toContain(config2);
      });

      it('should accept null and undefined values', () => {
        const config: IConfig = sampleWithRequiredData;
        expectedResult = service.addConfigToCollectionIfMissing([], null, config, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(config);
      });

      it('should return initial array if no Config is added', () => {
        const configCollection: IConfig[] = [sampleWithRequiredData];
        expectedResult = service.addConfigToCollectionIfMissing(configCollection, undefined, null);
        expect(expectedResult).toEqual(configCollection);
      });
    });

    describe('compareConfig', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareConfig(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareConfig(entity1, entity2);
        const compareResult2 = service.compareConfig(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareConfig(entity1, entity2);
        const compareResult2 = service.compareConfig(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareConfig(entity1, entity2);
        const compareResult2 = service.compareConfig(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
