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

    @Query(value = "SELECT * FROM vw_staff_list WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(username) LIKE '%' + LOWER(:keyword) + '%' OR LOWER(tenNhanVien) LIKE '%' + LOWER(:keyword) + '%') AND " +
           "(:roleId IS NULL OR :roleId = 'all' OR roleId = :roleId) AND " +
           "(:storeMa IS NULL OR :storeMa = 'all' OR maStore = :storeMa)", nativeQuery = true)
    List<StaffListDto> filterStaffList(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("roleId") String roleId,
            @org.springframework.data.repository.query.Param("storeMa") String storeMa);
}
