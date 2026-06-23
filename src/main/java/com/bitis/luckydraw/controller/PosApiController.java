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

    public PosApiController(PosService posService) {
        this.posService = posService;
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
}
