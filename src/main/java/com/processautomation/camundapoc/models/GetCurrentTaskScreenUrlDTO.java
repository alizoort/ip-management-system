package com.processautomation.camundapoc.models;

public class GetCurrentTaskScreenUrlDTO {
    public String bpmnProcessId;
    public String username;
    public GetCurrentTaskScreenUrlDTO(String bpmnProcessId, String username){
        this.username=username;
        this.bpmnProcessId=bpmnProcessId;
    }
}
