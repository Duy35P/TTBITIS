package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.SystemAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {
    List<SystemAuditLog> findByStaffIdOrderByCreatedAtDesc(Long staffId);
    List<SystemAuditLog> findByTargetTableOrderByCreatedAtDesc(String targetTable);
    List<SystemAuditLog> findByTargetTableAndTargetRecordIdOrderByIdDesc(String targetTable, String targetRecordId);
}
