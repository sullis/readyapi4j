package com.smartbear.readyapi4j.local.execution;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlProjectRunner;
import com.eviware.soapui.model.support.ProjectRunListenerAdapter;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.smartbear.ready.recipe.JsonRecipeParser;
import com.smartbear.ready.recipe.teststeps.TestCaseStruct;
import com.smartbear.readyapi.client.model.ProjectResultReport;
import com.smartbear.readyapi.client.model.TestCase;
import com.smartbear.readyapi4j.ExecutionListener;
import com.smartbear.readyapi4j.TestRecipe;
import com.smartbear.readyapi4j.execution.Execution;
import com.smartbear.readyapi4j.execution.RecipeExecutionException;
import com.smartbear.readyapi4j.execution.RecipeExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class that can execute a Test recipe locally, using the SoapUI core classes.
 */
public class SoapUIRecipeExecutor implements RecipeExecutor {
    private static final String LOCAL_CLIENT_EXECUTION_ID = "SoapUILocalClient#ExecutionId";

    private final Map<String, SoapUIRecipeExecution> executionsMap = new HashMap<>();
    private final JsonRecipeParser recipeParser = new JsonRecipeParser();
    private final List<ExecutionListener> executionListeners = new CopyOnWriteArrayList<>();
    private ObjectMapper objectMapper;

    @Override
    public Execution submitRecipe(TestRecipe recipe) {
        return postTestCase(recipe.getTestCase(), true);
    }

    @Override
    public Execution executeRecipe(TestRecipe recipe) {
        return postTestCase(recipe.getTestCase(), false);
    }

    @Override
    public List<Execution> getExecutions() {
        return Lists.newArrayList(executionsMap.values());
    }

    @Override
    public void addExecutionListener(ExecutionListener listener) {
        executionListeners.add(listener);
    }

    @Override
    public void removeExecutionListener(ExecutionListener listener) {
        executionListeners.remove(listener);
    }

    private Execution postTestCase(TestCase testCase, boolean async) {

        try {
            return postRecipe(getObjectMapper().writeValueAsString(testCase), async);
        } catch (Exception e) {
            notifyErrorOccurred(e);
            throw new RecipeExecutionException("Failed to execute Test recipe", e);
        }
    }

    private Execution postRecipe(String jsonText, boolean async) {
        String executionId = UUID.randomUUID().toString();
        try {
            TestCaseStruct testCaseStruct = getObjectMapper().readValue(jsonText, TestCaseStruct.class);
            WsdlProject project = recipeParser.parse(testCaseStruct);
            StringToObjectMap properties = new StringToObjectMap();

            if (async) {
                prepareAsyncExecution(executionId, project, properties);
            }

            WsdlProjectRunner projectRunner = new WsdlProjectRunner(project, properties);

            SoapUIRecipeExecution execution = new SoapUIRecipeExecution(executionId, projectRunner);
            executionsMap.put(executionId, execution);
            projectRunner.start(async);
            if (!async) {
                notifyExecutionFinished(execution.getCurrentReport());
            }
            return execution;
        } catch (Exception e) {
            notifyErrorOccurred(e);
            throw new RecipeExecutionException("Failed to execute Test recipe", e);
        }
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        }
        return objectMapper;
    }

    private void prepareAsyncExecution(String executionId, WsdlProject project, StringToObjectMap properties) {
        project.addProjectRunListener(new ProjectRunListenerAdapter() {
            @Override
            public void beforeRun(ProjectRunner projectRunner, ProjectRunContext runContext) {
                String executionId = (String) runContext.getProperty(LOCAL_CLIENT_EXECUTION_ID);
                if (executionId != null) {
                    notifyExecutionStarted(executionsMap.get(executionId).getCurrentReport());
                }
            }

            @Override
            public void afterRun(ProjectRunner projectRunner, ProjectRunContext runContext) {
                String executionId = (String) runContext.getProperty(LOCAL_CLIENT_EXECUTION_ID);
                if (executionId != null) {
                    notifyExecutionFinished(executionsMap.get(executionId).getCurrentReport());
                }
            }
        });
        properties.put(LOCAL_CLIENT_EXECUTION_ID, executionId);
    }


    private void notifyExecutionStarted(ProjectResultReport projectResultReport) {
        if (projectResultReport != null) {
            for (ExecutionListener executionListener : executionListeners) {
                executionListener.executionStarted(projectResultReport);
            }
        }
    }

    private void notifyErrorOccurred(Exception e) {
        for (ExecutionListener executionListener : executionListeners) {
            executionListener.errorOccurred(e);
        }
    }

    private void notifyExecutionFinished(ProjectResultReport projectResultReport) {
        for (ExecutionListener executionListener : executionListeners) {
            executionListener.executionFinished(projectResultReport);
        }
    }
}
