package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.repository.CampaignRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/campaigns")
public class AdminCampaignController {

    private final CampaignRepository campaignRepository;

    public AdminCampaignController(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @GetMapping
    public String listCampaigns(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        return "admin/campaign-list";
    }

    @PostMapping("/save")
    public String saveCampaign(@ModelAttribute Campaign campaign) {
        campaignRepository.save(campaign);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/toggle-status")
    public String toggleStatus(@RequestParam Long campaignId, @RequestParam Integer status) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            campaign.setTrangThai(status);
            campaignRepository.save(campaign);
        });
        return "redirect:/admin/campaigns";
    }
}
