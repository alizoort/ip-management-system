package com.processautomation.camundapoc.models;

public class GetCurrentTaskScreenResponseDTO {
    public String currentTaskScreen;
    public String taskId;
    public GetCurrentTaskScreenResponseDTO(String currentTaskScreen, String taskId){
        this.currentTaskScreen=currentTaskScreen;
        this.taskId=taskId;
    }
}
