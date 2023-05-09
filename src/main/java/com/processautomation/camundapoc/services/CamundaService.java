package com.processautomation.camundapoc.services;

import com.processautomation.camundapoc.models.CompleteBpmnInstanceRequest;
import com.processautomation.camundapoc.payload.InitiationRequest;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class CamundaService {
    @Autowired
    private ZeebeClientLifecycle client;
    public void completeBpmnInstance(CompleteBpmnInstanceRequest completeRequest){

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
