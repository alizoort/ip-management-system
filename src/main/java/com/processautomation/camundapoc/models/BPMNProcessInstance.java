package com.processautomation.camundapoc.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="bpmn_instances")
public class BPMNProcessInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank
    @Size(max=120)
    private String bpmnProcessId;

    private Long processInstanceKey;
    @OneToMany(mappedBy = "bpmnProcessInstance")
    Set<BPMNUsersAssociation> bpmnUsersAssociations;

    public Set<BPMNUsersAssociation> getBpmnUsersAssociations() {
        return bpmnUsersAssociations;
    }

    public void setBpmnUsersAssociations(Set<BPMNUsersAssociation> bpmnUsersAssociations) {
        this.bpmnUsersAssociations = bpmnUsersAssociations;
    }

    public BPMNProcessInstance(){

    }
    public BPMNProcessInstance(String bpmnProcessId,Long processInstanceKey){
        this.bpmnProcessId=bpmnProcessId;
        this.processInstanceKey=processInstanceKey;
    }
    public Long getProcessInstanceKey(){
        return this.processInstanceKey;
    }
    public String getBpmnProcessId(){
        return this.bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId){
        this.bpmnProcessId=bpmnProcessId;
    }
    public void setProcessInstanceKey(Long processInstanceKey){
        this.processInstanceKey=processInstanceKey;
    }


}
