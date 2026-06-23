package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.CampaignStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignStoreRepository extends JpaRepository<CampaignStore, Long> {

    List<CampaignStore> findByIdChienDich(Long idChienDich);
    List<CampaignStore> findByIdCuaHang(Long idCuaHang);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByIdChienDich(Long idChienDich);
}
