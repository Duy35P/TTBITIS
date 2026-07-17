import re
with open('src/main/java/com/bitis/luckydraw/controller/CustomerAuthController.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace mockLogin Smart Routing
content = re.sub(r'// 3\. Smart Routing.*?redirectAttributes\.addFlashAttribute\(\"successMessage\", \"Đăng nhập thành công!\"\);',
                 '// 3. Smart Routing\n        if (receipt != null && !receipt.trim().isEmpty()) {\n            return \"redirect:/customer/auth/process-receipt?receipt=\" + receipt;\n        }\n\n        redirectAttributes.addFlashAttribute(\"successMessage\", \"Đăng nhập thành công!\");',
                 content, flags=re.DOTALL)

# Replace zaloCallback Smart Routing
content = re.sub(r'// Smart Routing \(giống hệt mockLogin\).*?redirectAttributes\.addFlashAttribute\(\"successMessage\", \"Đăng nhập Zalo thành công!\"\);',
                 '// Smart Routing\n            if (customer.getPhone() != null && customer.getPhone().startsWith(\"ZALO\")) {\n                if (receipt != null && !receipt.trim().isEmpty()) {\n                    session.setAttribute(\"PENDING_RECEIPT\", receipt);\n                }\n                return \"redirect:/customer/update-phone\";\n            }\n            if (receipt != null && !receipt.trim().isEmpty()) {\n                return \"redirect:/customer/auth/process-receipt?receipt=\" + receipt;\n            }\n\n            redirectAttributes.addFlashAttribute(\"successMessage\", \"Đăng nhập Zalo thành công!\");',
                 content, flags=re.DOTALL)

# Add processReceipt
process_receipt_code = '''
    @org.springframework.web.bind.annotation.GetMapping("/process-receipt")
    public String processReceipt(@org.springframework.web.bind.annotation.RequestParam(name = "receipt") String receipt, HttpSession session, RedirectAttributes redirectAttributes) {
        String maKhachHang = (String) session.getAttribute("CUSTOMER_MA");
        if (maKhachHang == null) return "redirect:/customer/login";
        
        try {
            boolean tokenUsed = turnManagementService.useGameAccessToken(receipt, maKhachHang);
            if (tokenUsed) {
                redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thành công! Bạn đã dùng mã vào game.");
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
                redirectAttributes.addFlashAttribute("successMessage", "Chào mừng bạn quay lại! Hóa đơn này đã được xác nhận trước đó.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
            return "redirect:/customer/index";
        }
    }

    @RequestMapping("/logout")'''

content = re.sub(r'@RequestMapping\(\"/logout\"\)', process_receipt_code, content)

with open('src/main/java/com/bitis/luckydraw/controller/CustomerAuthController.java', 'w', encoding='utf-8') as f:
    f.write(content)
