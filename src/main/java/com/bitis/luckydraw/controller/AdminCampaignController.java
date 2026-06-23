package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.repository.CampaignRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.model.CampaignStore;
import com.bitis.luckydraw.repository.StoreRepository;
import com.bitis.luckydraw.repository.CampaignStoreRepository;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/campaigns")
public class AdminCampaignController {

    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final CampaignStoreRepository campaignStoreRepository;

    public AdminCampaignController(CampaignRepository campaignRepository, StoreRepository storeRepository, CampaignStoreRepository campaignStoreRepository) {
        this.campaignRepository = campaignRepository;
        this.storeRepository = storeRepository;
        this.campaignStoreRepository = campaignStoreRepository;
    }

    @GetMapping
    public String listCampaigns(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        return "admin/campaign-list";
    }

    @PostMapping("/save")
    public String saveCampaign(@ModelAttribute Campaign campaign, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            campaignRepository.save(campaign);
        } catch (Exception e) {
            String errorMsg = e.getCause() != null && e.getCause().getCause() != null 
                ? e.getCause().getCause().getMessage() 
                : e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
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
    
    @GetMapping("/{campaignId}/stores")
    public String getStoreAllocationModal(@PathVariable Long campaignId, Model model) {
        // Find all active stores
        List<Store> activeStores = storeRepository.findByTrangThai(1);
        
        // Find currently assigned stores
        List<Long> assignedStoreIds = campaignStoreRepository.findByIdChienDich(campaignId)
                .stream()
                .map(CampaignStore::getIdCuaHang)
                .collect(Collectors.toList());
                
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("activeStores", activeStores);
        model.addAttribute("assignedStoreIds", assignedStoreIds);
        
        // Return a fragment HTML to be injected into the modal body
        return "admin/fragments/store-allocation-fragment :: content";
    }
    
    @PostMapping("/{campaignId}/stores/save")
    public String saveStoreAllocation(@PathVariable Long campaignId, @RequestParam(required = false) List<Long> storeIds) {
        // Delete old assignments
        campaignStoreRepository.deleteByIdChienDich(campaignId);
        
        // Save new ones
        if (storeIds != null) {
            for (Long storeId : storeIds) {
                CampaignStore mapping = new CampaignStore();
                mapping.setIdChienDich(campaignId);
                mapping.setIdCuaHang(storeId);
                campaignStoreRepository.save(mapping);
            }
        }
        
        return "redirect:/admin/campaigns";
    }
}
