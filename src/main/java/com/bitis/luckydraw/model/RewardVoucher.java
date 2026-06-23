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
    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "id_giai_thuong", nullable = false)
    private Long idGiaiThuong;

    @Column(name = "id_khach_hang", nullable = false)
    private Long idKhachHang;

    @Column(name = "ma_voucher", nullable = false, length = 255)
    private String maVoucher;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "id_cua_hang_phat_hanh", nullable = false)
    private Long idCuaHangPhatHanh;

    @Column(name = "id_cua_hang_doi_thuong")
    private Long idCuaHangDoiThuong;

    @Column(name = "thoi_gian_tao", nullable = false, updatable = false, insertable = false)
    private LocalDateTime thoiGianTao;

    @Column(name = "thoi_gian_doi")
    private LocalDateTime thoiGianDoi;
}
