package com.bitis.luckydraw.service;

import com.bitis.luckydraw.dto.zalo.ZaloUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
public class ZaloAuthService {

    @Value("${zalo.app.id}")
    private String appId;

    @Value("${zalo.app.secret}")
    private String appSecret;

    @Value("${zalo.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Sinh code_verifier (PKCE)
    public String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    // 2. Sinh code_challenge từ code_verifier (PKCE)
    public String generateCodeChallenge(String codeVerifier) {
        try {
            byte[] bytes = codeVerifier.getBytes("US-ASCII");
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes, 0, bytes.length);
            byte[] digest = messageDigest.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi sinh code challenge", e);
        }
    }

    // 3. Tạo URL chuyển hướng đăng nhập
    public String getAuthorizationUrl(String state, String codeChallenge) {
        return String.format("https://oauth.zaloapp.com/v4/permission?app_id=%s&redirect_uri=%s&state=%s&code_challenge=%s",
                appId, redirectUri, state, codeChallenge);
    }

    // 4. Lấy Access Token từ authorization_code
    public String getAccessToken(String authCode, String codeVerifier) {
        String url = "https://oauth.zaloapp.com/v4/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("secret_key", appSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authCode);
        body.add("app_id", appId);
        body.add("grant_type", "authorization_code");
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        if (response.getBody() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> map = mapper.readValue(response.getBody(), java.util.Map.class);
                if (map.containsKey("access_token")) {
                    return (String) map.get("access_token");
                }
            } catch (Exception e) {
                throw new RuntimeException("Lỗi parse Zalo response: " + e.getMessage());
            }
        }
        throw new RuntimeException("Không thể lấy Access Token từ Zalo: " + response.getBody());
    }

    // 5. Lấy thông tin user bằng Access Token
    public ZaloUserInfo getUserInfo(String accessToken) {
        String url = "https://graph.zalo.me/v2.0/me?fields=id,name,picture";

        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", accessToken);
        
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        
        if (response.getBody() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                ZaloUserInfo userInfo = mapper.readValue(response.getBody(), ZaloUserInfo.class);
                if (userInfo.getError() != null && userInfo.getError() != 0) {
                    throw new RuntimeException("Zalo API Error (" + userInfo.getError() + "): " + userInfo.getMessage());
                }
                if (userInfo.getId() != null) {
                    return userInfo;
                }
            } catch (Exception e) {
                throw new RuntimeException("Lỗi parse Zalo User Info: " + e.getMessage() + " | Response: " + response.getBody());
            }
        }
        throw new RuntimeException("Không thể lấy thông tin Zalo User");
    }
}
