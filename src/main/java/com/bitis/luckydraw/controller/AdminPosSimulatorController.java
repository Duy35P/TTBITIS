package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/pos-simulator")
public class AdminPosSimulatorController {

    private final StoreRepository storeRepository;

    public AdminPosSimulatorController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public String showSimulator(Model model) {
        List<Store> activeStores = storeRepository.findByTrangThai(1);
        model.addAttribute("stores", activeStores);
        return "admin/pos-simulator";
    }
}
