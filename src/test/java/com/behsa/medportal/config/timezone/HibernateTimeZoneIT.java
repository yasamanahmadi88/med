package com.behsa.medportal.config.timezone;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.repository.timezone.DateTimeWrapper;
import com.behsa.medportal.repository.timezone.DateTimeWrapperRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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
 *
 * <p>Oracle TIMESTAMPTZ cannot always be read through {@code SqlRowSet#getString}/{@code getObject}
 * ("Invalid SQL type for column"). On Oracle, values are read via {@code TO_CHAR}; on H2 the
 * historical SqlRowSet path is preserved so existing timezone semantics stay intact.
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
    private Boolean oracleDatabase;

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

        String dbValue = readStoredColumn("instant", dateTimeWrapper.getId());
        String expectedValue = dateTimeFormatter.format(dateTimeWrapper.getInstant());

        assertStoredValueEquals(dbValue, expectedValue);
    }

    @Test
    @Transactional
    void storeLocalDateTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZone() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String dbValue = readStoredColumn("local_date_time", dateTimeWrapper.getId());
        String expectedValue = dateTimeWrapper.getLocalDateTime().atZone(ZoneId.systemDefault()).format(dateTimeFormatter);

        assertStoredValueEquals(dbValue, expectedValue);
    }

    @Test
    @Transactional
    void storeOffsetDateTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZone() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String dbValue = readStoredColumn("offset_date_time", dateTimeWrapper.getId());
        String expectedValue = dateTimeWrapper.getOffsetDateTime().format(dateTimeFormatter);

        assertStoredValueEquals(dbValue, expectedValue);
    }

    @Test
    @Transactional
    void storeZoneDateTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZone() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String dbValue = readStoredColumn("zoned_date_time", dateTimeWrapper.getId());
        String expectedValue = dateTimeWrapper.getZonedDateTime().format(dateTimeFormatter);

        assertStoredValueEquals(dbValue, expectedValue);
    }

    @Test
    @Transactional
    void storeLocalTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZoneAccordingToHis1stJan1970Value() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String dbValue = readStoredColumn("local_time", dateTimeWrapper.getId());
        String expectedValue = dateTimeWrapper
            .getLocalTime()
            .atDate(LocalDate.of(1970, Month.JANUARY, 1))
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter);

        assertStoredValueEquals(dbValue, expectedValue);
    }

    @Test
    @Transactional
    void storeOffsetTimeWithZoneIdConfigShouldBeStoredOnGMTTimeZoneAccordingToHis1stJan1970Value() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String dbValue = readStoredColumn("offset_time", dateTimeWrapper.getId());
        // Wall-clock local time of OffsetTime (H2 and Oracle TO_CHAR HH24:MI:SS both expose 14:30).
        String expectedValue = dateTimeWrapper
            .getOffsetTime()
            .toLocalTime()
            .atDate(LocalDate.of(1970, Month.JANUARY, 1))
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter);

        assertStoredValueEquals(dbValue, expectedValue);
    }

    @Test
    @Transactional
    void storeLocalDateWithZoneIdConfigShouldBeStoredWithoutTransformation() {
        dateTimeWrapperRepository.saveAndFlush(dateTimeWrapper);

        String dbValue = readStoredColumn("local_date", dateTimeWrapper.getId());
        String expectedValue = dateTimeWrapper.getLocalDate().format(dateFormatter);

        assertStoredValueEquals(dbValue, expectedValue);
    }

    private String readStoredColumn(String fieldName, long id) {
        if (isOracle()) {
            // Avoid Oracle TIMESTAMPTZ "Invalid SQL type for column" on SqlRowSet getters.
            // Always pass an explicit format — default TO_CHAR uses NLS (e.g. 12-NOV-14).
            String expression = oracleToCharExpression(fieldName);
            String sql = "SELECT %s FROM jhi_date_time_wrapper WHERE id = ?".formatted(expression);
            return jdbcTemplate.query(
                sql,
                rs -> {
                    assertThat(rs.next()).isTrue();
                    return rs.getString(1);
                },
                id
            );
        }

        String request = "SELECT %s FROM jhi_date_time_wrapper where id=%d".formatted(fieldName, id);
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(request);
        assertThat(resultSet.next()).isTrue();
        Object raw = resultSet.getObject(1);
        assertThat(raw).isNotNull();
        if (raw instanceof Timestamp timestamp) {
            return timestamp.toString();
        }
        return String.valueOf(raw);
    }

    private String oracleToCharExpression(String fieldName) {
        return switch (fieldName) {
            case "local_date" -> "TO_CHAR(%s, 'YYYY-MM-DD')".formatted(fieldName);
            case "local_time", "offset_time" -> "TO_CHAR(%s, 'HH24:MI:SS')".formatted(fieldName);
            case "instant", "offset_date_time", "zoned_date_time" ->
                "TO_CHAR(%s AT TIME ZONE 'UTC', 'YYYY-MM-DD HH24:MI:SS.FF1')".formatted(fieldName);
            default -> "TO_CHAR(%s, 'YYYY-MM-DD HH24:MI:SS.FF1')".formatted(fieldName);
        };
    }

    private boolean isOracle() {
        if (oracleDatabase != null) {
            return oracleDatabase;
        }
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            oracleDatabase = product != null && product.toLowerCase().contains("oracle");
        } catch (SQLException | NullPointerException ex) {
            oracleDatabase = false;
        }
        return oracleDatabase;
    }

    private void assertStoredValueEquals(String dbValue, String expectedValue) {
        assertThat(dbValue).isNotNull();
        assertThat(normalizeStoredDateTimeValue(dbValue, expectedValue)).isEqualTo(
            normalizeStoredDateTimeValue(expectedValue, expectedValue)
        );
    }

    private String normalizeStoredDateTimeValue(String value, String expectedHint) {
        String normalized = value.trim().replace('T', ' ');

        if (normalized.endsWith("Z")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        normalized = normalized.replaceFirst(
            "((?:\\d{4}-\\d{2}-\\d{2} )?\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d+)?)?)[+-]\\d{2}:\\d{2}$",
            "$1"
        );

        String timeOnlyHint = expectedHint.trim();
        boolean expectTimeOnly = timeOnlyHint.matches("\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d+)?)?");
        boolean expectDateOnly = timeOnlyHint.matches("\\d{4}-\\d{2}-\\d{2}");

        // TIME columns may render as "1970-01-01 HH:mm:ss" or "YYYY-MM-DD HH:mm:ss" (Oracle TO_CHAR).
        if (expectTimeOnly && normalized.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?")) {
            normalized = normalized.substring(11);
        }

        // Oracle DATE / TO_CHAR may render LocalDate as "2016-09-10 00:00:00"
        if (expectDateOnly && normalized.matches("\\d{4}-\\d{2}-\\d{2} 00:00:00(?:\\.\\d+)?")) {
            normalized = normalized.substring(0, 10);
        }

        if (normalized.matches("(?:\\d{4}-\\d{2}-\\d{2} )?\\d{2}:\\d{2}")) {
            normalized += ":00";
        }

        return normalized.replaceFirst("(\\d{2}:\\d{2}:\\d{2})\\.0+$", "$1");
    }
}
