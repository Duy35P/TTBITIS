package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.model.CampaignStore;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.CampaignStoreRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/stores")
public class AdminStoreController {

    private final StoreRepository storeRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignStoreRepository campaignStoreRepository;

    public AdminStoreController(StoreRepository storeRepository, CampaignRepository campaignRepository, CampaignStoreRepository campaignStoreRepository) {
        this.storeRepository = storeRepository;
        this.campaignRepository = campaignRepository;
        this.campaignStoreRepository = campaignStoreRepository;
    }

    @GetMapping
    public String listStores(Model model) {
        List<Store> stores = storeRepository.findAll();
        
        // Lấy tất cả mapping campaign_store
        List<CampaignStore> allMappings = campaignStoreRepository.findAll();
        
        // Lấy tất cả campaign active/draft (hoặc lấy hết)
        List<Campaign> allCampaigns = campaignRepository.findAll();
        Map<Long, Campaign> campaignMap = allCampaigns.stream()
                .collect(Collectors.toMap(Campaign::getCampaignId, c -> c));

        // Group campaigns by storeId
        Map<Long, List<Campaign>> storeCampaignsMap = new HashMap<>();
        for (Store s : stores) {
            List<Campaign> campaignsForStore = allMappings.stream()
                    .filter(m -> m.getIdCuaHang().equals(s.getStoreId()))
                    .map(m -> campaignMap.get(m.getIdChienDich()))
                    .filter(c -> c != null)
                    .collect(Collectors.toList());
            storeCampaignsMap.put(s.getStoreId(), campaignsForStore);
        }

        model.addAttribute("stores", stores);
        model.addAttribute("storeCampaignsMap", storeCampaignsMap);
        return "admin/store-list";
    }

    @PostMapping("/save")
    public String saveStore(@RequestParam(name = "storeId", required = false) String storeIdStr,
                            @RequestParam String tenCuaHang,
                            @RequestParam String maStore,
                            @RequestParam String diaChiStore,
                            @RequestParam Integer trangThai) {
        Store store;
        if (storeIdStr != null && !storeIdStr.trim().isEmpty()) {
            Long storeId = Long.parseLong(storeIdStr);
            store = storeRepository.findById(storeId).orElse(new Store());
        } else {
            store = new Store();
        }
        
        store.setTenCuaHang(tenCuaHang);
        store.setMaStore(maStore);
        store.setDiaChiStore(diaChiStore);
        store.setTrangThai(trangThai);
        
        storeRepository.save(store);
        
        return "redirect:/admin/stores";
    }
}
