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
    @Column(name = "rule_payment_id")
    private Long rulePaymentId;

    @Column(name = "id_chien_dich", nullable = false)
    private Long idChienDich;

    @Column(name = "phuong_thuc_thanh_toan", nullable = false, length = 100)
    private String phuongThucThanhToan;

    @Column(name = "so_luot_thuong", nullable = false)
    private Integer soLuotThuong;
}
