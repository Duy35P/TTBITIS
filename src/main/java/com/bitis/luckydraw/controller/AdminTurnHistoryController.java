package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.TurnHistoryDto;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.TurnTransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin/turns")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('QL_LUOTQUAY')")
public class AdminTurnHistoryController {

    private final TurnTransactionRepository turnTransactionRepository;
    private final CampaignRepository campaignRepository;

    public AdminTurnHistoryController(TurnTransactionRepository turnTransactionRepository, CampaignRepository campaignRepository) {
        this.turnTransactionRepository = turnTransactionRepository;
        this.campaignRepository = campaignRepository;
    }

    @GetMapping
    public String viewTurnHistory(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String loai,
            @RequestParam(required = false) String maChienDich,
            Model model) {

        Integer filterLoai = null;
        if (loai != null && !loai.isEmpty() && !loai.equals("all")) {
            filterLoai = Integer.parseInt(loai);
        }

        String filterCampaign = null;
        if (maChienDich != null && !maChienDich.isEmpty() && !maChienDich.equals("all")) {
            filterCampaign = maChienDich;
        }

        List<TurnHistoryDto> history = turnTransactionRepository.findTurnHistory(
                phone, filterLoai, filterCampaign
        );

        model.addAttribute("history", history);
        model.addAttribute("campaigns", campaignRepository.findAll());
        model.addAttribute("phone", phone);
        model.addAttribute("loai", loai == null ? "all" : loai);
        model.addAttribute("maChienDich", maChienDich == null ? "all" : maChienDich);

        return "admin/turn-history";
    }
}
