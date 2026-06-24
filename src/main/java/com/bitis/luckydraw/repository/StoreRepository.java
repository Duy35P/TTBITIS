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
    
    @Query(value = "SELECT * FROM vw_store_campaigns", nativeQuery = true)
    java.util.List<StoreCampaignProjection> getStoreCampaigns();
}
