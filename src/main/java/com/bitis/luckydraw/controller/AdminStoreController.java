package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/stores")
public class AdminStoreController {

    private final StoreRepository storeRepository;

    public AdminStoreController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public String listStores(Model model) {
        model.addAttribute("stores", storeRepository.findAll());
        return "admin/store-list";
    }

    @PostMapping("/save")
    public String saveStore(@RequestParam(required = false) Long storeId,
                            @RequestParam String tenCuaHang,
                            @RequestParam String maStore,
                            @RequestParam String diaChiStore,
                            @RequestParam Integer trangThai) {
        Store store;
        if (storeId != null) {
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
