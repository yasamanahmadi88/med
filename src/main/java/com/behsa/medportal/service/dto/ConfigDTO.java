package com.behsa.medportal.service.dto;

import com.behsa.medportal.domain.ConfigEntity;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * A DTO for the {@link ConfigEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String CONFIG_KEY_PATTERN = "^[A-Za-z0-9_.-]+$";
    private static final String CONFIG_KEY_MESSAGE = "Config property may contain only letters, digits, underscore, dot and dash";

    private static final String PLAIN_TEXT_PATTERN = "^[^<>]*$";
    private static final String PLAIN_TEXT_MESSAGE = "HTML content is not allowed";

    private Long id;

    @NotNull
    @Size(max = 100)
    @Pattern(regexp = CONFIG_KEY_PATTERN, message = CONFIG_KEY_MESSAGE)
    private String property;

    @NotNull
    @Size(max = 300)
    @Pattern(regexp = PLAIN_TEXT_PATTERN, message = PLAIN_TEXT_MESSAGE)
    private String pValue;

    @Size(max = 200)
    @Pattern(regexp = PLAIN_TEXT_PATTERN, message = PLAIN_TEXT_MESSAGE)
    private String commentDesc;

    private ModuleDTO module;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getpValue() {
        return pValue;
    }

    public void setpValue(String pValue) {
        this.pValue = pValue;
    }

    public String getCommentDesc() {
        return commentDesc;
    }

    public void setCommentDesc(String commentDesc) {
        this.commentDesc = commentDesc;
    }

    public ModuleDTO getModule() {
        return module;
    }

    public void setModule(ModuleDTO module) {
        this.module = module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigDTO)) {
            return false;
        }

        ConfigDTO configDTO = (ConfigDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, configDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConfigDTO{" +
            "id=" + getId() +
            ", property='" + getProperty() + "'" +
            ", pValue='" + getpValue() + "'" +
            ", commentDesc='" + getCommentDesc() + "'" +
            ", module=" + getModule() +
            "}";
    }
}
