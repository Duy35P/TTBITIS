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

    @Column(name = "ma_chien_dich", unique = true)
    private String maChienDich;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ten_chien_dich", nullable = false, length = 255)
    private String tenChienDich;

    @org.springframework.format.annotation.DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @org.springframework.format.annotation.DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "duong_dan_slug", length = 255, unique = true)
    private String duongDanSlug;

    @Column(name = "hinh_anh_url", columnDefinition = "VARCHAR(MAX)")
    private String hinhAnhUrl;

    @Column(name = "cauhinh_theme_json", columnDefinition = "NVARCHAR(MAX)")
    private String cauhinhThemeJson;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "doc_quyen")
    private Boolean docQuyen;

    @Column(name = "han_token_ngay", nullable = false)
    private Integer hanTokenNgay = 30;

    @Transient
    public String getDisplayStatus() {
        if (trangThai == null || trangThai != 1) return "Tạm ngưng";
        LocalDateTime now = LocalDateTime.now();
        if (ngayKetThuc != null && now.isAfter(ngayKetThuc)) return "Kết thúc";
        if (ngayBatDau != null && now.isBefore(ngayBatDau)) return "Chưa bắt đầu";
        return "Đang diễn ra";
    }

    @Transient
    public String getDisplayStatusColor() {
        String status = getDisplayStatus();
        switch (status) {
            case "Tạm ngưng": return "bg-red";
            case "Kết thúc": return "bg-gray";
            case "Chưa bắt đầu": return "bg-blue";
            default: return "bg-green";
        }
    }
}
