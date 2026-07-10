package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;

import com.behsa.medportal.domain.InstanceEntity;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link InstanceEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.InstanceResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /instances?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InstanceCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter moduleName;

    private StringFilter port;

    private StringFilter moduleStatus;

    private LocalDateFilter lastUpdateDate;

    private LongFilter threadPoolQueueSize;

    private LongFilter moduleStartTime;

    private LongFilter totalProcessedWork;

    private LongFilter totalProcessedTask;

    private StringFilter processedStatistics;

    private StringFilter hostName;

    private Boolean distinct;

    public InstanceCriteria() {}

    public InstanceCriteria(InstanceCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.moduleName = other.moduleName == null ? null : other.moduleName.copy();
        this.port = other.port == null ? null : other.port.copy();
        this.moduleStatus = other.moduleStatus == null ? null : other.moduleStatus.copy();
        this.lastUpdateDate = other.lastUpdateDate == null ? null : other.lastUpdateDate.copy();
        this.threadPoolQueueSize = other.threadPoolQueueSize == null ? null : other.threadPoolQueueSize.copy();
        this.moduleStartTime = other.moduleStartTime == null ? null : other.moduleStartTime.copy();
        this.totalProcessedWork = other.totalProcessedWork == null ? null : other.totalProcessedWork.copy();
        this.totalProcessedTask = other.totalProcessedTask == null ? null : other.totalProcessedTask.copy();
        this.processedStatistics = other.processedStatistics == null ? null : other.processedStatistics.copy();
        this.hostName = other.hostName == null ? null : other.hostName.copy();
        this.distinct = other.distinct;
    }

    @Override
    public InstanceCriteria copy() {
        return new InstanceCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getModuleName() {
        return moduleName;
    }

    public StringFilter moduleName() {
        if (moduleName == null) {
            moduleName = new StringFilter();
        }
        return moduleName;
    }

    public void setModuleName(StringFilter moduleName) {
        this.moduleName = moduleName;
    }

    public StringFilter getPort() {
        return port;
    }

    public StringFilter port() {
        if (port == null) {
            port = new StringFilter();
        }
        return port;
    }

    public void setPort(StringFilter port) {
        this.port = port;
    }

    public StringFilter getModuleStatus() {
        return moduleStatus;
    }

    public StringFilter moduleStatus() {
        if (moduleStatus == null) {
            moduleStatus = new StringFilter();
        }
        return moduleStatus;
    }

    public void setModuleStatus(StringFilter moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public LocalDateFilter getLastUpdateDate() {
        return lastUpdateDate;
    }

    public LocalDateFilter lastUpdateDate() {
        if (lastUpdateDate == null) {
            lastUpdateDate = new LocalDateFilter();
        }
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateFilter lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public LongFilter getThreadPoolQueueSize() {
        return threadPoolQueueSize;
    }

    public LongFilter threadPoolQueueSize() {
        if (threadPoolQueueSize == null) {
            threadPoolQueueSize = new LongFilter();
        }
        return threadPoolQueueSize;
    }

    public void setThreadPoolQueueSize(LongFilter threadPoolQueueSize) {
        this.threadPoolQueueSize = threadPoolQueueSize;
    }

    public LongFilter getModuleStartTime() {
        return moduleStartTime;
    }

    public LongFilter moduleStartTime() {
        if (moduleStartTime == null) {
            moduleStartTime = new LongFilter();
        }
        return moduleStartTime;
    }

    public void setModuleStartTime(LongFilter moduleStartTime) {
        this.moduleStartTime = moduleStartTime;
    }

    public LongFilter getTotalProcessedWork() {
        return totalProcessedWork;
    }

    public LongFilter totalProcessedWork() {
        if (totalProcessedWork == null) {
            totalProcessedWork = new LongFilter();
        }
        return totalProcessedWork;
    }

    public void setTotalProcessedWork(LongFilter totalProcessedWork) {
        this.totalProcessedWork = totalProcessedWork;
    }

    public LongFilter getTotalProcessedTask() {
        return totalProcessedTask;
    }

    public LongFilter totalProcessedTask() {
        if (totalProcessedTask == null) {
            totalProcessedTask = new LongFilter();
        }
        return totalProcessedTask;
    }

    public void setTotalProcessedTask(LongFilter totalProcessedTask) {
        this.totalProcessedTask = totalProcessedTask;
    }

    public StringFilter getProcessedStatistics() {
        return processedStatistics;
    }

    public StringFilter processedStatistics() {
        if (processedStatistics == null) {
            processedStatistics = new StringFilter();
        }
        return processedStatistics;
    }

    public void setProcessedStatistics(StringFilter processedStatistics) {
        this.processedStatistics = processedStatistics;
    }

    public StringFilter getHostName() {
        return hostName;
    }

    public StringFilter hostName() {
        if (hostName == null) {
            hostName = new StringFilter();
        }
        return hostName;
    }

    public void setHostName(StringFilter hostName) {
        this.hostName = hostName;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InstanceCriteria that = (InstanceCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(moduleName, that.moduleName) &&
            Objects.equals(port, that.port) &&
            Objects.equals(moduleStatus, that.moduleStatus) &&
            Objects.equals(lastUpdateDate, that.lastUpdateDate) &&
            Objects.equals(threadPoolQueueSize, that.threadPoolQueueSize) &&
            Objects.equals(moduleStartTime, that.moduleStartTime) &&
            Objects.equals(totalProcessedWork, that.totalProcessedWork) &&
            Objects.equals(totalProcessedTask, that.totalProcessedTask) &&
            Objects.equals(processedStatistics, that.processedStatistics) &&
            Objects.equals(hostName, that.hostName) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            moduleName,
            port,
            moduleStatus,
            lastUpdateDate,
            threadPoolQueueSize,
            moduleStartTime,
            totalProcessedWork,
            totalProcessedTask,
            processedStatistics,
            hostName,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InstanceCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (moduleName != null ? "moduleName=" + moduleName + ", " : "") +
            (port != null ? "port=" + port + ", " : "") +
            (moduleStatus != null ? "moduleStatus=" + moduleStatus + ", " : "") +
            (lastUpdateDate != null ? "lastUpdateDate=" + lastUpdateDate + ", " : "") +
            (threadPoolQueueSize != null ? "threadPoolQueueSize=" + threadPoolQueueSize + ", " : "") +
            (moduleStartTime != null ? "moduleStartTime=" + moduleStartTime + ", " : "") +
            (totalProcessedWork != null ? "totalProcessedWork=" + totalProcessedWork + ", " : "") +
            (totalProcessedTask != null ? "totalProcessedTask=" + totalProcessedTask + ", " : "") +
            (processedStatistics != null ? "processedStatistics=" + processedStatistics + ", " : "") +
            (hostName != null ? "hostName=" + hostName + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
