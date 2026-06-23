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
            PosSyncResponse response = posService.syncInvoice(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PosSyncResponse err = new PosSyncResponse();
            err.setStatus("ERROR");
            err.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
