package com.bitis.luckydraw.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.bitis.luckydraw.dto.RewardVoucherListDto;
import com.bitis.luckydraw.model.RewardVoucher;
import com.bitis.luckydraw.repository.RewardVoucherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/redemption")
public class AdminRedemptionController {

    private final RewardVoucherRepository rewardVoucherRepository;
    private final com.bitis.luckydraw.repository.StorePrizeInventoryRepository storePrizeInventoryRepository;

    public AdminRedemptionController(RewardVoucherRepository rewardVoucherRepository, 
                                     com.bitis.luckydraw.repository.StorePrizeInventoryRepository storePrizeInventoryRepository) {
        this.rewardVoucherRepository = rewardVoucherRepository;
        this.storePrizeInventoryRepository = storePrizeInventoryRepository;
    }

    @GetMapping
    public String viewRedemptionPage(Model model) {
        List<RewardVoucherListDto> recentHistory = rewardVoucherRepository.getRecentRedemptions();
        model.addAttribute("recentHistory", recentHistory);
        return "admin/staff-redemption";
    }

    @GetMapping("/check")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('QL_KIEMTRAMA')")
    @ResponseBody
    public ResponseEntity<?> checkVoucher(@RequestParam("code") String code) {
        Optional<RewardVoucherListDto> opt = rewardVoucherRepository.getRewardVoucherDetail(code);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã không tồn tại trong hệ thống."));
        }
        
        RewardVoucherListDto detail = opt.get();
        
        // Fetch tồn kho cửa hàng
        String maStore = detail.getMaStorePhatHanh();
        Integer tonKho = null;
        Optional<com.bitis.luckydraw.model.StorePrizeInventory> invOpt = storePrizeInventoryRepository.findByMaStoreAndMaGiaiThuong(maStore, detail.getMaGiaiThuong());
        if (invOpt.isPresent()) {
            tonKho = invOpt.get().getTonKho();
        }

        if (detail.getTrangThai() != null && detail.getTrangThai() == 1) {
            String timeDoi = "không rõ";
            if (detail.getThoiGianDoi() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                timeDoi = detail.getThoiGianDoi().format(formatter);
            }
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Mã đã được sử dụng / Đổi quà rồi vào lúc " + timeDoi + " tại " + (detail.getTenStoreDoiThuong() != null ? detail.getTenStoreDoiThuong() : detail.getMaStoreDoiThuong()),
                "detail", detail,
                "tonKho", tonKho != null ? tonKho : 0
            ));
        }

        return ResponseEntity.ok(Map.of(
            "detail", detail,
            "tonKho", tonKho != null ? tonKho : 0
        ));
    }
}
