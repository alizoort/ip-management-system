package com.processautomation.camundapoc.services;

import com.processautomation.camundapoc.models.*;
import com.processautomation.camundapoc.payload.InitiationRequest;
import com.processautomation.camundapoc.repositories.BPMNUsersAssociationRepository;
import com.processautomation.camundapoc.repositories.UserRepository;
import io.camunda.operate.exception.OperateException;
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
    HashMap<String,String> taskScreenAssociation = new HashMap<String,String>(){{
        put("FillPersonnelInfo","https://pabudtywntpghop.form.io/ipsimpleform");
        put("FillNativeInformation","https://pabudtywntpghop.form.io/nativeinformation");
    }};
    @Autowired
    private Environment env;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BPMNUsersAssociationRepository bpmnUsersAssociationRepository;
    public Task findCurrentTask(CamundaTaskListClient taskListClient, String bpmnProcessId)  throws TaskListException{
        var processInstanceAssociationWrapper = new Object(){
            BPMNUsersAssociation bpmnUsersAssociation;
            Task currentTask;
            User loggedInUser;
        };
        String loggedInUsername = ((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        userRepository.findByUsername(loggedInUsername).ifPresent(userInstance -> processInstanceAssociationWrapper.loggedInUser=userInstance);
        List<BPMNUsersAssociation> bpmnUsersAssociationList = bpmnUsersAssociationRepository.findBPMNUsersAssociationBy(processInstanceAssociationWrapper.loggedInUser.getUsername());
        bpmnUsersAssociationList.stream().filter(association -> association.getBpmnProcessInstance().getBpmnProcessId().equals(bpmnProcessId)).findFirst().ifPresent(association-> processInstanceAssociationWrapper.bpmnUsersAssociation=association);
        TaskSearch ts = new TaskSearch().setProcessInstanceId(processInstanceAssociationWrapper.bpmnUsersAssociation.getBpmnProcessInstance().getProcessInstanceKey().toString()).setState(TaskState.CREATED);
        TaskList tasksFromInstance = taskListClient.getTasks(ts);
        tasksFromInstance.getItems().stream().findFirst().ifPresent((Task availableTask)-> processInstanceAssociationWrapper.currentTask=availableTask);
        return processInstanceAssociationWrapper.currentTask;
    }
    public GetCurrentTaskScreenResponseDTO retrieveCurrentTaskScreenUrl(GetCurrentTaskScreenUrlDTO currentScreen) throws TaskListException {
        SelfManagedAuthentication selfManagedAuthentication = new SelfManagedAuthentication(env.getProperty("ipManagementSystem.app.clientId"),env.getProperty("ipManagementSystem.app.clientSecret"));
        CamundaTaskListClient tasklistClient = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8082").shouldReturnVariables().authentication(selfManagedAuthentication).build();
        Task currentTask = findCurrentTask(tasklistClient,currentScreen.bpmnProcessId);
        return new GetCurrentTaskScreenResponseDTO(taskScreenAssociation.get(currentTask.getTaskDefinitionId()), currentTask.getTaskDefinitionId());
    }
    //Create a method that takes bpmn and username and gives -> the current camunda screen (This should be put insde redirection variable)
    public void completeBpmnInstance(CompleteBpmnInstanceRequest completeRequest) throws TaskListException , OperateException {
        SelfManagedAuthentication selfManagedAuthentication = new SelfManagedAuthentication(env.getProperty("ipManagementSystem.app.clientId"),env.getProperty("ipManagementSystem.app.clientSecret"));
        CamundaTaskListClient tasklistClient = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8082").shouldReturnVariables().authentication(selfManagedAuthentication).build();
        Task currentTask = findCurrentTask(tasklistClient,completeRequest.bpmnProcessId);
        HashMap variablesMap = new HashMap<>();
        completeRequest.variables.forEach( variableInstance -> {
            variablesMap.put(variableInstance.get("name"),variableInstance.get("value"));
        });
        tasklistClient.completeTask(currentTask.getId(),variablesMap);
    }

    public ProcessInstanceEvent initiateBpmnInstance(InitiationRequest initiationRequest){

     return     client.newCreateInstanceCommand()
                .bpmnProcessId(initiationRequest.bpmnProcessId)
                .latestVersion()
                .send().join();
    }
}
