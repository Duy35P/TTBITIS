package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.PrizeListDto;
import com.bitis.luckydraw.dto.RewardVoucherListDto;
import com.bitis.luckydraw.repository.PrizeRepository;
import com.bitis.luckydraw.repository.RewardVoucherRepository;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    @Autowired
    private RewardVoucherRepository rewardVoucherRepository;

    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private StoreRepository storeRepository;

    @GetMapping
    public String index(Model model, 
                        @RequestParam(name = "prize", required = false) String prizeMa,
                        @RequestParam(name = "status", required = false) String statusStr,
                        @RequestParam(name = "campaign", required = false) String campaignMa,
                        @RequestParam(name = "store", required = false) String storeMa) {
        
        List<PrizeListDto> allPrizes = prizeRepository.getPrizeList();
        
        List<PrizeListDto> prizes = allPrizes.stream()
                .filter(p -> Boolean.TRUE.equals(p.getLaGiaiThuong()))
                .collect(Collectors.toList());

        String pPrize = (prizeMa != null && !prizeMa.isEmpty()) ? prizeMa : "all";
        String pCampaign = (campaignMa != null && !campaignMa.isEmpty()) ? campaignMa : "all";
        String pStore = (storeMa != null && !storeMa.isEmpty()) ? storeMa : "all";
        int pStatus = -1;
        String pStatusStr = "all";
        
        if (statusStr != null && !statusStr.isEmpty() && !statusStr.equals("all")) {
            pStatus = statusStr.equals("issued") ? 0 : 1;
            pStatusStr = statusStr;
        }
                
        List<RewardVoucherListDto> vouchers = rewardVoucherRepository.filterRewardVoucherList(pPrize, pStatus, pCampaign, pStore);

        model.addAttribute("campaigns", campaignRepository.findAll());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("prizes", prizes);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("selectedPrize", pPrize);
        model.addAttribute("selectedStatus", pStatusStr);
        model.addAttribute("selectedCampaign", pCampaign);
        model.addAttribute("selectedStore", pStore);

        return "admin/voucher-list";
    }
}
