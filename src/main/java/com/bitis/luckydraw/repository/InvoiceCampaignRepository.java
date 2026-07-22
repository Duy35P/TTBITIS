package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.InvoiceCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceCampaignRepository extends JpaRepository<InvoiceCampaign, Long> {
    List<InvoiceCampaign> findByMaChienDich(String maChienDich);
    List<InvoiceCampaign> findByMaStore(String maStore);
    List<InvoiceCampaign> findByMaChienDichAndMaStore(String maChienDich, String maStore);
    List<InvoiceCampaign> findByMaHoaDon(String maHoaDon);
    List<InvoiceCampaign> findByMaHoaDonIn(List<String> maHoaDons);
}
