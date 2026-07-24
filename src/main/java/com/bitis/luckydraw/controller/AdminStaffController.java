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
@RequestMapping("/quanly/staffs")
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
    private com.bitis.luckydraw.repository.ManagerStoreAssignmentRepository managerStoreAssignmentRepository;

    @Autowired
    private com.bitis.luckydraw.service.StaffExcelService staffExcelService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "role", required = false) String role,
                        @RequestParam(name = "store", required = false) String storeMa) {

        List<StaffListDto> staffs = staffRepository.getStaffList();
        List<Store> stores = storeRepository.findAll();
        
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        java.util.List<String> allowedStores = (principal instanceof com.bitis.luckydraw.security.CustomUserDetails ud) ? ud.getEffectiveStores() : null;
        if (allowedStores != null) {
            staffs.removeIf(s -> !allowedStores.contains(s.getMaStore()));
            stores.removeIf(s -> !allowedStores.contains(s.getMaStore()));
        }
        List<VaiTro> roles = vaiTroRepository.findAll();
        List<ChucNang> chucNangs = chucNangRepository.findAll();
        List<PhanQuyen> phanQuyens = phanQuyenRepository.findAll();
        List<com.bitis.luckydraw.model.ManagerStoreAssignment> managerStoreAssignments = managerStoreAssignmentRepository.findAll();

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
        model.addAttribute("managerStoreAssignments", managerStoreAssignments);
        model.addAttribute("selectedKeyword", keyword != null ? keyword : "");
        model.addAttribute("selectedRole", role != null ? role : "all");
        model.addAttribute("selectedStore", storeMa != null ? storeMa : "all");
        
        String activeTab = (String) model.getAttribute("activeTab");
        if (activeTab == null) {
            model.addAttribute("activeTab", "staffs");
        }

        return "quanly/staff-list";
    }

    @PostMapping("/role/save")
    @Transactional
    public String saveRole(@RequestParam("roleId") String roleId,
                           @RequestParam("roleName") String roleName,
                           @RequestParam(value = "moTa", required = false) String moTa,
                           @RequestParam(value = "loaiPhanBo", required = false, defaultValue = "0") Integer loaiPhanBo,
                           @RequestParam(value = "chucNangIds", required = false) List<String> chucNangIds,
                           RedirectAttributes redirectAttributes) {
        try {
            VaiTro vaiTro = vaiTroRepository.findById(roleId).orElse(new VaiTro());
            vaiTro.setRoleId(roleId);
            vaiTro.setRoleName(roleName);
            vaiTro.setMoTa(moTa);
            vaiTro.setLoaiPhanBo(loaiPhanBo);
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
        return "redirect:/quanly/staffs";
    }

    @PostMapping("/save")
    @Transactional
    public String saveStaff(@RequestParam("username") String username,
                            @RequestParam(value = "password", required = false) String password,
                            @RequestParam("tenNhanVien") String tenNhanVien,
                            @RequestParam("roleId") String roleId,
                            @RequestParam(value = "maStore", required = false) String maStore,
                            @RequestParam(value = "multiStore", required = false) List<String> multiStore,
                            @RequestParam("trangThai") Integer trangThai,
                            RedirectAttributes redirectAttributes) {

        Optional<Staff> opt = staffRepository.findByUsername(username);
        Staff staff;
        if (opt.isPresent()) {
            staff = opt.get();
            if (password != null && !password.trim().isEmpty()) {
                staff.setPassword(passwordEncoder.encode(password));
            }
        } else {
            staff = new Staff();
            staff.setMaNhanVien("NV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            staff.setUsername(username);
            if (password != null && !password.trim().isEmpty()) {
                staff.setPassword(passwordEncoder.encode(password));
            } else {
                staff.setPassword(passwordEncoder.encode("123456")); // default
            }
        }

        staff.setTenNhanVien(tenNhanVien);
        staff.setRoleId(roleId);
        
        VaiTro role = vaiTroRepository.findById(roleId).orElse(null);
        int loaiPhanBo = role != null && role.getLoaiPhanBo() != null ? role.getLoaiPhanBo() : 0;
        
        if (loaiPhanBo == 1) {
            staff.setMaStore(maStore);
        } else {
            staff.setMaStore(null);
        }
        
        staff.setTrangThai(trangThai);
        staffRepository.save(staff);

        managerStoreAssignmentRepository.deleteByUsername(username);
        if (loaiPhanBo == 2 && multiStore != null && !multiStore.isEmpty()) {
            for (String s : multiStore) {
                managerStoreAssignmentRepository.save(new com.bitis.luckydraw.model.ManagerStoreAssignment(username, s));
            }
        }
        redirectAttributes.addFlashAttribute("success", "Lưu nhân viên thành công!");
        return "redirect:/quanly/staffs";
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
        return "redirect:/quanly/staffs";
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
