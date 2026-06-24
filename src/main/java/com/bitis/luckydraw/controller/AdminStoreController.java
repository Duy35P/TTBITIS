package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bitis.luckydraw.dto.StoreCampaignProjection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.model.CampaignStore;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.CampaignStoreRepository;

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
        List<Campaign> allCampaigns = campaignRepository.findAll();
        List<CampaignStore> allMappings = campaignStoreRepository.findAll();
        
        Map<String, Campaign> campaignMap = allCampaigns.stream().collect(Collectors.toMap(Campaign::getMaChienDich, c -> c));
        
        Map<Long, List<Campaign>> activeCampaignsMap = new HashMap<>();
        Map<Long, List<Campaign>> pendingCampaignsMap = new HashMap<>();
        
        for (Store store : stores) {
            List<Campaign> storeCampaigns = allMappings.stream()
                .filter(m -> m.getMaStore().equals(store.getMaStore()))
                .map(m -> campaignMap.get(m.getMaChienDich()))
                .filter(c -> c != null)
                .collect(Collectors.toList());
                
            activeCampaignsMap.put(store.getId(), storeCampaigns.stream().filter(c -> c.getTrangThai() == 1).collect(Collectors.toList()));
            pendingCampaignsMap.put(store.getId(), storeCampaigns.stream().filter(c -> c.getTrangThai() == 0).collect(Collectors.toList()));
        }

        model.addAttribute("stores", stores);
        model.addAttribute("activeCampaignsMap", activeCampaignsMap);
        model.addAttribute("pendingCampaignsMap", pendingCampaignsMap);
        return "admin/store-list";
    }

    @PostMapping("/save")
    public String saveStore(@RequestParam(name = "storeId", required = false) String storeIdStr,
                            @RequestParam String tenCuaHang,
                            @RequestParam String maStore,
                            @RequestParam String diaChiStore,
                            @RequestParam(required = false, defaultValue = "0") Integer trangThai) {
        Store store;
        if (storeIdStr != null && !storeIdStr.trim().isEmpty()) {
            Long storeId = Long.parseLong(storeIdStr);
            store = storeRepository.findById(storeId).orElse(new Store());
            store.setTrangThai(trangThai);
        } else {
            store = new Store();
            store.setTrangThai(0); // Luôn luôn tạm ngưng khi mới tạo
        }
        
        store.setTenCuaHang(tenCuaHang);
        store.setMaStore(maStore);
        store.setDiaChiStore(diaChiStore);
        
        storeRepository.save(store);
        
        return "redirect:/admin/stores";
    }
}
