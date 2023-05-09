package com.processautomation.camundapoc.controllers;

import com.processautomation.camundapoc.CamundaPocApplication;
import com.processautomation.camundapoc.payload.InitiationRequest;
import com.processautomation.camundapoc.payload.MessageResponse;
import com.processautomation.camundapoc.services.CamundaService;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController @RequestMapping("ip-management-system")
public class CamundaPOCController {
    private final static Logger LOG = LoggerFactory.getLogger(CamundaPocApplication.class);
    @Autowired
    CamundaService camundaService;
    @GetMapping("user")
    public String helloUser(){
        return "Hello User";
    }
    @GetMapping("admin")
    public String helloAdmin(){
        return "Hello Admin";
    }
    @PostMapping("/initiateUserProcess")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<?> initiateEnrollment(@Valid @RequestBody InitiationRequest initiationRequest){
        ProcessInstanceEvent event = camundaService.initiateBpmnInstance(initiationRequest);
                LOG.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
                event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
           return ResponseEntity.ok(new MessageResponse("Process Has Been Instantiated"));
    }

}
