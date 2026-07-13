package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Prize;
import com.bitis.luckydraw.repository.PrizeRepository;
import com.bitis.luckydraw.service.CustomerSpinService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/api/spin")
public class CustomerSpinApiController {

    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private CustomerSpinService customerSpinService;

    @Autowired
    private com.bitis.luckydraw.repository.CampaignRepository campaignRepository;

    @GetMapping("/config")
    public ResponseEntity<?> getConfig(@RequestParam("campaign") String maChienDich) {
        List<Prize> prizes = prizeRepository.findByMaChienDich(maChienDich);
        if (prizes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Chiến dịch không có giải thưởng"));
        }

        com.bitis.luckydraw.model.Campaign campaign = campaignRepository.findByMaChienDich(maChienDich).orElse(null);
        Map<String, Object> config = new HashMap<>();
        
        if (campaign != null && campaign.getCauhinhThemeJson() != null && !campaign.getCauhinhThemeJson().trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                config = mapper.readValue(campaign.getCauhinhThemeJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                
                // Map bgBot -> bgBottom để tương thích FE
                if (config.containsKey("bgBot") && !config.containsKey("bgBottom")) {
                    config.put("bgBottom", config.get("bgBot"));
                }
                
                // Nối cờ isPrize vào prizes từ DB
                List<Map<String, Object>> themePrizes = (List<Map<String, Object>>) config.get("prizes");
                if (themePrizes != null) {
                    for (Map<String, Object> tp : themePrizes) {
                        String pCode = (String) tp.get("code");
                        for (Prize p : prizes) {
                            if (p.getMaGiaiThuong().equals(pCode)) {
                                tp.put("isPrize", p.getLaGiaiThuong());
                                tp.put("id", p.getMaGiaiThuong());
                                break;
                            }
                        }
                    }
                }
                return ResponseEntity.ok(config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Fallback
        config.put("title", "VÒNG QUAY MAY MẮN");
        config.put("centerText1", "QUAY");
        config.put("centerText2", "THƯỞNG");
        config.put("buttonText", "Quay ngay");
        
        List<Map<String, Object>> prizeList = new ArrayList<>();
        String[] colors = {"#1e4a8a", "#d6ecfa"};
        for (int i = 0; i < prizes.size(); i++) {
            Prize p = prizes.get(i);
            Map<String, Object> prizeMap = new HashMap<>();
            prizeMap.put("id", p.getMaGiaiThuong());
            prizeMap.put("name", p.getTenGiai());
            prizeMap.put("color", colors[i % colors.length]);
            prizeMap.put("isPrize", p.getLaGiaiThuong());
            prizeMap.put("icon", p.getLaGiaiThuong() ? "🎁" : "😢");
            prizeList.add(prizeMap);
        }
        config.put("prizes", prizeList);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/play")
    public ResponseEntity<?> playSpin(@RequestParam("campaign") String maChienDich, HttpSession session) {
        String maKhachHang = (String) session.getAttribute("CUSTOMER_MA");
        if (maKhachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập"));
        }

        try {
            // Đọc maStore từ Session (đã lưu lúc user quét QR hóa đơn). Nếu không có (ví dụ chơi qua public link) thì mặc định là STORE_ONLINE.
            String maStore = (String) session.getAttribute("CURRENT_STORE");
            if (maStore == null) {
                maStore = "STORE_ONLINE"; 
            }
            
            com.bitis.luckydraw.model.Campaign campaign = campaignRepository.findByMaChienDich(maChienDich).orElse(null);
            if (campaign == null || campaign.getTrangThai() != 1 || 
                campaign.getNgayBatDau().isAfter(java.time.LocalDateTime.now()) || 
                campaign.getNgayKetThuc().isBefore(java.time.LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Chiến dịch đã kết thúc hoặc không khả dụng."));
            }
            
            Prize wonPrize = customerSpinService.playSpin(maKhachHang, maChienDich, maStore);

            // Lấy index từ cấu hình theme JSON để khớp với FE
            int prizeIndex = -1;
            
            if (campaign != null && campaign.getCauhinhThemeJson() != null && !campaign.getCauhinhThemeJson().trim().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> config = mapper.readValue(campaign.getCauhinhThemeJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                    List<Map<String, Object>> themePrizes = (List<Map<String, Object>>) config.get("prizes");
                    if (themePrizes != null) {
                        for (int i = 0; i < themePrizes.size(); i++) {
                            String pCode = (String) themePrizes.get(i).get("code");
                            if (wonPrize.getMaGiaiThuong().equals(pCode)) {
                                prizeIndex = i;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Fallback nếu không tìm thấy trong JSON
            if (prizeIndex == -1) {
                List<Prize> prizes = prizeRepository.findByMaChienDich(maChienDich);
                for (int i = 0; i < prizes.size(); i++) {
                    if (prizes.get(i).getMaGiaiThuong().equals(wonPrize.getMaGiaiThuong())) {
                        prizeIndex = i;
                        break;
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("prizeIndex", prizeIndex);
            response.put("prizeName", wonPrize.getTenGiai());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            // Lấy message lỗi gốc từ SQL Server (ném ra bằng lệnh THROW)
            String errorMsg = e.getMessage();
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause.getMessage() != null && cause.getMessage().contains("Khách hàng không còn đủ lượt quay")) {
                    errorMsg = "Bạn đã hết lượt quay của chương trình này rồi!";
                    break;
                }
                if (cause.getMessage() != null && cause.getMessage().contains("Vượt quá giới hạn trúng thưởng")) {
                    errorMsg = "Bạn đã đạt giới hạn trúng thưởng của chương trình này!";
                    break;
                }
                cause = cause.getCause();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMsg));
        }
    }
}
