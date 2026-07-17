package com.bitis.luckydraw.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class RootVerifyController {
    @GetMapping("/")
    public String root() {
        return "<html><head><meta name=\"zalo-platform-site-verification\" content=\"SlZkDvZORsbSgzXefuL2KMAIma-Ev1WGDp8t\" /></head><body>Zalo Verify</body></html>";
    }
}
