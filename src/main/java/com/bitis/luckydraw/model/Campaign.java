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
@Table(name = "campaign")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "ten_chien_dich", nullable = false, length = 255)
    private String tenChienDich;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "tong_luot_du_kien")
    private Integer tongLuotDuKien;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "duong_dan_slug", length = 255, unique = true)
    private String duongDanSlug;

    @Column(name = "hinh_anh_url", length = 500)
    private String hinhAnhUrl;

    @Column(name = "cauhinh_theme_json", columnDefinition = "NVARCHAR(MAX)")
    private String cauhinhThemeJson;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;
}
