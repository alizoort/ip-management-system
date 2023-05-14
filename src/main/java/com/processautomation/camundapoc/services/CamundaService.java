package com.processautomation.camundapoc.services;

import com.processautomation.camundapoc.models.CompleteBpmnInstanceRequest;
import com.processautomation.camundapoc.models.GetCurrentTaskScreenResponseDTO;
import com.processautomation.camundapoc.models.GetCurrentTaskScreenUrlDTO;
import com.processautomation.camundapoc.payload.InitiationRequest;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.dto.ProcessInstance;
import io.camunda.operate.dto.ProcessInstanceState;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.ProcessInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.VariableFilter;
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

@Service
public class CamundaService {
    @Autowired
    private ZeebeClientLifecycle client;
    HashMap screenTaskAssociation = new HashMap<Object,Object>(){{
        put("FillPersonnelInfo","https://pabudtywntpghop.form.io/nativeinformation");
        put("FillNativeInformation","https://pabudtywntpghop.form.io/nativeinformation");
    }
    };
    HashMap<String,String> taskScreenAssociation = new HashMap<String,String>(){{
        put("https://pabudtywntpghop.form.io/ipsimpleform","FillPersonnelInfo");
        put("https://pabudtywntpghop.form.io/nativeinformation","FillNativeInformation");
    }};
    @Autowired
    private Environment env;
    public GetCurrentTaskScreenResponseDTO retrieveCurrentTaskScreenUrl(GetCurrentTaskScreenUrlDTO currentScreen) throws TaskListException , OperateException{
        SelfManagedAuthentication selfManagedAuthentication = new SelfManagedAuthentication(env.getProperty("ipManagementSystem.app.clientId"),env.getProperty("ipManagementSystem.app.clientSecret"));
        CamundaTaskListClient tasklistClient = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8082").shouldReturnVariables().authentication(selfManagedAuthentication).build();
        CamundaOperateClient operateClient = new CamundaOperateClient.Builder().authentication(new io.camunda.operate.auth.SelfManagedAuthentication().clientId(env.getProperty("ipManagementSystem.app.clientId")).clientSecret(env.getProperty("ipManagementSystem.app.clientSecret")))
                .operateUrl("http://localhost:8081/").build();
        ProcessInstanceFilter instanceFilter = new ProcessInstanceFilter.Builder().bpmnProcessId(currentScreen.bpmnProcessId).state(ProcessInstanceState.ACTIVE).build();
        SearchQuery instanceQuery = new SearchQuery.Builder().filter(instanceFilter).size(20).build();
        List<ProcessInstance> list = operateClient.searchProcessInstances(instanceQuery);
        for(ProcessInstance processElement : list){
            VariableFilter variableFilter = new VariableFilter.Builder().processInstanceKey(processElement.getKey()).build();
            SearchQuery varQuery = new SearchQuery.Builder().filter(variableFilter).size(20).build();
            List<io.camunda.operate.dto.Variable> variables = operateClient.searchVariables(varQuery);
            io.camunda.operate.dto.Variable searchedVariable = new io.camunda.operate.dto.Variable();
            searchedVariable.setName("assignedBusinessUser");
            searchedVariable.setValue(currentScreen.username);
            if(variables.stream().filter( item-> item.getName().equals("assignedBusinessUser") && item.getValue().substring(1,item.getValue().length()-1).equals(currentScreen.username)).toList().size()>0){
                var wrapper = new Object(){ String currentUrl = "";};
                variables.stream().filter(variable-> variable.getName().equals("currentScreenUrl")).findFirst().ifPresent(variable -> wrapper.currentUrl =variable.getValue());
                return new GetCurrentTaskScreenResponseDTO(wrapper.currentUrl,taskScreenAssociation.get(wrapper.currentUrl.substring(1,wrapper.currentUrl.length()-1)));
            }
        }
        throw new TaskListException();
    }
    //Create a method that takes bpmn and username and gives -> the current camunda screen (This should be put insde redirection variable)
    public void completeBpmnInstance(CompleteBpmnInstanceRequest completeRequest) throws TaskListException , OperateException {
        SelfManagedAuthentication selfManagedAuthentication = new SelfManagedAuthentication(env.getProperty("ipManagementSystem.app.clientId"),env.getProperty("ipManagementSystem.app.clientSecret"));
        CamundaTaskListClient tasklistClient = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8082").shouldReturnVariables().authentication(selfManagedAuthentication).build();
        CamundaOperateClient operateClient = new CamundaOperateClient.Builder().authentication(new io.camunda.operate.auth.SelfManagedAuthentication().clientId(env.getProperty("ipManagementSystem.app.clientId")).clientSecret(env.getProperty("ipManagementSystem.app.clientSecret")))
                .operateUrl("http://localhost:8081/").build();
        ProcessInstanceFilter instanceFilter = new ProcessInstanceFilter.Builder().bpmnProcessId(completeRequest.bpmnProcessId).state(ProcessInstanceState.ACTIVE).build();
        SearchQuery instanceQuery = new SearchQuery.Builder().filter(instanceFilter).size(20).build();
        //key = currentTask -> val = url of the next screen
        List<ProcessInstance> list = operateClient.searchProcessInstances(instanceQuery);
        ProcessInstance processInstance = list.get(0);
        TaskSearch ts = new TaskSearch().setState(TaskState.CREATED).setWithVariables(true).setProcessDefinitionId(processInstance.getProcessDefinitionKey().toString());
        TaskList tasksFromInstance = tasklistClient.getTasks(ts);
        VariableFilter variableFilter = new VariableFilter.Builder().processInstanceKey(processInstance.getKey()).build();
        SearchQuery varQuery = new SearchQuery.Builder().filter(variableFilter).size(20).build();
        List<io.camunda.operate.dto.Variable> variables = operateClient.searchVariables(varQuery);
        for(Task taskElement : tasksFromInstance){
            for(Variable variable : taskElement.getVariables()){
                if(completeRequest.taskId.equals(taskElement.getTaskDefinitionId()) && variable.getName().equals("assignedBusinessUser") && ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername().equals(variable.getValue())){
                    HashMap variablesMap = new HashMap<>();
                    completeRequest.variables.forEach( variableInstance -> {
                        variablesMap.put(variableInstance.get("name"),variableInstance.get("value"));
                    });
                    variablesMap.put("currentScreenUrl",screenTaskAssociation.get(completeRequest.taskId));
                    tasklistClient.completeTask(taskElement.getId(),variablesMap);
                    return;
                }
            }
        }
        throw new TaskListException();
    }

    public ProcessInstanceEvent initiateBpmnInstance(InitiationRequest initiationRequest){
        HashMap screenTaskAssociation = new HashMap<Object,Object>(){{
            put("FillPersonnelInfo","https://pabudtywntpghop.form.io/ipsimpleform");
            put("FillNativeInformation","https://pabudtywntpghop.form.io/nativeinformation");
        }
        };
     return     client.newCreateInstanceCommand()
                .bpmnProcessId(initiationRequest.bpmnProcessId)
                .latestVersion()
                .variables( new HashMap<Object,Object>(){{
                                put("assignedBusinessUser",((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
                                put("stakeholders",initiationRequest.stakeholders);
                                put("taskScreenAssociation",screenTaskAssociation);
                                put("currentScreenUrl",screenTaskAssociation.get("FillPersonnelInfo"));
                            }}
                ).send().join();
    }
}
