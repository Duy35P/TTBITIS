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

    @PostMapping("/mock-login")
    public String mockLogin(
            @RequestParam(name = "receipt", required = false) String receipt,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Tạo mock user
        String mockPhone = "0999999999";
        
        // Ponytail: Nếu có receipt, lấy SĐT của hóa đơn đó làm SĐT Zalo luôn để test cho tiện!
        if (receipt != null && !receipt.trim().isEmpty()) {
            Optional<com.bitis.luckydraw.model.Invoice> optInvoice = invoiceRepository.findByMaHoaDon(receipt);
            if (optInvoice.isPresent()) {
                mockPhone = optInvoice.get().getMaKhachHang().replace("CUS-", "");
                session.setAttribute("CURRENT_STORE", optInvoice.get().getMaStore());
            }
        }

        final String finalPhone = mockPhone;
        Customer customer = customerRepository.findByPhone(finalPhone).orElseGet(() -> {
            Customer c = new Customer();
            c.setPhone(finalPhone);
            c.setTenKhach("Zalo User (Mock)");
            c.setZaloId("mock_zalo_id_123");
            c.setTrangThai(1);
            c.setMaKhachHang("CUS-" + finalPhone);
            return customerRepository.save(c);
        });

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
                // Ponytail: Nếu là lỗi "đã sử dụng", chuyển nó thành câu chào mừng thân thiện thay vì báo lỗi đỏ lòm
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

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/customer/login";
    }
}
