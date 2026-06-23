package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "campaign_store")
public class CampaignStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_store_id")
    private Long campaignStoreId;

    @Column(name = "id_chien_dich", nullable = false)
    private Long idChienDich;

    @Column(name = "id_cua_hang", nullable = false)
    private Long idCuaHang;
}
