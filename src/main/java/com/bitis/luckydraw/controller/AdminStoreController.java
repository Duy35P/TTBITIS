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

@Controller
@RequestMapping("/admin/stores")
public class AdminStoreController {

    private final StoreRepository storeRepository;

    public AdminStoreController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public String listStores(Model model) {
        List<Store> stores = storeRepository.findAll();
        
        // Map from Native Query
        Map<Long, String> storeCampaignsMap = new HashMap<>();
        for (StoreCampaignProjection p : storeRepository.getStoreCampaigns()) {
            storeCampaignsMap.put(p.getStoreId(), p.getCampaigns());
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
