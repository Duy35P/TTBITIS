package com.bitis.luckydraw.service;

import com.bitis.luckydraw.model.Prize;
import com.bitis.luckydraw.model.StorePrizeInventory;
import com.bitis.luckydraw.repository.PrizeRepository;
import com.bitis.luckydraw.repository.StorePrizeInventoryRepository;
import com.bitis.luckydraw.repository.CampaignStoreRepository;
import com.bitis.luckydraw.model.CampaignStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
public class PrizeService {

    private final PrizeRepository prizeRepository;
    private final StorePrizeInventoryRepository storePrizeInventoryRepository;
    private final CampaignStoreRepository campaignStoreRepository;

    public PrizeService(PrizeRepository prizeRepository, 
                        StorePrizeInventoryRepository storePrizeInventoryRepository,
                        CampaignStoreRepository campaignStoreRepository) {
        this.prizeRepository = prizeRepository;
        this.storePrizeInventoryRepository = storePrizeInventoryRepository;
        this.campaignStoreRepository = campaignStoreRepository;
    }

    @Transactional
    public void allocatePrizeToStore(String maStore, String maGiaiThuong, int quantity) {
        if (quantity <= 0) return; // ponytail: ignore dummy/infinite prizes
        storePrizeInventoryRepository.allocatePrizeToStore(maStore, maGiaiThuong, quantity);
    }

    @Transactional
    public void updateAllocation(String maStore, String maGiaiThuong, int newTongLuongCap) {
        storePrizeInventoryRepository.updateStorePrizeInventory(maStore, maGiaiThuong, newTongLuongCap);
    }

    @Transactional
    public void reclaimUnredeemedVouchers(String maChienDich) {
        prizeRepository.reclaimUnredeemedVouchers(maChienDich);
    }
}
