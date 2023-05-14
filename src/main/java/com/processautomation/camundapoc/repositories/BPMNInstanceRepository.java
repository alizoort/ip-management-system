package com.processautomation.camundapoc.repositories;

import com.processautomation.camundapoc.models.BPMNProcessInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BPMNInstanceRepository extends JpaRepository<BPMNProcessInstance,Long> {
      Optional<BPMNProcessInstance> findBPMNProcessInstanceByBpmnProcessId(String bpmnProcessId);
      Boolean existsBPMNProcessInstanceByProcessInstanceKey(Long processInstanceKey);
}
