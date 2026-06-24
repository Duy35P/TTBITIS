package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "campaign_rule_payment")
public class CampaignRulePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_chien_dich", nullable = false)
    private String maChienDich;

    @Column(name = "phuong_thuc_thanh_toan", nullable = false, length = 100)
    private String phuongThucThanhToan;

    @Column(name = "so_luot_thuong", nullable = false)
    private Integer soLuotThuong;
}
