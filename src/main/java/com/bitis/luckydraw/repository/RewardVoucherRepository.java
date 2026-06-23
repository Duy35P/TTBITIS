package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.RewardVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardVoucherRepository extends JpaRepository<RewardVoucher, Long> {
    Optional<RewardVoucher> findByMaVoucher(String maVoucher);
    List<RewardVoucher> findByIdKhachHang(Long idKhachHang);
    long countByIdGiaiThuongAndIdKhachHang(Long idGiaiThuong, Long idKhachHang);
}
