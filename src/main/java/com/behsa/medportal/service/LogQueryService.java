package com.behsa.medportal.service;

import com.behsa.medportal.domain.LogEntity;
import com.behsa.medportal.repository.LogRepository;
import com.behsa.medportal.service.criteria.LogCriteria;
import com.behsa.medportal.service.dto.LogDTO;
import com.behsa.medportal.service.dto.ProductDTO;
import com.behsa.medportal.service.mapper.LogMapper;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.jhipster.service.filter.RangeFilter;
import tech.jhipster.service.filter.StringFilter;

@Service
@Transactional(readOnly = true)
public class LogQueryService {

    private final Logger log = LoggerFactory.getLogger(LogQueryService.class);

    private final LogRepository logRepository;
    private final LogMapper logMapper;

    public LogQueryService(LogRepository logRepository, LogMapper logMapper) {
        this.logRepository = logRepository;
        this.logMapper = logMapper;
    }

    public List<LogDTO> findByCriteria(LogCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        Specification<LogEntity> spec = toSpecification(criteria);
        return logMapper.toDto(logRepository.findAll(spec));
    }

    public Page<LogDTO> findByCriteria(LogCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        Specification<LogEntity> spec = toSpecification(criteria);
        return logRepository.findAll(spec, page).map(logMapper::toDto);
    }

    public long countByCriteria(LogCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        Specification<LogEntity> spec = toSpecification(criteria);
        return logRepository.count(spec);
    }

    // ---------- Core: build Specification WITHOUT LogEntity_ ----------
    private Specification<LogEntity> toSpecification(LogCriteria c) {
        Specification<LogEntity> spec = Specification.where(null);

        if (c == null) return spec;

        // distinct handling without metamodel
        if (Boolean.TRUE.equals(c.getDistinct())) {
            spec = spec.and((root, query, cb) -> { query.distinct(true); return cb.conjunction(); });
        }

        // Numeric / id
        if (c.getId() != null)               spec = spec.and(range("id", c.getId()));

        // Strings
        if (c.getLogType() != null)          spec = spec.and(string("logType", c.getLogType()));
        if (c.getMsgType() != null)          spec = spec.and(string("msgType", c.getMsgType()));
        if (c.getCorrelationId() != null)    spec = spec.and(string("correlationId", c.getCorrelationId()));
        if (c.getReferenceType() != null)    spec = spec.and(string("referenceType", c.getReferenceType()));
        if (c.getReference() != null)        spec = spec.and(string("reference", c.getReference()));
        if (c.getModuleSource() != null)     spec = spec.and(string("moduleSource", c.getModuleSource()));
        if (c.getModuleDestination() != null)spec = spec.and(string("moduleDestination", c.getModuleDestination()));
        if (c.getError() != null)            spec = spec.and(string("error", c.getError()));
        if (c.getErrorDetails() != null)     spec = spec.and(string("errorDetails", c.getErrorDetails()));
        if (c.getRetryMode() != null)        spec = spec.and(string("retryMode", c.getRetryMode()));
        if (c.getRetryDestinationModule()!=null)
            spec = spec.and(string("retryDestinationModule", c.getRetryDestinationModule()));

        // Numbers (non-range Filter also supported by range() since LongFilter extends RangeFilter<Long>)
        if (c.getIsValidate() != null)       spec = spec.and(range("isValidate", c.getIsValidate()));
        if (c.getCheckFlag() != null)        spec = spec.and(range("checkFlag", c.getCheckFlag()));
        if (c.getRetryCount() != null)       spec = spec.and(range("retryCount", c.getRetryCount()));

        // Date/times
        if (c.getInitialDate() != null)      spec = spec.and(range("initialDate", c.getInitialDate()));
        if (c.getCreateDate() != null)       spec = spec.and(range("createDate", c.getCreateDate()));
        if (c.getExpireDate() != null)       spec = spec.and(range("expireDate", c.getExpireDate()));
        if (c.getInsertDate() != null)       spec = spec.and(range("insertDate", c.getInsertDate()));

        return spec;
    }

    // ---------- Helpers (string/equals/contains/specified/in/notIn) ----------
    private Specification<LogEntity> string(String attr, StringFilter f) {
        return (root, query, cb) -> {
            Path<String> p = root.get(attr);
            javax.persistence.criteria.Predicate predicate = cb.conjunction();

            if (f.getEquals() != null)          predicate = cb.and(predicate, cb.equal(p, f.getEquals()));
            if (f.getNotEquals() != null)       predicate = cb.and(predicate, cb.notEqual(p, f.getNotEquals()));
            if (Boolean.TRUE.equals(f.getSpecified()))  predicate = cb.and(predicate, cb.isNotNull(p));
            if (Boolean.FALSE.equals(f.getSpecified())) predicate = cb.and(predicate, cb.isNull(p));
            if (f.getIn() != null && !f.getIn().isEmpty())     predicate = cb.and(predicate, p.in(f.getIn()));
            if (f.getNotIn() != null && !f.getNotIn().isEmpty()) predicate = cb.and(predicate, cb.not(p.in(f.getNotIn())));
            if (f.getContains() != null) {
                String like = "%" + f.getContains().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.like(cb.lower(p), like));
            }
            if (f.getDoesNotContain() != null) {
                String nlike = "%" + f.getDoesNotContain().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.notLike(cb.lower(p), nlike));
            }
            return predicate;
        };
    }

    private <X extends Comparable<? super X>> Specification<LogEntity> range(String attr, RangeFilter<X> f) {
        return (root, query, cb) -> {
            Path<X> p = root.get(attr);
            javax.persistence.criteria.Predicate predicate = cb.conjunction();

            if (f.getEquals() != null)          predicate = cb.and(predicate, cb.equal(p, f.getEquals()));
            if (f.getNotEquals() != null)       predicate = cb.and(predicate, cb.notEqual(p, f.getNotEquals()));
            if (Boolean.TRUE.equals(f.getSpecified()))  predicate = cb.and(predicate, cb.isNotNull(p));
            if (Boolean.FALSE.equals(f.getSpecified())) predicate = cb.and(predicate, cb.isNull(p));
            if (f.getIn() != null && !f.getIn().isEmpty())     predicate = cb.and(predicate, p.in(f.getIn()));
            if (f.getNotIn() != null && !f.getNotIn().isEmpty()) predicate = cb.and(predicate, cb.not(p.in(f.getNotIn())));
            if (f.getGreaterThan() != null)           predicate = cb.and(predicate, cb.greaterThan(p, f.getGreaterThan()));
            if (f.getGreaterThanOrEqual() != null)    predicate = cb.and(predicate, cb.greaterThanOrEqualTo(p, f.getGreaterThanOrEqual()));
            if (f.getLessThan() != null)              predicate = cb.and(predicate, cb.lessThan(p, f.getLessThan()));
            if (f.getLessThanOrEqual() != null)       predicate = cb.and(predicate, cb.lessThanOrEqualTo(p, f.getLessThanOrEqual()));

            return predicate;
        };
    }

    public Page<LogDTO> searchByCorrelationId(String correlationId, Pageable pageable) {
        log.debug("Search logs by correlationId: {}", correlationId);
        return logRepository.findAllByCorrelationIdContainingIgnoreCase(correlationId, pageable)
            .map(logMapper::toDto);
    }
}
