package com.bitis.luckydraw.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/customer")
public class CustomerViewController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "receipt", required = false) String receipt, HttpSession session) {
        // Ponytail: Nếu quét QR mới thì tự động logout tài khoản cũ cho dễ test!
        if (receipt != null && !receipt.trim().isEmpty()) {
            session.invalidate();
        } else if (session.getAttribute("CUSTOMER_ID") != null) {
            return "redirect:/customer/index";
        }
        return "customer/login";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.bitis.luckydraw.repository.CampaignRepository campaignRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.bitis.luckydraw.repository.CustomerTurnRepository customerTurnRepository;

    @GetMapping("/index")
    public String indexPage(HttpSession session, Model model, jakarta.servlet.http.HttpServletResponse response) {
        // Chống cache để khi user nhấn Back từ màn hình quay, trang index sẽ tự tải lại số lượt quay mới nhất!
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        if (session.getAttribute("CUSTOMER_ID") == null) {
            return "redirect:/customer/login";
        }
        
        String maKhachHang = (String) session.getAttribute("CUSTOMER_MA");
        
        // 1. Lấy danh sách Campaign đang active (trạng thái = 1)
        java.util.List<com.bitis.luckydraw.model.Campaign> allCampaigns = campaignRepository.findAll();
        java.util.List<com.bitis.luckydraw.model.Campaign> activeCampaigns = new java.util.ArrayList<>();
        for (com.bitis.luckydraw.model.Campaign c : allCampaigns) {
            if (c.getTrangThai() != null && c.getTrangThai() == 1) {
                activeCampaigns.add(c);
            }
        }
        
        // 2. Lấy số lượt quay của user
        java.util.List<com.bitis.luckydraw.model.CustomerTurn> userTurns = customerTurnRepository.findByMaKhachHang(maKhachHang);
        java.util.Map<String, Integer> turnMap = new java.util.HashMap<>();
        for (com.bitis.luckydraw.model.CustomerTurn ct : userTurns) {
            turnMap.put(ct.getMaChienDich(), ct.getLuotConLai() != null ? ct.getLuotConLai() : 0);
        }

        // 3. Chuẩn bị data hiển thị (Sắp xếp campaign có lượt lên đầu)
        java.util.List<java.util.Map<String, Object>> displayCampaigns = new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> otherCampaigns = new java.util.ArrayList<>();

        for (com.bitis.luckydraw.model.Campaign camp : activeCampaigns) {
            java.util.Map<String, Object> cData = new java.util.HashMap<>();
            cData.put("id", camp.getId());
            cData.put("maChienDich", camp.getMaChienDich());
            cData.put("tenChienDich", camp.getTenChienDich());
            cData.put("moTa", camp.getMoTa());
            cData.put("duongDanSlug", camp.getDuongDanSlug());
            int turns = turnMap.getOrDefault(camp.getMaChienDich(), 0);
            cData.put("turns", turns);
            
            if (turns > 0) {
                displayCampaigns.add(cData);
            } else {
                otherCampaigns.add(cData);
            }
        }
        
        // Gộp lại: có lượt lên trước
        displayCampaigns.addAll(otherCampaigns);
        
        model.addAttribute("campaigns", displayCampaigns);
        return "customer/index";
    }



    // Các URL tạm để render html, sau này sẽ có data thật
    @GetMapping("/spin")
    public String spinPage(@RequestParam(value = "campaign", required = false) String campaign, HttpSession session, Model model) {
        if (session.getAttribute("CUSTOMER_ID") == null) {
            return "redirect:/customer/login";
        }
        
        if (campaign == null || campaign.trim().isEmpty()) {
            return "redirect:/customer/index";
        }
        
        model.addAttribute("campaignId", campaign);
        return "customer/spin-wheel-demo";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.bitis.luckydraw.repository.RewardVoucherRepository rewardVoucherRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.bitis.luckydraw.repository.CustomerRepository customerRepository;

    @GetMapping("/account")
    public String accountPage(HttpSession session, Model model) {
        if (session.getAttribute("CUSTOMER_ID") == null) {
            return "redirect:/customer/login";
        }
        String maKhachHang = (String) session.getAttribute("CUSTOMER_MA");
        customerRepository.findByMaKhachHang(maKhachHang).ifPresent(c -> {
            model.addAttribute("customer", c);
        });
        return "customer/account";
    }

    @GetMapping("/history")
    public String historyPage(HttpSession session, Model model) {
        if (session.getAttribute("CUSTOMER_ID") == null) {
            return "redirect:/customer/login";
        }
        String maKhachHang = (String) session.getAttribute("CUSTOMER_MA");
        java.util.List<com.bitis.luckydraw.dto.RewardVoucherListDto> history = rewardVoucherRepository.getRewardVoucherListByKhachHang(maKhachHang);
        model.addAttribute("history", history);
        return "customer/history";
    }

    @GetMapping("/prize-detail")
    public String prizeDetailPage(@RequestParam(value = "code", required = false) String code, HttpSession session, Model model) {
        if (session.getAttribute("CUSTOMER_ID") == null) {
            return "redirect:/customer/login";
        }
        if (code == null || code.trim().isEmpty()) {
            return "redirect:/customer/history";
        }
        
        String maKhachHang = (String) session.getAttribute("CUSTOMER_MA");
        com.bitis.luckydraw.model.RewardVoucher voucher = rewardVoucherRepository.findByMaVoucher(code).orElse(null);
        
        if (voucher == null || !maKhachHang.equals(voucher.getMaKhachHang())) {
            return "redirect:/customer/history"; // Bảo mật, không cho xem của người khác
        }
        
        // Gọi lên view vw_reward_voucher_list để lấy Tên Giải
        java.util.List<com.bitis.luckydraw.dto.RewardVoucherListDto> list = rewardVoucherRepository.getRewardVoucherListByKhachHang(maKhachHang);
        com.bitis.luckydraw.dto.RewardVoucherListDto dto = list.stream().filter(v -> v.getMaVoucher().equals(code)).findFirst().orElse(null);
        
        model.addAttribute("voucher", dto); // Truyền DTO vì nó có tenGiai
        return "customer/prize-detail";
    }
}
