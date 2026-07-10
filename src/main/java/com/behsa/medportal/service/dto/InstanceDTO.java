package com.behsa.medportal.service.dto;

import com.behsa.medportal.domain.InstanceEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link InstanceEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InstanceDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String moduleName;

    @NotNull
    @Size(max = 10)
    private String port;

    @NotNull
    @Size(max = 50)
    private String moduleStatus;

    @NotNull
    private LocalDate lastUpdateDate;

    private Long threadPoolQueueSize;

    private Long moduleStartTime;

    private Long totalProcessedWork;

    private Long totalProcessedTask;

    private String processedStatistics;

    @NotNull
    @Size(max = 50)
    private String hostName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(String moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public LocalDate getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getThreadPoolQueueSize() {
        return threadPoolQueueSize;
    }

    public void setThreadPoolQueueSize(Long threadPoolQueueSize) {
        this.threadPoolQueueSize = threadPoolQueueSize;
    }

    public Long getModuleStartTime() {
        return moduleStartTime;
    }

    public void setModuleStartTime(Long moduleStartTime) {
        this.moduleStartTime = moduleStartTime;
    }

    public Long getTotalProcessedWork() {
        return totalProcessedWork;
    }

    public void setTotalProcessedWork(Long totalProcessedWork) {
        this.totalProcessedWork = totalProcessedWork;
    }

    public Long getTotalProcessedTask() {
        return totalProcessedTask;
    }

    public void setTotalProcessedTask(Long totalProcessedTask) {
        this.totalProcessedTask = totalProcessedTask;
    }

    public String getProcessedStatistics() {
        return processedStatistics;
    }

    public void setProcessedStatistics(String processedStatistics) {
        this.processedStatistics = processedStatistics;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InstanceDTO)) {
            return false;
        }

        InstanceDTO instanceDTO = (InstanceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, instanceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InstanceDTO{" +
            "id=" + getId() +
            ", moduleName='" + getModuleName() + "'" +
            ", port='" + getPort() + "'" +
            ", moduleStatus='" + getModuleStatus() + "'" +
            ", lastUpdateDate='" + getLastUpdateDate() + "'" +
            ", threadPoolQueueSize=" + getThreadPoolQueueSize() +
            ", moduleStartTime=" + getModuleStartTime() +
            ", totalProcessedWork=" + getTotalProcessedWork() +
            ", totalProcessedTask=" + getTotalProcessedTask() +
            ", processedStatistics='" + getProcessedStatistics() + "'" +
            ", hostName='" + getHostName() + "'" +
            "}";
    }
}
