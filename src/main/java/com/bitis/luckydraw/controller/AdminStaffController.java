package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.StaffListDto;
import com.bitis.luckydraw.model.Staff;
import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.model.VaiTro;
import com.bitis.luckydraw.model.ChucNang;
import com.bitis.luckydraw.model.PhanQuyen;
import com.bitis.luckydraw.model.PhanQuyenId;
import com.bitis.luckydraw.repository.ChucNangRepository;
import com.bitis.luckydraw.repository.PhanQuyenRepository;
import com.bitis.luckydraw.repository.StaffRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import com.bitis.luckydraw.repository.VaiTroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/staffs")
public class AdminStaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private ChucNangRepository chucNangRepository;

    @Autowired
    private PhanQuyenRepository phanQuyenRepository;

    @Autowired
    private com.bitis.luckydraw.service.StaffExcelService staffExcelService;

    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "role", required = false) String role,
                        @RequestParam(name = "store", required = false) String storeMa) {

        List<StaffListDto> staffs = staffRepository.getStaffList();
        List<Store> stores = storeRepository.findAll();
        List<VaiTro> roles = vaiTroRepository.findAll();
        List<ChucNang> chucNangs = chucNangRepository.findAll();
        List<PhanQuyen> phanQuyens = phanQuyenRepository.findAll();

        // Apply filters
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            staffs = staffs.stream()
                    .filter(s -> (s.getUsername() != null && s.getUsername().toLowerCase().contains(lowerKeyword)) ||
                            (s.getTenNhanVien() != null && s.getTenNhanVien().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());
        }
        if (role != null && !role.isEmpty() && !role.equals("all")) {
            staffs = staffs.stream()
                    .filter(s -> role.equals(s.getRoleId()))
                    .collect(Collectors.toList());
        }
        if (storeMa != null && !storeMa.isEmpty() && !storeMa.equals("all")) {
            staffs = staffs.stream()
                    .filter(s -> storeMa.equals(s.getMaStore()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("staffs", staffs);
        model.addAttribute("stores", stores);
        model.addAttribute("roles", roles);
        model.addAttribute("chucNangs", chucNangs);
        model.addAttribute("phanQuyens", phanQuyens);
        model.addAttribute("selectedKeyword", keyword != null ? keyword : "");
        model.addAttribute("selectedRole", role != null ? role : "all");
        model.addAttribute("selectedStore", storeMa != null ? storeMa : "all");
        
        String activeTab = (String) model.getAttribute("activeTab");
        if (activeTab == null) {
            model.addAttribute("activeTab", "staffs");
        }

        return "admin/staff-list";
    }

    @PostMapping("/role/save")
    @Transactional
    public String saveRole(@RequestParam("roleId") String roleId,
                           @RequestParam("roleName") String roleName,
                           @RequestParam(value = "moTa", required = false) String moTa,
                           @RequestParam(value = "chucNangIds", required = false) List<String> chucNangIds,
                           RedirectAttributes redirectAttributes) {
        try {
            VaiTro vaiTro = vaiTroRepository.findById(roleId).orElse(new VaiTro());
            vaiTro.setRoleId(roleId);
            vaiTro.setRoleName(roleName);
            vaiTro.setMoTa(moTa);
            vaiTroRepository.save(vaiTro);

            phanQuyenRepository.deleteByIdRoleId(roleId);

            if (chucNangIds != null && !chucNangIds.isEmpty()) {
                for (String cnId : chucNangIds) {
                    PhanQuyen pq = new PhanQuyen();
                    pq.setId(new PhanQuyenId(roleId, cnId));
                    phanQuyenRepository.save(pq);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Lưu vai trò và phân quyền thành công!");
            redirectAttributes.addFlashAttribute("activeTab", "roles");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "roles");
        }
        return "redirect:/admin/staffs";
    }

    @PostMapping("/save")
    public String saveStaff(@RequestParam("username") String username,
                            @RequestParam(value = "password", required = false) String password,
                            @RequestParam("tenNhanVien") String tenNhanVien,
                            @RequestParam("roleId") String roleId,
                            @RequestParam(value = "maStore", required = false) String maStore,
                            @RequestParam("trangThai") Integer trangThai,
                            RedirectAttributes redirectAttributes) {

        Optional<Staff> opt = staffRepository.findByUsername(username);
        Staff staff;
        if (opt.isPresent()) {
            staff = opt.get();
            if (password != null && !password.trim().isEmpty()) {
                staff.setPassword(password); // Note: should encode password in real app
            }
        } else {
            staff = new Staff();
            staff.setMaNhanVien("NV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            staff.setUsername(username);
            if (password != null && !password.trim().isEmpty()) {
                staff.setPassword(password);
            } else {
                staff.setPassword("123456"); // default
            }
        }

        staff.setTenNhanVien(tenNhanVien);
        staff.setRoleId(roleId);
        
        if ("ADMIN".equals(roleId)) {
            staff.setMaStore(null);
        } else {
            staff.setMaStore(maStore);
        }
        
        staff.setTrangThai(trangThai);

        staffRepository.save(staff);
        redirectAttributes.addFlashAttribute("success", "Lưu nhân viên thành công!");
        return "redirect:/admin/staffs";
    }

    @PostMapping("/toggle-status")
    public String toggleStatus(@RequestParam("username") String username, RedirectAttributes redirectAttributes) {
        Optional<Staff> opt = staffRepository.findByUsername(username);
        if (opt.isPresent()) {
            Staff staff = opt.get();
            staff.setTrangThai(staff.getTrangThai() == 1 ? 0 : 1);
            staffRepository.save(staff);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/staffs";
    }

    @PostMapping("/import-excel")
    public String importExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel!");
            return "redirect:/admin/staffs";
        }
        try {
            List<Staff> staffs = staffExcelService.parseExcelFile(file);
            int count = 0;
            for (Staff staff : staffs) {
                Optional<Staff> opt = staffRepository.findByUsername(staff.getUsername());
                if (opt.isPresent()) {
                    Staff existing = opt.get();
                    existing.setTenNhanVien(staff.getTenNhanVien());
                    existing.setRoleId(staff.getRoleId());
                    existing.setMaStore(staff.getMaStore());
                    existing.setTrangThai(staff.getTrangThai());
                    if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
                        existing.setPassword(staff.getPassword());
                    }
                    staffRepository.save(existing);
                    count++;
                } else {
                    if (staff.getPassword() == null || staff.getPassword().isEmpty()) {
                        staff.setPassword("123456");
                    }
                    staffRepository.save(staff);
                    count++;
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã import thành công " + count + " nhân viên mới!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi import: " + e.getMessage());
        }
        return "redirect:/admin/staffs";
    }

    @GetMapping("/export-excel")
    public void exportExcel(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<StaffListDto> staffs = staffRepository.getStaffList();
            String[] headers = {"Username", "Tên Nhân Viên", "Vai Trò", "Mã Cửa Hàng", "Trạng Thái"};
            List<String[]> data = staffs.stream().map(s -> new String[]{
                s.getUsername(),
                s.getTenNhanVien(),
                s.getRoleId(),
                s.getMaStore() != null ? s.getMaStore() : "",
                s.getTrangThai() != null && s.getTrangThai() == 1 ? "1" : "0"
            }).collect(Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachNhanVien", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
