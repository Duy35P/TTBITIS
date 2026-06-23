package com.bitis.luckydraw.dto;

import java.util.List;
import lombok.Data;

@Data
public class CampaignRuleForm {
    private Double giaTriDonHangToiThieu;
    private List<String> paymentMethods;
    private List<Integer> paymentTurns;
    private List<String> skuCodes;
    private List<Integer> skuTurns;
}
