package com.processautomation.camundapoc.models;

import java.util.List;
import java.util.Map;

public class CompleteBpmnInstanceRequest {
    public List<Map<String,String>> variables;
    public String bpmnProcessId;
    public String taskId;
    CompleteBpmnInstanceRequest(List<Map<String,String>> variables,String bpmnProcessInstance,String taskId){
        this.variables= variables;
        this.bpmnProcessId=bpmnProcessInstance;
        this.taskId= taskId;
    }
}
