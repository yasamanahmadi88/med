package com.behsa.medportal.filter;

import tech.jhipster.service.filter.RangeFilter;
import java.time.LocalDateTime;

public class LocalDateTimeFilter extends RangeFilter<LocalDateTime> {

    public LocalDateTimeFilter() {
    }

    public LocalDateTimeFilter(LocalDateTimeFilter filter) {
        super(filter);
    }

    @Override
    public LocalDateTimeFilter copy() {
        return new LocalDateTimeFilter(this);
    }
}
