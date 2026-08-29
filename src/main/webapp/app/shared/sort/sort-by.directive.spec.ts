import { EventEmitter, signal } from '@angular/core';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';

import { SortByDirective } from './sort-by.directive';
import { SortDirective } from './sort.directive';

describe('Directive: SortByDirective', () => {
  let sortDirective: SortDirective<string>;
  let sortByDirective: SortByDirective<string>;
  // FaIconComponent.icon is a ModelSignal in v4. Stubbing it as a plain property is what let
  // the directive's old assignment pass these specs while throwing in a real browser.
  let icon: { icon: ReturnType<typeof signal<unknown>> };
  let sortChanges: { predicate: string; ascending: boolean }[];

  beforeEach(() => {
    sortDirective = new SortDirective<string>();
    sortDirective.predicateChange = new EventEmitter();
    sortDirective.ascendingChange = new EventEmitter();
    sortDirective.sortChange = new EventEmitter();
    sortChanges = [];
    sortDirective.sortChange.subscribe(v => sortChanges.push(v));

    sortByDirective = new SortByDirective<string>(sortDirective);
    sortByDirective.jhiSortBy = 'name';
    icon = { icon: signal<unknown>('sort') };
    sortByDirective.iconComponent = icon as unknown as FaIconComponent;
  });

  afterEach(() => {
    sortByDirective.ngOnDestroy();
  });

  it('should initialize predicate, order, icon when initial component predicate differs from column predicate', () => {
    sortDirective.predicate = 'id';
    sortByDirective.ngAfterContentInit();

    expect(sortByDirective.jhiSortBy).toEqual('name');
    expect(sortDirective.predicate).toEqual('id');
    expect(icon.icon()).toEqual(faSort);
    expect(sortChanges).toHaveLength(0);
  });

  it('should initialize predicate, order, icon when initial component predicate is same as column predicate', () => {
    sortDirective.predicate = 'name';
    sortDirective.ascending = true;
    sortByDirective.ngAfterContentInit();

    expect(sortByDirective.jhiSortBy).toEqual('name');
    expect(sortDirective.predicate).toEqual('name');
    expect(sortDirective.ascending).toEqual(true);
    expect(icon.icon()).toEqual(faSortUp);
    expect(sortChanges).toHaveLength(0);
  });

  it('should update component predicate, order, icon when user clicks on column header', () => {
    sortDirective.predicate = 'name';
    sortDirective.ascending = true;
    sortByDirective.ngAfterContentInit();

    sortByDirective.onClick();

    expect(sortDirective.predicate).toEqual('name');
    expect(sortDirective.ascending).toEqual(false);
    expect(icon.icon()).toEqual(faSortDown);
    expect(sortChanges).toHaveLength(1);
    expect(sortChanges[0]).toEqual({ predicate: 'name', ascending: false });
  });

  it('should update component predicate, order, icon when user double clicks on column header', () => {
    sortDirective.predicate = 'name';
    sortDirective.ascending = true;
    sortByDirective.ngAfterContentInit();

    sortByDirective.onClick();
    sortByDirective.onClick();

    expect(sortDirective.predicate).toEqual('name');
    expect(sortDirective.ascending).toEqual(true);
    expect(icon.icon()).toEqual(faSortUp);
    expect(sortChanges).toHaveLength(2);
    expect(sortChanges[0]).toEqual({ predicate: 'name', ascending: false });
    expect(sortChanges[1]).toEqual({ predicate: 'name', ascending: true });
  });

  it('should not run sorting on click if sorting icon is hidden', () => {
    sortDirective.predicate = 'id';
    sortDirective.ascending = false;
    sortByDirective.iconComponent = undefined;
    sortByDirective.ngAfterContentInit();

    sortByDirective.onClick();

    expect(sortDirective.predicate).toEqual('id');
    expect(sortDirective.ascending).toEqual(false);
    expect(sortChanges).toHaveLength(0);
  });
});
