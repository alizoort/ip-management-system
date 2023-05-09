package com.processautomation.camundapoc.models;

import java.util.List;
import java.util.Map;

public class CompleteBpmnInstanceRequest {
    List<Map<String,String>> variables;
    String bpmnProcessId;
    CompleteBpmnInstanceRequest(List<Map<String,String>> variables,String bpmnProcessInstance){
        this.variables= variables;
        this.bpmnProcessId=bpmnProcessInstance;
    }
}
