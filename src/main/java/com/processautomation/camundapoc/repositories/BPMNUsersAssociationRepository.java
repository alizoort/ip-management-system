package com.processautomation.camundapoc.repositories;

import com.processautomation.camundapoc.models.BPMNUsersAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BPMNUsersAssociationRepository extends JpaRepository<BPMNUsersAssociation,Long> {
   @Query("SELECT association FROM BPMNUsersAssociation association WHERE association.user.username = ?1")
   List<BPMNUsersAssociation> findBPMNUsersAssociationBy(String username);
}
