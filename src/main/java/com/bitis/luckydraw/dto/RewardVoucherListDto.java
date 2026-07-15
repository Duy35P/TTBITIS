package com.bitis.luckydraw.dto;

import java.time.LocalDateTime;

public interface RewardVoucherListDto {
    Long getId();
    String getMaVoucher();
    String getMaGiaiThuong();
    String getTenGiai();
    Integer getLoaiGiai();
    String getMaKhachHang();
    String getTenKhach();
    String getPhone();
    String getMaStorePhatHanh();
    String getTenStorePhatHanh();
    LocalDateTime getThoiGianTao();
    String getMaStoreDoiThuong();
    String getTenStoreDoiThuong();
    LocalDateTime getThoiGianDoi();
    Integer getTrangThai();
    String getMaChienDich();
    String getTenChienDich();
}
