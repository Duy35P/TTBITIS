package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.PrizeListDto;
import com.bitis.luckydraw.dto.RewardVoucherListDto;
import com.bitis.luckydraw.repository.PrizeRepository;
import com.bitis.luckydraw.repository.RewardVoucherRepository;
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

    @GetMapping
    public String index(Model model, 
                        @RequestParam(name = "prize", required = false) String prizeMa,
                        @RequestParam(name = "status", required = false) String statusStr) {
        
        List<PrizeListDto> allPrizes = prizeRepository.getPrizeList();
        // Ponytail: Chỉ hiển thị các giải thật trên bộ lọc thả xuống
        List<PrizeListDto> prizes = allPrizes.stream()
                .filter(p -> Boolean.TRUE.equals(p.getLaGiaiThuong()))
                .collect(Collectors.toList());
                
        List<RewardVoucherListDto> vouchers = rewardVoucherRepository.getRewardVoucherList();

        // Apply filters
        if (prizeMa != null && !prizeMa.isEmpty() && !prizeMa.equals("all")) {
            vouchers = vouchers.stream()
                    .filter(v -> prizeMa.equals(v.getMaGiaiThuong()))
                    .collect(Collectors.toList());
        }
        if (statusStr != null && !statusStr.isEmpty() && !statusStr.equals("all")) {
            int status = statusStr.equals("issued") ? 0 : 1;
            vouchers = vouchers.stream()
                    .filter(v -> v.getTrangThai() != null && v.getTrangThai() == status)
                    .collect(Collectors.toList());
        }

        model.addAttribute("prizes", prizes);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("selectedPrize", prizeMa != null ? prizeMa : "all");
        model.addAttribute("selectedStatus", statusStr != null ? statusStr : "all");

        return "admin/voucher-list";
    }
}
