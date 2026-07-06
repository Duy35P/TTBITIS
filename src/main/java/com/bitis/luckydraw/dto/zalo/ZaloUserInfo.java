package com.bitis.luckydraw.dto.zalo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZaloUserInfo {
    private String id;
    private String name;
    private Picture picture;
    
    // API Error fields
    private Integer error;
    private String message;

    @Data
    public static class Picture {
        private PictureData data;
    }

    @Data
    public static class PictureData {
        private String url;
    }
}
