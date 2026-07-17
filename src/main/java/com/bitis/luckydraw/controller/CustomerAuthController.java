package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Customer;
import com.bitis.luckydraw.repository.CustomerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/customer/auth")
public class CustomerAuthController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private com.bitis.luckydraw.service.TurnManagementService turnManagementService;

    @Autowired
    private com.bitis.luckydraw.repository.InvoiceRepository invoiceRepository;

    @Autowired
    private com.bitis.luckydraw.repository.GameAccessTokenRepository gameAccessTokenRepository;

    @Autowired
    private com.bitis.luckydraw.service.ZaloAuthService zaloAuthService;

    @PostMapping("/mock-login")
    public String mockLogin(
            @RequestParam(name = "receipt", required = false) String receipt,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Tạo mock user
        // String[] để lambda ifPresent có thể ghi vào
        String[] mockPhone = {"0999999999"};
        
        if (receipt != null && !receipt.trim().isEmpty()) {
            gameAccessTokenRepository.findByToken(receipt).ifPresent(gat -> {
                if (gat.getMaKhachHangKichHoat() != null) {
                    mockPhone[0] = gat.getMaKhachHangKichHoat().replace("CUS-", "");
                }
                invoiceRepository.findByMaHoaDon(gat.getMaHoaDon())
                        .ifPresent(inv -> session.setAttribute("CURRENT_STORE", inv.getMaStore()));
            });
        }

        final String finalPhone = mockPhone[0];
        Customer customer = null;
        Optional<Customer> optCustomer = customerRepository.findByPhone(finalPhone);
        if (optCustomer.isPresent()) {
            customer = optCustomer.get();
        } else {
            Customer c = new Customer();
            c.setPhone(finalPhone);
            c.setTenKhach("Zalo User (Mock)");
            c.setZaloId("mock_zalo_id_123");
            c.setTrangThai(1);
            c.setMaKhachHang("CUS-" + finalPhone);
            customer = customerRepository.save(c);
        }

        // Kiểm tra tài khoản bị khóa
        if (customer.getTrangThai() != null && customer.getTrangThai() == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản Zalo của bạn đã bị khóa. Vui lòng liên hệ CSKH.");
            if (receipt != null && !receipt.trim().isEmpty()) {
                return "redirect:/customer/login?receipt=" + receipt;
            }
            return "redirect:/customer/login";
        }

        // 2. Lưu vào Session
        session.setAttribute("CUSTOMER_ID", customer.getId());
        session.setAttribute("CUSTOMER_PHONE", customer.getPhone());
        session.setAttribute("CUSTOMER_NAME", customer.getTenKhach());
        session.setAttribute("CUSTOMER_MA", customer.getMaKhachHang());

        // 3. Smart Routing
        if (receipt != null && !receipt.trim().isEmpty()) {
            try {
                // Thử tìm và sử dụng token nếu đã được POS xử lý (Webhook)
                boolean tokenUsed = turnManagementService.useGameAccessToken(receipt, customer.getMaKhachHang());
                
                if (tokenUsed) {
                    redirectAttributes.addFlashAttribute("successMessage", "Bạn đã dùng mã QR thành công và vào game.");
                    return "redirect:/customer/index";
                }
                
                // Fallback: Xử lý trực tiếp hóa đơn nếu token chưa có
                java.util.List<String> campaigns = turnManagementService.claimInvoice(receipt, customer.getMaKhachHang());
                if (campaigns.size() == 1) {
                    redirectAttributes.addFlashAttribute("successMessage", "Bạn nhận được lượt quay từ mã QR " + receipt);
                    return "redirect:/customer/spin?campaign=" + campaigns.get(0);
                } else if (campaigns.size() > 1) {
                    redirectAttributes.addFlashAttribute("successMessage", "Tuyệt vời! Mã QR mang lại lượt quay ở " + campaigns.size() + " chương trình. Mời bạn chọn chương trình để chơi.");
                    return "redirect:/customer/index";
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mã QR (Hóa đơn) này không thỏa điều kiện nhận lượt quay.");
                    return "redirect:/customer/index";
                }
            } catch (Exception e) {
                if (e.getMessage() != null && (e.getMessage().contains("đã được sử dụng") || e.getMessage().contains("đã được nhận lượt"))) {
                    redirectAttributes.addFlashAttribute("successMessage", "Chào mừng bạn quay lại! Hóa đơn này đã được xác nhận trước đó.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                }
                return "redirect:/customer/index";
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
        return "redirect:/customer/index";
    }

    @org.springframework.web.bind.annotation.GetMapping("/zalo/login")
    public String zaloLogin(@RequestParam(name = "receipt", required = false) String receipt, HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
        String codeVerifier = zaloAuthService.generateCodeVerifier();
        String codeChallenge = zaloAuthService.generateCodeChallenge(codeVerifier);
        session.setAttribute("ZALO_CODE_VERIFIER", codeVerifier); // PKCE require verifier in callback
        
        String state = receipt != null && !receipt.trim().isEmpty() ? receipt : "GENERAL_LOGIN";
        
        // Dynamically build redirect URI based on current request origin
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) scheme = request.getScheme();
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null) {
            host = request.getServerName();
            int port = request.getServerPort();
            if (port != 80 && port != 443) {
                host += ":" + port;
            }
        }
        String dynamicRedirectUri = scheme + "://" + host + "/customer/auth/zalo/callback";
        
        return "redirect:" + zaloAuthService.getAuthorizationUrl(state, codeChallenge, dynamicRedirectUri);
    }

    @org.springframework.web.bind.annotation.GetMapping("/zalo/callback")
    public String zaloCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (code == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không nhận được mã xác thực từ Zalo.");
            return "redirect:/customer/login";
        }

        String codeVerifier = (String) session.getAttribute("ZALO_CODE_VERIFIER");
        if (codeVerifier == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng nhập hết hạn. Vui lòng thử lại.");
            return "redirect:/customer/login";
        }

        try {
            // Lấy token và thông tin Zalo
            String accessToken = zaloAuthService.getAccessToken(code, codeVerifier);
            com.bitis.luckydraw.dto.zalo.ZaloUserInfo userInfo = zaloAuthService.getUserInfo(accessToken);

            // Tìm hoặc tạo Customer
            Customer customer;
            Optional<Customer> optCustomer = customerRepository.findByZaloId(userInfo.getId());
            if (optCustomer.isPresent()) {
                customer = optCustomer.get();
            } else {
                customer = new Customer();
                customer.setZaloId(userInfo.getId());
                customer.setTenKhach(userInfo.getName());
                // Khách từ Zalo nếu không có SDT thì dùng Zalo ID làm SDT giả để thỏa mãn logic hiện tại (laziest path)
                String fakePhone = "ZALO" + userInfo.getId().substring(0, Math.min(6, userInfo.getId().length()));
                customer.setPhone(fakePhone);
                customer.setTrangThai(1);
                customer.setMaKhachHang("CUS-ZALO-" + userInfo.getId());
                customer = customerRepository.save(customer);
            }

            // Kiểm tra tài khoản bị khóa
            if (customer.getTrangThai() != null && customer.getTrangThai() == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ CSKH.");
                return "redirect:/customer/login" + (state != null && !state.isEmpty() ? "?receipt=" + state : "");
            }

            // Set Store nếu có state (receipt)
            String receipt = state;
            if ("GENERAL_LOGIN".equals(receipt)) {
                receipt = null;
            }
            if (receipt != null && !receipt.trim().isEmpty()) {
                gameAccessTokenRepository.findByToken(receipt).ifPresent(gat -> {
                    invoiceRepository.findByMaHoaDon(gat.getMaHoaDon())
                            .ifPresent(inv -> session.setAttribute("CURRENT_STORE", inv.getMaStore()));
                });
            }

            // Lưu Session
            session.setAttribute("CUSTOMER_ID", customer.getId());
            session.setAttribute("CUSTOMER_PHONE", customer.getPhone());
            session.setAttribute("CUSTOMER_NAME", customer.getTenKhach());
            session.setAttribute("CUSTOMER_MA", customer.getMaKhachHang());
            if (userInfo.getPicture() != null && userInfo.getPicture().getData() != null) {
                session.setAttribute("CUSTOMER_AVATAR", userInfo.getPicture().getData().getUrl());
            }

            // --- SMART ROUTING ĐỂ ÉP NHẬP SỐ ĐIỆN THOẠI TRƯỚC KHI DÙNG TOKEN ---
            if (customer.getPhone().startsWith("ZALO")) {
                if (receipt != null && !receipt.trim().isEmpty()) {
                    session.setAttribute("PENDING_RECEIPT", receipt);
                }
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng cập nhật số điện thoại thật để tiếp tục.");
                return "redirect:/customer/update-phone";
            }

            // Smart Routing (giống hệt mockLogin)
            if (receipt != null && !receipt.trim().isEmpty()) {
                try {
                    boolean tokenUsed = turnManagementService.useGameAccessToken(receipt, customer.getMaKhachHang());
                    if (tokenUsed) {
                        redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập Zalo thành công! Bạn đã dùng mã QR vào game.");
                        return "redirect:/customer/index";
                    }
                    java.util.List<String> campaigns = turnManagementService.claimInvoice(receipt, customer.getMaKhachHang());
                    if (campaigns.size() == 1) {
                        redirectAttributes.addFlashAttribute("successMessage", "Bạn nhận được lượt quay từ hóa đơn.");
                        return "redirect:/customer/spin?campaign=" + campaigns.get(0);
                    } else if (campaigns.size() > 1) {
                        redirectAttributes.addFlashAttribute("successMessage", "Mã hóa đơn áp dụng cho " + campaigns.size() + " chương trình.");
                        return "redirect:/customer/index";
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", "Mã không thỏa điều kiện nhận lượt.");
                        return "redirect:/customer/index";
                    }
                } catch (Exception e) {
                    if (e.getMessage() != null && (e.getMessage().contains("đã được sử dụng") || e.getMessage().contains("đã được nhận lượt"))) {
                        redirectAttributes.addFlashAttribute("successMessage", "Chào mừng bạn quay lại!");
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                    }
                    return "redirect:/customer/index";
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập Zalo thành công!");
            return "redirect:/customer/index";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đăng nhập Zalo: " + e.getMessage());
            return "redirect:/customer/login";
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/customer/login";
    }

    @org.springframework.web.bind.annotation.GetMapping("/process-receipt")
    public String processReceipt(HttpSession session, RedirectAttributes redirectAttributes) {
        String receipt = (String) session.getAttribute("PENDING_RECEIPT");
        String maKhachHang = (String) session.getAttribute("CUSTOMER_MA");

        if (maKhachHang == null) {
            return "redirect:/customer/login";
        }

        if (receipt == null || receipt.trim().isEmpty()) {
            return "redirect:/customer/index";
        }

        // Xóa receipt khỏi session sau khi xử lý để tránh lặp lại
        session.removeAttribute("PENDING_RECEIPT");

        try {
            boolean tokenUsed = turnManagementService.useGameAccessToken(receipt, maKhachHang);
            if (tokenUsed) {
                redirectAttributes.addFlashAttribute("successMessage", "Xác nhận mã QR thành công! Lượt quay đã được cộng vào tài khoản của bạn.");
                return "redirect:/customer/index";
            }
            java.util.List<String> campaigns = turnManagementService.claimInvoice(receipt, maKhachHang);
            if (campaigns.size() == 1) {
                redirectAttributes.addFlashAttribute("successMessage", "Bạn nhận được lượt quay từ hóa đơn.");
                return "redirect:/customer/spin?campaign=" + campaigns.get(0);
            } else if (campaigns.size() > 1) {
                redirectAttributes.addFlashAttribute("successMessage", "Mã hóa đơn áp dụng cho " + campaigns.size() + " chương trình.");
                return "redirect:/customer/index";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Mã không thỏa điều kiện nhận lượt.");
                return "redirect:/customer/index";
            }
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("đã được sử dụng") || e.getMessage().contains("đã được nhận lượt"))) {
                redirectAttributes.addFlashAttribute("successMessage", "Chào mừng bạn quay lại!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
            return "redirect:/customer/index";
        }
    }
}
