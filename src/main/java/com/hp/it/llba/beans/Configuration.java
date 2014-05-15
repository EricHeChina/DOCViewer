package com.hp.it.llba.beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author mingxin.he@hp.com
 */
@Component
public class Configuration {
    @Value("${officeHomeParam}")
    private String officeHomeParam;
    @Value("${portNumbers}")
    private String portNumbers;

    @Value("${taskQueueTimeout}")
    private String taskQueueTimeout;
    @Value("${taskExecutionTimeout}")
    private String taskExecutionTimeout;

    @Value("${maxTasksPerProcess}")
    private String maxTasksPerProcess;
    @Value("${retryTimeout}")
    private String retryTimeout;

    public String getOfficeHomeParam() {
        return officeHomeParam;
    }

    public void setOfficeHomeParam(String officeHomeParam) {
        this.officeHomeParam = officeHomeParam;
    }

    public String getPortNumbers() {
        return portNumbers;
    }

    public void setPortNumbers(String portNumbers) {
        this.portNumbers = portNumbers;
    }

    public String getTaskQueueTimeout() {
        return taskQueueTimeout;
    }

    public void setTaskQueueTimeout(String taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

    public String getTaskExecutionTimeout() {
        return taskExecutionTimeout;
    }

    public void setTaskExecutionTimeout(String taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

    public String getMaxTasksPerProcess() {
        return maxTasksPerProcess;
    }

    public void setMaxTasksPerProcess(String maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
    }

    public String getRetryTimeout() {
        return retryTimeout;
    }

    public void setRetryTimeout(String retryTimeout) {
        this.retryTimeout = retryTimeout;
    }
}
