package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.PrizeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeCodeRepository extends JpaRepository<PrizeCode, Long> {
    long countByMaGiaiThuongAndIsUsed(String maGiaiThuong, Boolean isUsed);
    boolean existsByCode(String code);
}
