package com.behsa.medportal.domain;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Spring Security role authority (ROLE_USER / ROLE_ADMIN).
 *
 * <p>Maps to {@code jhi_authority}. In this product the same table also stores
 * {@link MedAuthorityEntity} rows used for resource-based grants; role names are looked up by
 * {@code MedAuthorityRepository#findByNameIn}. Liquibase aligns the table to a numeric {@code id}
 * primary key (see {@code 20260711_001_align_authority_numeric_pk.xml}).
 */
@Entity
@Table(name = "jhi_authority")
public class Authority implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull
    @Column
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(length = 50)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Authority)) {
            return false;
        }
        return Objects.equals(name, ((Authority) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Authority{" +
            "id=" + id +
            "name='" + name + '\'' +
            "}";
    }
}
