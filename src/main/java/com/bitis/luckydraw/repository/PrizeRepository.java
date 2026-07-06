package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.bitis.luckydraw.dto.PrizeListDto;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
    Optional<Prize> findByMaGiaiThuong(String maGiaiThuong);
    List<Prize> findByMaChienDich(String maChienDich);

    @Query(value = "SELECT * FROM vw_prize_list", nativeQuery = true)
    List<PrizeListDto> getPrizeList();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE prize SET xac_suat = 100 - COALESCE((SELECT SUM(xac_suat) FROM prize p2 WHERE p2.ma_chien_dich = prize.ma_chien_dich AND p2.la_giai_thuong = 1), 0) WHERE ma_chien_dich = :maChienDich AND la_giai_thuong = 0", nativeQuery = true)
    void recalibrateDummyPrize(@org.springframework.data.repository.query.Param("maChienDich") String maChienDich);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "EXEC sp_ReclaimUnredeemedVouchers @maChienDich = :maChienDich", nativeQuery = true)
    void reclaimUnredeemedVouchers(@org.springframework.data.repository.query.Param("maChienDich") String maChienDich);
}
