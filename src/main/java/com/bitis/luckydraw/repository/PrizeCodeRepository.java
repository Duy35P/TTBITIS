package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.PrizeCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PrizeCodeRepository extends JpaRepository<PrizeCode, Long> {
    long countByMaGiaiThuongAndIsUsed(String maGiaiThuong, Boolean isUsed);
    long countByMaGiaiThuong(String maGiaiThuong);
    boolean existsByCode(String code);
    
    Page<PrizeCode> findByMaGiaiThuong(String maGiaiThuong, Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PrizeCode p WHERE p.maGiaiThuong = :maGiaiThuong AND p.isUsed = :isUsed")
    void deleteByMaGiaiThuongAndIsUsed(String maGiaiThuong, Boolean isUsed);
}
