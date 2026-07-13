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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Autowired
    private com.bitis.luckydraw.repository.RewardVoucherRepository rewardVoucherRepository;

    @Autowired
    private com.bitis.luckydraw.repository.StorePrizeInventoryRepository storePrizeInventoryRepository;

    @GetMapping
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("QL_TONGQUAN"))) {
            // Không có quyền xem tổng quan -> redirect trang khác
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("QL_CHIENDICH") || a.getAuthority().equals("ROLE_GAME_MAKER"))) {
                return "redirect:/admin/campaigns";
            }
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("QL_CUAHANG"))) {
                return "redirect:/admin/stores";
            }
            return "redirect:/admin/profile"; // Trang cá nhân/cài đặt (luôn có)
        }

        long totalCustomers = customerRepository.count();
        long totalStores = storeRepository.count();
        long runningCampaigns = campaignRepository.countByTrangThai(1);
        long totalSpins = turnTransactionRepository.sumSpins();

        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalStores", totalStores);
        model.addAttribute("runningCampaigns", runningCampaigns);
        model.addAttribute("totalSpins", totalSpins);

        // Chart 1: Lượt quay
        java.util.List<Object[]> spinsData = turnTransactionRepository.getSpinsPerDay();
        model.addAttribute("spinsData", spinsData);

        // Chart 2: Tỷ lệ phát thưởng
        Long daPhat = storePrizeInventoryRepository.sumDaPhat();
        Long tonKho = storePrizeInventoryRepository.sumTonKho();
        model.addAttribute("totalDaPhat", daPhat != null ? daPhat : 0);
        model.addAttribute("totalTonKho", tonKho != null ? tonKho : 0);

        // Table: Lịch sử trúng thưởng
        model.addAttribute("recentWins", rewardVoucherRepository.getRecentWins());

        return "admin/index";
    }
}
