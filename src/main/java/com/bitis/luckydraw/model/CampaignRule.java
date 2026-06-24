package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "campaign_rule")
public class CampaignRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_chien_dich", nullable = false)
    private String maChienDich;

    @Column(name = "gia_tri_don_hang_toi_thieu")
    private Double giaTriDonHangToiThieu;
}
