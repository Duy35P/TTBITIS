package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import com.bitis.luckydraw.dto.StoreCampaignProjection;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByMaStore(String maStore);
    java.util.List<Store> findByTrangThai(Integer trangThai);
    
    @Query(value = "SELECT s.id AS storeId, " +
                   "STRING_AGG(CASE WHEN c.trang_thai = 1 THEN c.ten_chien_dich ELSE NULL END, ', ') AS activeCampaigns, " +
                   "STRING_AGG(CASE WHEN c.trang_thai = 0 THEN c.ten_chien_dich ELSE NULL END, ', ') AS pendingCampaigns " +
                   "FROM store s " +
                   "LEFT JOIN campaign_store cs ON s.ma_store = cs.ma_store " +
                   "LEFT JOIN campaign c ON cs.ma_chien_dich = c.ma_chien_dich " +
                   "GROUP BY s.id", nativeQuery = true)
    java.util.List<StoreCampaignProjection> getStoreCampaigns();
}
