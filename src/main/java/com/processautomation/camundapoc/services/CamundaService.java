package com.processautomation.camundapoc.services;

import com.processautomation.camundapoc.models.CompleteBpmnInstanceRequest;
import com.processautomation.camundapoc.payload.InitiationRequest;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.dto.ProcessInstance;
import io.camunda.operate.dto.ProcessInstanceState;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.ProcessInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SelfManagedAuthentication;
import io.camunda.tasklist.dto.*;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class CamundaService {
    @Autowired
    private ZeebeClientLifecycle client;
    @Autowired
    private Environment env;
    public void completeBpmnInstance(CompleteBpmnInstanceRequest completeRequest) throws TaskListException , OperateException {
        SelfManagedAuthentication selfManagedAuthentication = new SelfManagedAuthentication(env.getProperty("ipManagementSystem.app.clientId"),env.getProperty("ipManagementSystem.app.clientSecret"));
        CamundaTaskListClient tasklistClient = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8082").shouldReturnVariables().authentication(selfManagedAuthentication).build();
        CamundaOperateClient operateClient = new CamundaOperateClient.Builder().authentication(new io.camunda.operate.auth.SelfManagedAuthentication().clientId(env.getProperty("ipManagementSystem.app.clientId")).clientSecret(env.getProperty("ipManagementSystem.app.clientSecret")))
                .operateUrl("http://localhost:8081/").build();
        ProcessInstanceFilter instanceFilter = new ProcessInstanceFilter.Builder().bpmnProcessId("IPManagementBusinessProcess").state(ProcessInstanceState.ACTIVE).build();
        SearchQuery instanceQuery = new SearchQuery.Builder().filter(instanceFilter).size(20).build();
        List<ProcessInstance> list = operateClient.searchProcessInstances(instanceQuery);
        ProcessInstance processInstance = list.get(0);
        TaskSearch ts = new TaskSearch().setState(TaskState.CREATED).setWithVariables(true).setProcessDefinitionId(processInstance.getProcessDefinitionKey().toString());
        TaskList tasksFromInstance = tasklistClient.getTasks(ts);

        for(Task taskElement : tasksFromInstance){
            for(Variable variable : taskElement.getVariables()){
                if(completeRequest.taskId.equals(taskElement.getTaskDefinitionId()) && variable.getName().equals("assignedBusinessUser") && ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername().equals(variable.getValue())){
                    HashMap variablesMap = new HashMap<>();
                    completeRequest.variables.forEach( variableInstance -> {
                        variablesMap.put(variableInstance.get("name"),variableInstance.get("value"));
                    });
                    tasklistClient.completeTask(taskElement.getId(),variablesMap);
                }
            }
        }
    }

    public ProcessInstanceEvent initiateBpmnInstance(InitiationRequest initiationRequest){


     return     client.newCreateInstanceCommand()
                .bpmnProcessId(initiationRequest.bpmnProcessId)
                .latestVersion()
                .variables( new HashMap<Object,Object>(){{
                                put("assignedBusinessUser",((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
                                put("stakeholders",initiationRequest.stakeholders);
                            }}
                ).send().join();
    }
}
