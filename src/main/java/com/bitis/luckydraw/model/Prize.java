package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prize")
public class Prize {

    @Column(name = "ma_giai_thuong", unique = true)
    private String maGiaiThuong;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_chien_dich", nullable = false)
    private String maChienDich;

    @Column(name = "ten_giai", nullable = false, length = 255)
    private String tenGiai;

    @Column(name = "loai_giai", nullable = false)
    private Integer loaiGiai;

    @Column(name = "la_giai_thuong", nullable = false)
    private Boolean laGiaiThuong;

    @Column(name = "xac_suat", nullable = false)
    private Double xacSuat;

    @Column(name = "ton_kho_toan_he_thong", nullable = false)
    private Integer tonKhoToanHeThong;

    @Column(name = "gioi_han_trung_moi_customer")
    private Integer gioiHanTrungMoiCustomer;
}
