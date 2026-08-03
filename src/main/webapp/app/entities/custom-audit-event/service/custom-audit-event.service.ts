import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ICustomAuditEvent, NewCustomAuditEvent } from '../custom-audit-event.model';

export type PartialUpdateCustomAuditEvent = Partial<ICustomAuditEvent> & Pick<ICustomAuditEvent, 'id'>;

type RestOf<T extends ICustomAuditEvent | NewCustomAuditEvent> = Omit<T, 'eventDate'> & {
  eventDate?: string | null;
};

export type RestCustomAuditEvent = RestOf<ICustomAuditEvent>;

export type NewRestCustomAuditEvent = RestOf<NewCustomAuditEvent>;

export type PartialUpdateRestCustomAuditEvent = RestOf<PartialUpdateCustomAuditEvent>;

export type EntityResponseType = HttpResponse<ICustomAuditEvent>;
export type EntityArrayResponseType = HttpResponse<ICustomAuditEvent[]>;

@Injectable({ providedIn: 'root' })
export class CustomAuditEventService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/custom-audit-events');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(customAuditEvent: NewCustomAuditEvent): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(customAuditEvent);
    return this.http
      .post<RestCustomAuditEvent>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(customAuditEvent: ICustomAuditEvent): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(customAuditEvent);
    return this.http
      .put<RestCustomAuditEvent>(`${this.resourceUrl}/${this.getCustomAuditEventIdentifier(customAuditEvent)}`, copy, {
        observe: 'response',
      })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(customAuditEvent: PartialUpdateCustomAuditEvent): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(customAuditEvent);
    return this.http
      .patch<RestCustomAuditEvent>(`${this.resourceUrl}/${this.getCustomAuditEventIdentifier(customAuditEvent)}`, copy, {
        observe: 'response',
      })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestCustomAuditEvent>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestCustomAuditEvent[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getCustomAuditEventIdentifier(customAuditEvent: Pick<ICustomAuditEvent, 'id'>): number {
    return customAuditEvent.id;
  }

  compareCustomAuditEvent(o1: Pick<ICustomAuditEvent, 'id'> | null, o2: Pick<ICustomAuditEvent, 'id'> | null): boolean {
    return o1 && o2 ? this.getCustomAuditEventIdentifier(o1) === this.getCustomAuditEventIdentifier(o2) : o1 === o2;
  }

  addCustomAuditEventToCollectionIfMissing<Type extends Pick<ICustomAuditEvent, 'id'>>(
    customAuditEventCollection: Type[],
    ...customAuditEventsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const customAuditEvents: Type[] = customAuditEventsToCheck.filter(isPresent);
    if (customAuditEvents.length > 0) {
      const customAuditEventCollectionIdentifiers = customAuditEventCollection.map(customAuditEventItem =>
        this.getCustomAuditEventIdentifier(customAuditEventItem),
      );
      const customAuditEventsToAdd = customAuditEvents.filter(customAuditEventItem => {
        const customAuditEventIdentifier = this.getCustomAuditEventIdentifier(customAuditEventItem);
        if (customAuditEventCollectionIdentifiers.includes(customAuditEventIdentifier)) {
          return false;
        }
        customAuditEventCollectionIdentifiers.push(customAuditEventIdentifier);
        return true;
      });
      return [...customAuditEventsToAdd, ...customAuditEventCollection];
    }
    return customAuditEventCollection;
  }

  protected convertDateFromClient<T extends ICustomAuditEvent | NewCustomAuditEvent | PartialUpdateCustomAuditEvent>(
    customAuditEvent: T,
  ): RestOf<T> {
    return {
      ...customAuditEvent,
      eventDate: customAuditEvent.eventDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restCustomAuditEvent: RestCustomAuditEvent): ICustomAuditEvent {
    return {
      ...restCustomAuditEvent,
      eventDate: restCustomAuditEvent.eventDate ? dayjs(restCustomAuditEvent.eventDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestCustomAuditEvent>): HttpResponse<ICustomAuditEvent> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestCustomAuditEvent[]>): HttpResponse<ICustomAuditEvent[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
