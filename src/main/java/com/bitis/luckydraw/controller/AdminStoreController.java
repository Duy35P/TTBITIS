package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bitis.luckydraw.dto.StoreCampaignProjection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.model.CampaignStore;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.CampaignStoreRepository;
import com.bitis.luckydraw.service.StoreExcelService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin/stores")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('QL_CUAHANG')")
public class AdminStoreController {

    private final StoreRepository storeRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final StoreExcelService storeExcelService;

    public AdminStoreController(StoreRepository storeRepository, CampaignRepository campaignRepository, CampaignStoreRepository campaignStoreRepository, StoreExcelService storeExcelService) {
        this.storeRepository = storeRepository;
        this.campaignRepository = campaignRepository;
        this.campaignStoreRepository = campaignStoreRepository;
        this.storeExcelService = storeExcelService;
    }

    @GetMapping
    public String listStores(Model model) {
        List<Store> stores = storeRepository.findAll();
        List<Campaign> allCampaigns = campaignRepository.findAll();
        List<CampaignStore> allMappings = campaignStoreRepository.findAll();
        
        Map<String, Campaign> campaignMap = allCampaigns.stream().collect(Collectors.toMap(Campaign::getMaChienDich, c -> c));
        
        Map<Long, List<Campaign>> activeCampaignsMap = new HashMap<>();
        Map<Long, List<Campaign>> pendingCampaignsMap = new HashMap<>();
        
        for (Store store : stores) {
            List<Campaign> storeCampaigns = allMappings.stream()
                .filter(m -> m.getMaStore().equals(store.getMaStore()))
                .map(m -> campaignMap.get(m.getMaChienDich()))
                .filter(c -> c != null)
                .collect(Collectors.toList());
                
            activeCampaignsMap.put(store.getId(), storeCampaigns.stream().filter(c -> c.getTrangThai() == 1).collect(Collectors.toList()));
            pendingCampaignsMap.put(store.getId(), storeCampaigns.stream().filter(c -> c.getTrangThai() == 0).collect(Collectors.toList()));
        }

        model.addAttribute("stores", stores);
        model.addAttribute("activeCampaignsMap", activeCampaignsMap);
        model.addAttribute("pendingCampaignsMap", pendingCampaignsMap);
        return "admin/store-list";
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CUAHANG_EDIT') or hasAuthority('ACT_CUAHANG_ADD')")
    @PostMapping("/save")
    public String saveStore(@RequestParam(name = "storeId", required = false) String storeIdStr,
                            @RequestParam String tenCuaHang,
                            @RequestParam String maStore,
                            @RequestParam String diaChiStore,
                            @RequestParam(required = false, defaultValue = "0") Integer trangThai) {
        Store store;
        if (storeIdStr != null && !storeIdStr.trim().isEmpty()) {
            Long storeId = Long.parseLong(storeIdStr);
            store = storeRepository.findById(storeId).orElse(new Store());
            store.setTrangThai(trangThai);
        } else {
            store = new Store();
            store.setTrangThai(0); // Luôn luôn tạm ngưng khi mới tạo
            store.setMaStore(maStore);
        }
        
        store.setTenCuaHang(tenCuaHang);
        store.setDiaChiStore(diaChiStore);
        
        storeRepository.save(store);
        
        return "redirect:/admin/stores";
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CUAHANG_IMPORT')")
    @PostMapping("/import-excel")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel!");
            return "redirect:/admin/stores";
        }
        try {
            List<Store> stores = storeExcelService.parseExcelFile(file);
            int count = 0;
            for (Store store : stores) {
                java.util.Optional<Store> opt = storeRepository.findByMaStore(store.getMaStore());
                if (opt.isPresent()) {
                    Store existing = opt.get();
                    existing.setTenCuaHang(store.getTenCuaHang());
                    existing.setDiaChiStore(store.getDiaChiStore());
                    // Không đổi trạng thái nếu đã tồn tại để tránh rủi ro
                    storeRepository.save(existing);
                    count++;
                } else {
                    storeRepository.save(store);
                    count++;
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã import thành công " + count + " cửa hàng mới!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi import: " + e.getMessage());
        }
        return "redirect:/admin/stores";
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CUAHANG_EXPORT')")
    @GetMapping("/export-excel")
    public void exportExcel(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<Store> stores = storeRepository.findAll();
            String[] headers = {"Mã Cửa Hàng", "Tên Cửa Hàng", "Địa Chỉ", "Trạng Thái"};
            List<String[]> data = stores.stream().map(s -> new String[]{
                s.getMaStore(),
                s.getTenCuaHang(),
                s.getDiaChiStore(),
                s.getTrangThai() != null && s.getTrangThai() == 1 ? "Hoạt động" : "Tạm ngưng"
            }).collect(Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachCuaHang", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
