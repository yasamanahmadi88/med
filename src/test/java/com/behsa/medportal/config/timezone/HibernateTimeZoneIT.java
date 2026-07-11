package com.behsa.medportal.config.timezone;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.repository.timezone.DateTimeWrapper;
import com.behsa.medportal.repository.timezone.DateTimeWrapperRepository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the ZoneId Hibernate configuration.
 */
@IntegrationTest
class HibernateTimeZoneIT {

    @Autowired
    private DateTimeWrapperRepository dateTimeWrapperRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.properties.hibernate.jdbc.time_zone:UTC}")
    private String zoneId;

    private DateTimeWrapper dateTimeWrapper;
    private DateTimeFormatter dateTimeFormatter;
    private DateTimeFormatter timeFormatter;
    private DateTimeFormatter dateFormatter;

    @BeforeEach
    public void setup() {
        dateTimeWrapper = new DateTimeWrapper();
        dateTimeWrapper.setInstant(Instant.parse("2014-11-12T05:50:00.0Z"));
        dateTimeWrapper.setLocalDateTime(LocalDateTime.parse("2014-11-12T07:50:00.0"));
        dateTimeWrapper.setOffsetDateTime(OffsetDateTime.parse("2011-12-14T08:30:00.0Z"));
        dateTimeWrapper.setZonedDateTime(ZonedDateTime.parse("2011-12-14T08:30:00.0Z"));
        dateTimeWrapper.setLocalTime(LocalTime.parse("14:30:00"));
        dateTimeWrapper.setOffsetTime(OffsetTime.parse("14:30:00+02:00"));
        dateTimeWrapper.setLocalDate(LocalDate.parse("2016-09-10"));

        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").withZone(ZoneId.of(zoneId));

        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of(zoneId));

        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    @Test
    @Transactional
    void storeInstantWithZoneIdConfigShouldBeStoredOnGMTTimeZone() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String request = generateSqlRequest("instant", dateTimeWrapper.getId());
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        String expectedValue = dateTimeFormatter.format(dateTimeWrapper.getInstant());

        assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(resultSet, expectedValue);
    }

    @Test
    @Transactional
    void storeLocalDateTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZone() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String request = generateSqlRequest("local_date_time", dateTimeWrapper.getId());
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        String expectedValue = dateTimeWrapper.getLocalDateTime().atZone(ZoneId.systemDefault()).format(dateTimeFormatter);

        assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(resultSet, expectedValue);
    }

    @Test
    @Transactional
    void storeOffsetDateTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZone() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String request = generateSqlRequest("offset_date_time", dateTimeWrapper.getId());
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        String expectedValue = dateTimeWrapper.getOffsetDateTime().format(dateTimeFormatter);

        assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(resultSet, expectedValue);
    }

    @Test
    @Transactional
    void storeZoneDateTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZone() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String request = generateSqlRequest("zoned_date_time", dateTimeWrapper.getId());
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        String expectedValue = dateTimeWrapper.getZonedDateTime().format(dateTimeFormatter);

        assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(resultSet, expectedValue);
    }

    @Test
    @Transactional
    void storeLocalTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZoneAccordingToHis1stJan1970Value() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String request = generateSqlRequest("local_time", dateTimeWrapper.getId());
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        String expectedValue = dateTimeWrapper
            .getLocalTime()
            .atDate(LocalDate.of(1970, Month.JANUARY, 1))
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter);

        assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(resultSet, expectedValue);
    }

    @Test
    @Transactional
    void storeOffsetTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZoneAccordingToHis1stJan1970Value() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String request = generateSqlRequest("offset_time", dateTimeWrapper.getId());
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        String expectedValue = dateTimeWrapper
            .getOffsetTime()
            .toLocalTime()
            .atDate(LocalDate.of(1970, Month.JANUARY, 1))
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter);

        assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(resultSet, expectedValue);
    }

    @Test
    @Transactional
    void storeLocalDateWithZoneIdConfigShouldBeStoredWithoutTransformation() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String request = generateSqlRequest("local_date", dateTimeWrapper.getId());
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        String expectedValue = dateTimeWrapper.getLocalDate().format(dateFormatter);

        assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(resultSet, expectedValue);
    }

    private String generateSqlRequest(String fieldName, long id) {
        return "SELECT %s FROM jhi_date_time_wrapper where id=%d".formatted(fieldName, id);
    }

    private void assertThatDateStoredValueIsEqualToInsertDateValueOnGMTTimeZone(SqlRowSet sqlRowSet, String expectedValue) {
        while (sqlRowSet.next()) {
            // Oracle TIMESTAMPTZ / TIMESTAMP WITH LOCAL TIME ZONE cannot always be read via getString.
            Object raw = sqlRowSet.getObject(1);
            assertThat(raw).isNotNull();
            String dbValue = raw instanceof java.sql.Timestamp timestamp
                ? timestamp.toString()
                : String.valueOf(raw);

            assertThat(normalizeStoredDateTimeValue(dbValue)).isEqualTo(normalizeStoredDateTimeValue(expectedValue));
        }
    }

    private String normalizeStoredDateTimeValue(String value) {
        String normalized = value.trim().replace('T', ' ');

        if (normalized.endsWith("Z")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        normalized = normalized.replaceFirst(
            "((?:\\d{4}-\\d{2}-\\d{2} )?\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d+)?)?)[+-]\\d{2}:\\d{2}$",
            "$1"
        );

        // Oracle often returns TIME as epoch date + time: "1970-01-01 14:30:00"
        if (normalized.matches("1970-01-01 \\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?")) {
            normalized = normalized.substring("1970-01-01 ".length());
        }

        // Oracle DATE may render as "2016-09-10 00:00:00" for a LocalDate column
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2} 00:00:00(?:\\.\\d+)?")) {
            normalized = normalized.substring(0, 10);
        }

        if (normalized.matches("(?:\\d{4}-\\d{2}-\\d{2} )?\\d{2}:\\d{2}")) {
            normalized += ":00";
        }

        return normalized.replaceFirst("(\\d{2}:\\d{2}:\\d{2})\\.0+$", "$1");
    }
}