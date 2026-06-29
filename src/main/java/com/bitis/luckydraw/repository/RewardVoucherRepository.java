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
}
