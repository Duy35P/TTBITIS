package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.RewardVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.bitis.luckydraw.dto.RewardVoucherListDto;

@Repository
public interface RewardVoucherRepository extends JpaRepository<RewardVoucher, Long> {
    Optional<RewardVoucher> findByMaVoucher(String maVoucher);
    List<RewardVoucher> findByMaKhachHang(String maKhachHang);
    long countByMaGiaiThuongAndMaKhachHang(String maGiaiThuong, String maKhachHang);

    @Query(value = "SELECT * FROM vw_reward_voucher_list", nativeQuery = true)
    List<RewardVoucherListDto> getRewardVoucherList();

    @Query(value = "SELECT * FROM vw_reward_voucher_list WHERE " +
           "(:prizeMa = 'all' OR maGiaiThuong = :prizeMa) AND " +
           "(:status = -1 OR trangThai = :status) AND " +
           "(:campaignMa = 'all' OR maChienDich = :campaignMa) AND " +
           "(:storeMa = 'all' OR maStorePhatHanh = :storeMa OR maStoreDoiThuong = :storeMa) " +
           "ORDER BY thoiGianTao DESC", nativeQuery = true)
    List<RewardVoucherListDto> filterRewardVoucherList(
            @org.springframework.data.repository.query.Param("prizeMa") String prizeMa, 
            @org.springframework.data.repository.query.Param("status") Integer status, 
            @org.springframework.data.repository.query.Param("campaignMa") String campaignMa, 
            @org.springframework.data.repository.query.Param("storeMa") String storeMa);

    @Query(value = "SELECT * FROM vw_reward_voucher_list WHERE maKhachHang = :maKhachHang ORDER BY thoiGianTao DESC", nativeQuery = true)
    List<RewardVoucherListDto> getRewardVoucherListByKhachHang(@org.springframework.data.repository.query.Param("maKhachHang") String maKhachHang);

    @Query(value = "SELECT * FROM vw_reward_voucher_list WHERE maVoucher = :maVoucher", nativeQuery = true)
    Optional<RewardVoucherListDto> getRewardVoucherDetail(@org.springframework.data.repository.query.Param("maVoucher") String maVoucher);

    @Query(value = "SELECT * FROM vw_reward_voucher_list ORDER BY thoiGianTao DESC OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY", nativeQuery = true)
    List<RewardVoucherListDto> getRecentWins();

    @Query(value = "SELECT * FROM vw_reward_voucher_list WHERE trangThai = 1 ORDER BY thoiGianDoi DESC OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY", nativeQuery = true)
    List<RewardVoucherListDto> getRecentRedemptions();
}
