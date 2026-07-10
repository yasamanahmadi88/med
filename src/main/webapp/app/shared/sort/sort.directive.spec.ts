import { EventEmitter } from '@angular/core';

import { SortDirective } from './sort.directive';

describe('Directive: SortDirective', () => {
  let sortDirective: SortDirective<string>;
  let predicateChanges: Array<string | undefined>;
  let ascendingChanges: Array<boolean | undefined>;
  let sortChanges: Array<{ predicate: string; ascending: boolean }>;

  beforeEach(() => {
    sortDirective = new SortDirective<string>();
    predicateChanges = [];
    ascendingChanges = [];
    sortChanges = [];
    sortDirective.predicateChange = new EventEmitter();
    sortDirective.ascendingChange = new EventEmitter();
    sortDirective.sortChange = new EventEmitter();
    sortDirective.predicateChange.subscribe(v => predicateChanges.push(v));
    sortDirective.ascendingChange.subscribe(v => ascendingChanges.push(v));
    sortDirective.sortChange.subscribe(v => sortChanges.push(v));
  });

  it('should update predicate, order and invoke sortChange function', () => {
    sortDirective.sort('ID');

    expect(sortDirective.predicate).toEqual('ID');
    expect(sortDirective.ascending).toEqual(true);
    expect(sortChanges).toHaveLength(1);
    expect(sortChanges[0]).toEqual({ predicate: 'ID', ascending: true });
  });

  it('should change sort order to descending when same field is sorted again', () => {
    sortDirective.sort('ID');
    sortDirective.sort('ID');

    expect(sortDirective.predicate).toEqual('ID');
    expect(sortDirective.ascending).toEqual(false);
    expect(sortChanges).toHaveLength(2);
    expect(sortChanges[0]).toEqual({ predicate: 'ID', ascending: true });
    expect(sortChanges[1]).toEqual({ predicate: 'ID', ascending: false });
  });

  it('should change sort order to ascending when different field is sorted', () => {
    sortDirective.sort('ID');
    sortDirective.sort('NAME');

    expect(sortDirective.predicate).toEqual('NAME');
    expect(sortDirective.ascending).toEqual(true);
    expect(sortChanges).toHaveLength(2);
    expect(sortChanges[0]).toEqual({ predicate: 'ID', ascending: true });
    expect(sortChanges[1]).toEqual({ predicate: 'NAME', ascending: true });
  });
});
