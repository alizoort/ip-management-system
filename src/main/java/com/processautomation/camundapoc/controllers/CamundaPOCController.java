package com.processautomation.camundapoc.controllers;

import com.processautomation.camundapoc.CamundaPocApplication;
import com.processautomation.camundapoc.models.CompleteBpmnInstanceRequest;
import com.processautomation.camundapoc.models.GetCurrentTaskScreenResponseDTO;
import com.processautomation.camundapoc.models.GetCurrentTaskScreenUrlDTO;
import com.processautomation.camundapoc.payload.InitiationRequest;
import com.processautomation.camundapoc.payload.MessageResponse;
import com.processautomation.camundapoc.services.CamundaService;
import io.camunda.operate.exception.OperateException;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@CrossOrigin(origins = "http://localhost:4200")
@RestController @RequestMapping("ip-management-system")
public class CamundaPOCController {
    private final static Logger LOG = LoggerFactory.getLogger(CamundaPocApplication.class);
    @Autowired
    CamundaService camundaService;
    @PatchMapping("/completeProcessInstance")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<?> completeProcessInstance(@Valid @RequestBody CompleteBpmnInstanceRequest completeBpmnInstanceRequest) throws TaskListException, OperateException {
        camundaService.completeBpmnInstance(completeBpmnInstanceRequest);
        return ResponseEntity.ok(new MessageResponse("Tested Sucessfuly"));
    }
    @PostMapping("/initiateUserProcess")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<?> initiateEnrollment(@Valid @RequestBody InitiationRequest initiationRequest){
        ProcessInstanceEvent event = camundaService.initiateBpmnInstance(initiationRequest);
                LOG.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
                event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
           return ResponseEntity.ok(new MessageResponse("Process Has Been Instantiated"));
    }
    @PostMapping("/currentScreenUrl")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> currentCamundaScreen(@Valid @RequestBody GetCurrentTaskScreenUrlDTO currentScreenUrl) throws TaskListException, OperateException {
       GetCurrentTaskScreenResponseDTO currentTaskScreen = camundaService.retrieveCurrentTaskScreenUrl(currentScreenUrl);
        return ResponseEntity.ok(new HashMap<Object,Object>(){{
            put("currentTaskScreen",currentTaskScreen.currentTaskScreen);
            put("currentTaskId",currentTaskScreen.taskId);
        }});
    }

}
