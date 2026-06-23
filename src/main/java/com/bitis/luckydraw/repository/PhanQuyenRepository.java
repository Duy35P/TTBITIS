package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.PhanQuyen;
import com.bitis.luckydraw.model.PhanQuyenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhanQuyenRepository extends JpaRepository<PhanQuyen, PhanQuyenId> {
    List<PhanQuyen> findByIdRoleId(String roleId);
}
