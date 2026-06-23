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
    
    @Query(value = "SELECT s.store_id AS storeId, STRING_AGG(c.ten_chien_dich, ', ') AS campaigns " +
                   "FROM store s " +
                   "LEFT JOIN campaign_store cs ON s.store_id = cs.id_cua_hang " +
                   "LEFT JOIN campaign c ON cs.id_chien_dich = c.campaign_id " +
                   "GROUP BY s.store_id", nativeQuery = true)
    java.util.List<StoreCampaignProjection> getStoreCampaigns();
}
