package com.bitis.luckydraw.dto;

public interface StoreCampaignProjection {
    Long getStoreId();
    String getActiveCampaigns();
    String getPendingCampaigns();
}
