package com.bitis.luckydraw.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class GameViewController {

    @Autowired
    private com.bitis.luckydraw.repository.CampaignRepository campaignRepository;

    // Cung cấp URL Slug thân thiện SEO
    @GetMapping("/game/{slug}")
    public String spinPageBySlug(@PathVariable("slug") String slug, 
                                 @org.springframework.web.bind.annotation.RequestParam(value = "preview", required = false) String preview,
                                 HttpSession session, Model model) {
        if (session.getAttribute("CUSTOMER_ID") == null) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if ("true".equals(preview) && auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                session.setAttribute("CUSTOMER_ID", "PREVIEW");
                model.addAttribute("isPreview", true);
            } else {
                return "redirect:/customer/login";
            }
        }
        
        String phone = (String) session.getAttribute("CUSTOMER_PHONE");
        if (phone != null && phone.startsWith("ZALO") && !"PREVIEW".equals(session.getAttribute("CUSTOMER_ID"))) {
            return "redirect:/customer/update-phone";
        }

        com.bitis.luckydraw.model.Campaign campaign = null;
        for (com.bitis.luckydraw.model.Campaign c : campaignRepository.findAll()) {
            if (slug.equals(c.getDuongDanSlug())) {
                campaign = c;
                break;
            }
        }

        if (campaign == null) {
            return "redirect:/customer/index";
        }

        model.addAttribute("campaignId", campaign.getMaChienDich());
        return "customer/spin-wheel-demo";
    }
}
