package com.processautomation.camundapoc.models;

import jakarta.persistence.*;

@Entity
public class BPMNUsersAssociation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name="user_id")
    User user;
    @ManyToOne
    @JoinColumn(name="bpmn_instance_id")
    BPMNProcessInstance bpmnProcessInstance;
    public Long getId() {
        return id;
    }
    public User getUser(){
        return this.user;
    }
    public BPMNUsersAssociation(){

    }
    public BPMNUsersAssociation(User user,BPMNProcessInstance bpmnProcessInstance){
        this.user=user;
        this.bpmnProcessInstance =bpmnProcessInstance;
    }
    public BPMNProcessInstance getBpmnProcessInstance() {
        return bpmnProcessInstance;
    }

    public void setBpmnProcessInstance(BPMNProcessInstance bpmnProcessInstance) {
        this.bpmnProcessInstance = bpmnProcessInstance;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
