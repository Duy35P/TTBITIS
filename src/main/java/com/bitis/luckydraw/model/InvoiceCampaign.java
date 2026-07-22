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
@Table(name = "invoice_campaign")
public class InvoiceCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_hoa_don", nullable = false)
    private String maHoaDon;

    @Column(name = "ma_chien_dich", nullable = false)
    private String maChienDich;

    @Column(name = "ma_store", nullable = false)
    private String maStore;

    @Column(name = "tong_tien", nullable = false)
    private Double tongTien;

    @Column(name = "so_luot_cap", nullable = false)
    private Integer soLuotCap;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();
}
