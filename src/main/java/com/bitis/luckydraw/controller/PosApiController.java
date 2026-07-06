package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.PosSyncRequest;
import com.bitis.luckydraw.dto.PosSyncResponse;
import com.bitis.luckydraw.service.PosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pos")
public class PosApiController {

    private final PosService posService;
    private final com.bitis.luckydraw.repository.RewardVoucherRepository rewardVoucherRepository;

    public PosApiController(PosService posService, com.bitis.luckydraw.repository.RewardVoucherRepository rewardVoucherRepository) {
        this.posService = posService;
        this.rewardVoucherRepository = rewardVoucherRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<PosSyncResponse> syncInvoice(@RequestBody PosSyncRequest request) {
        try {
            PosSyncResponse response = posService.processInvoice(request);
            if ("ERROR".equals(response.getStatus())) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(PosSyncResponse.builder()
                        .status("ERROR")
                        .message(e.getMessage())
                        .build());
        }
    }

    @GetMapping("/voucher/check")
    public ResponseEntity<?> checkVoucher(@RequestParam("code") String code) {
        java.util.Optional<com.bitis.luckydraw.dto.RewardVoucherListDto> opt = rewardVoucherRepository.getRewardVoucherDetail(code);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("status", "ERROR", "message", "Mã không tồn tại trong hệ thống."));
        }
        com.bitis.luckydraw.dto.RewardVoucherListDto detail = opt.get();
        if (detail.getTrangThai() != null && detail.getTrangThai() == 1) {
            return ResponseEntity.badRequest().body(java.util.Map.of("status", "ERROR", "message", "Mã đã được sử dụng."));
        }
        return ResponseEntity.ok(java.util.Map.of(
            "status", "SUCCESS",
            "data", java.util.Map.of(
                "maVoucher", detail.getMaVoucher(),
                "tenGiai", detail.getTenGiai(),
                "tenKhach", detail.getTenKhach(),
                "phone", detail.getPhone()
            )
        ));
    }

    @PostMapping("/voucher/use")
    public ResponseEntity<?> useVoucher(@RequestBody java.util.Map<String, String> payload) {
        String code = payload.get("code");
        String storeCode = payload.get("storeCode");
        
        java.util.Optional<com.bitis.luckydraw.model.RewardVoucher> opt = rewardVoucherRepository.findByMaVoucher(code);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("status", "ERROR", "message", "Mã không tồn tại trong hệ thống."));
        }
        
        com.bitis.luckydraw.model.RewardVoucher voucher = opt.get();
        if (voucher.getTrangThai() != null && voucher.getTrangThai() == 1) {
            return ResponseEntity.badRequest().body(java.util.Map.of("status", "ERROR", "message", "Mã đã được sử dụng rồi."));
        }

        voucher.setTrangThai(1);
        voucher.setThoiGianDoi(java.time.LocalDateTime.now());
        voucher.setMaStoreDoiThuong(storeCode != null ? storeCode : voucher.getMaStorePhatHanh());
        rewardVoucherRepository.save(voucher);

        return ResponseEntity.ok(java.util.Map.of("status", "SUCCESS", "message", "Đã gạch mã voucher thành công."));
    }
}
