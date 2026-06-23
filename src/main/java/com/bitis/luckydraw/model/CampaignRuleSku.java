package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "campaign_rule_sku")
public class CampaignRuleSku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_sku_id", nullable = false)
    private Long ruleSkuId;

    @Column(name = "id_chien_dich", nullable = false)
    private Long idChienDich;

    @Column(name = "ma_sku", nullable = false, length = 100)
    private String maSku;

    @Column(name = "so_luot_thuong", nullable = false)
    private Integer soLuotThuong;
}
