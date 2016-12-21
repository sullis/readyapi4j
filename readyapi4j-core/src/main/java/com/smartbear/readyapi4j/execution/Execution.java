package com.smartbear.readyapi4j.execution;

import com.smartbear.readyapi.client.model.ProjectResultReport;
import com.smartbear.readyapi4j.result.RecipeExecutionResult;

import java.util.List;

/**
 * Represents an execution, synchronous or asynchronous, and encapsulates all
 * available information about the execution.
 */

public interface Execution {
    String getId();

    ProjectResultReport.StatusEnum getCurrentStatus();

    ProjectResultReport getCurrentReport();

    RecipeExecutionResult getExecutionResult();

    List<String> getErrorMessages();

    void cancelExecution();
}