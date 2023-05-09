package com.processautomation.camundapoc.payload;

import java.util.List;

public class InitiationRequest {
    public String bpmnProcessId;
    public List<String>  stakeholders;
    InitiationRequest(String bpmnProcessId,List<String> stakeholders){
        this.bpmnProcessId=bpmnProcessId;
        this.stakeholders= stakeholders;
    }
}
