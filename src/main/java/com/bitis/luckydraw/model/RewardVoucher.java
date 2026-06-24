package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reward_voucher")
public class RewardVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_giai_thuong", nullable = false)
    private String maGiaiThuong;

    @Column(name = "ma_khach_hang", nullable = false)
    private String maKhachHang;

    @Column(name = "ma_voucher", nullable = false, length = 255)
    private String maVoucher;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ma_store_phat_hanh", nullable = false)
    private String maStorePhatHanh;

    @Column(name = "ma_store_doi_thuong")
    private String maStoreDoiThuong;

    @Column(name = "thoi_gian_tao", nullable = false, updatable = false, insertable = false)
    private LocalDateTime thoiGianTao;

    @Column(name = "thoi_gian_doi")
    private LocalDateTime thoiGianDoi;
}
