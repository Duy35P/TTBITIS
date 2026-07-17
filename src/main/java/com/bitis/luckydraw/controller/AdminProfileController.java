package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Staff;
import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.StaffRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/quanly/profile")
public class AdminProfileController {

    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping
    public String index(Model model) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Staff staff = staffRepository.findByUsername(username).orElse(null);
        if (staff == null) {
            return "redirect:/quanly"; // Fallback nếu không có dữ liệu
        }
        
        String storeName = "Không có (Trụ sở chính)";
        if (staff.getMaStore() != null && !staff.getMaStore().isEmpty()) {
            Optional<Store> storeOpt = storeRepository.findByMaStore(staff.getMaStore());
            if (storeOpt.isPresent()) {
                storeName = storeOpt.get().getTenCuaHang() + " (" + staff.getMaStore() + ")";
            }
        }

        model.addAttribute("staff", staff);
        model.addAttribute("storeName", storeName);
        
        return "quanly/profile";
    }

    @PostMapping("/update-info")
    @ResponseBody
    @Transactional
    public Map<String, Object> updateInfo(@RequestParam("tenNhanVien") String tenNhanVien) {
        Map<String, Object> response = new HashMap<>();
        if (tenNhanVien == null || tenNhanVien.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Họ và tên không được để trống!");
            return response;
        }

        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Staff staff = staffRepository.findByUsername(username).orElse(null);
            if (staff != null) {
                staff.setTenNhanVien(tenNhanVien.trim());
                staffRepository.save(staff);
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy người dùng!");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/change-password")
    @ResponseBody
    @Transactional
    public Map<String, Object> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        
        Map<String, Object> response = new HashMap<>();
        if (newPassword == null || newPassword.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Mật khẩu mới không được để trống!");
            return response;
        }

        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Staff staff = staffRepository.findByUsername(username).orElse(null);
            if (staff != null) {
                if (!passwordEncoder.matches(oldPassword, staff.getPassword())) {
                    response.put("success", false);
                    response.put("message", "Mật khẩu hiện tại không đúng!");
                    return response;
                }
                
                staff.setPassword(passwordEncoder.encode(newPassword));
                staffRepository.save(staff);
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy người dùng!");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        return response;
    }
}
