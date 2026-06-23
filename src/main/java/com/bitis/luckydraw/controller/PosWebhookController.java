package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.InvoiceRequestDTO;
import com.bitis.luckydraw.model.GameAccessToken;
import com.bitis.luckydraw.service.TurnManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pos")
public class PosWebhookController {

    private final TurnManagementService turnManagementService;

    public PosWebhookController(TurnManagementService turnManagementService) {
        this.turnManagementService = turnManagementService;
    }

    @PostMapping("/invoice")
    public ResponseEntity<?> receiveInvoice(@RequestBody InvoiceRequestDTO request) {
        try {
            GameAccessToken token = turnManagementService.processInvoice(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Hóa đơn đã được xử lý thành công.");
            
            if (token != null) {
                response.put("token", token.getToken());
                response.put("turnsGranted", token.getSoLuongLuotThuong());
                // Link để mở giao diện web game (giả định)
                response.put("playUrl", "http://localhost:8080/play?token=" + token.getToken());
            } else {
                response.put("message", "Hóa đơn đã được ghi nhận nhưng không đủ điều kiện cấp lượt hoặc là hóa đơn trả hàng (Delta <= 0).");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
