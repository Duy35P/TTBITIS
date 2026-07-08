package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.ManagerStoreAssignment;
import com.bitis.luckydraw.model.ManagerStoreAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagerStoreAssignmentRepository extends JpaRepository<ManagerStoreAssignment, ManagerStoreAssignmentId> {
    List<ManagerStoreAssignment> findByUsername(String username);
    void deleteByUsername(String username);
}
