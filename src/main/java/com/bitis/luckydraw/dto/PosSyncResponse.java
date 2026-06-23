package com.bitis.luckydraw.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosSyncResponse {
    private String status;
    private String message;
    private int totalTurns;
    private List<String> appliedCampaigns;
}
