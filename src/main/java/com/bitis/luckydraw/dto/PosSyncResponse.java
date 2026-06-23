package com.bitis.luckydraw.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class PosSyncResponse {
    private String status; // "SUCCESS", "ERROR"
    private String message;
    private List<String> appliedCampaigns;
    private int totalTurns;
}
