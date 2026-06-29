package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.bitis.luckydraw.dto.StaffListDto;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUsername(String username);

    @Query(value = "SELECT * FROM vw_staff_list", nativeQuery = true)
    List<StaffListDto> getStaffList();
}
