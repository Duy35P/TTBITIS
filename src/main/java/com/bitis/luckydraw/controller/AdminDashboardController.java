package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.CustomerRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import com.bitis.luckydraw.repository.TurnTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/admin", "/admin/"})
public class AdminDashboardController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private TurnTransactionRepository turnTransactionRepository;

    @GetMapping
    public String index(Model model) {
        long totalCustomers = customerRepository.count();
        long totalStores = storeRepository.count();
        long runningCampaigns = campaignRepository.countByTrangThai(1);
        long totalSpins = turnTransactionRepository.sumSpins();

        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalStores", totalStores);
        model.addAttribute("runningCampaigns", runningCampaigns);
        model.addAttribute("totalSpins", totalSpins);

        return "admin/index";
    }
}
