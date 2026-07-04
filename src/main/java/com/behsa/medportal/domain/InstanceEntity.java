package com.behsa.medportal.domain;

import java.io.Serializable;
import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A InstanceEntity.
 */
@Entity
@Table(name = "MEDIATION.TBL_INSTANCES")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InstanceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "INSTANCE_KEY")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "module_name", length = 50, nullable = false)
    private String moduleName;

    @NotNull
    @Size(max = 10)
    @Column(name = "port", length = 10, nullable = false)
    private String port;

    @NotNull
    @Size(max = 50)
    @Column(name = "module_status", length = 50, nullable = false)
    private String moduleStatus;

    @NotNull
    @Column(name = "last_update_date", nullable = false)
    private LocalDate lastUpdateDate;

    @Column(name = "thread_pool_queue_size")
    private Long threadPoolQueueSize;

    @Column(name = "module_start_time")
    private Long moduleStartTime;

    @Column(name = "total_processed_work")
    private Long totalProcessedWork;

    @Column(name = "total_processed_task")
    private Long totalProcessedTask;

    @Column(name = "processed_statistics")
    private String processedStatistics;

    @NotNull
    @Size(max = 50)
    @Column(name = "host_name", length = 50, nullable = false)
    private String hostName;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InstanceEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public InstanceEntity moduleName(String moduleName) {
        this.setModuleName(moduleName);
        return this;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPort() {
        return this.port;
    }

    public InstanceEntity port(String port) {
        this.setPort(port);
        return this;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getModuleStatus() {
        return this.moduleStatus;
    }

    public InstanceEntity moduleStatus(String moduleStatus) {
        this.setModuleStatus(moduleStatus);
        return this;
    }

    public void setModuleStatus(String moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public LocalDate getLastUpdateDate() {
        return this.lastUpdateDate;
    }

    public InstanceEntity lastUpdateDate(LocalDate lastUpdateDate) {
        this.setLastUpdateDate(lastUpdateDate);
        return this;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getThreadPoolQueueSize() {
        return this.threadPoolQueueSize;
    }

    public InstanceEntity threadPoolQueueSize(Long threadPoolQueueSize) {
        this.setThreadPoolQueueSize(threadPoolQueueSize);
        return this;
    }

    public void setThreadPoolQueueSize(Long threadPoolQueueSize) {
        this.threadPoolQueueSize = threadPoolQueueSize;
    }

    public Long getModuleStartTime() {
        return this.moduleStartTime;
    }

    public InstanceEntity moduleStartTime(Long moduleStartTime) {
        this.setModuleStartTime(moduleStartTime);
        return this;
    }

    public void setModuleStartTime(Long moduleStartTime) {
        this.moduleStartTime = moduleStartTime;
    }

    public Long getTotalProcessedWork() {
        return this.totalProcessedWork;
    }

    public InstanceEntity totalProcessedWork(Long totalProcessedWork) {
        this.setTotalProcessedWork(totalProcessedWork);
        return this;
    }

    public void setTotalProcessedWork(Long totalProcessedWork) {
        this.totalProcessedWork = totalProcessedWork;
    }

    public Long getTotalProcessedTask() {
        return this.totalProcessedTask;
    }

    public InstanceEntity totalProcessedTask(Long totalProcessedTask) {
        this.setTotalProcessedTask(totalProcessedTask);
        return this;
    }

    public void setTotalProcessedTask(Long totalProcessedTask) {
        this.totalProcessedTask = totalProcessedTask;
    }

    public String getProcessedStatistics() {
        return this.processedStatistics;
    }

    public InstanceEntity processedStatistics(String processedStatistics) {
        this.setProcessedStatistics(processedStatistics);
        return this;
    }

    public void setProcessedStatistics(String processedStatistics) {
        this.processedStatistics = processedStatistics;
    }

    public String getHostName() {
        return this.hostName;
    }

    public InstanceEntity hostName(String hostName) {
        this.setHostName(hostName);
        return this;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InstanceEntity)) {
            return false;
        }
        return id != null && id.equals(((InstanceEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InstanceEntity{" +
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
