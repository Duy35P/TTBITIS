package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test/pos-simulator")
public class AdminPosSimulatorController {

    private final StoreRepository storeRepository;

    public AdminPosSimulatorController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public String showSimulator(Model model) {
        model.addAttribute("stores", storeRepository.findByTrangThai(1));
        return "test/pos-simulator";
    }
}
