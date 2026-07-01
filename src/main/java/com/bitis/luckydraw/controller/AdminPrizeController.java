package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.StoreInventoryDto;
import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.model.Prize;
import com.bitis.luckydraw.dto.PrizeListDto;
import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.PrizeRepository;
import com.bitis.luckydraw.repository.StorePrizeInventoryRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import com.bitis.luckydraw.model.CampaignStore;
import com.bitis.luckydraw.repository.CampaignStoreRepository;
import com.bitis.luckydraw.service.PrizeService;
import com.bitis.luckydraw.service.PrizeExcelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/prizes")
public class AdminPrizeController {

    private final PrizeRepository prizeRepository;
    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final StorePrizeInventoryRepository storePrizeInventoryRepository;
    private final PrizeService prizeService;
    private final CampaignStoreRepository campaignStoreRepository;
    private final PrizeExcelService prizeExcelService;

    public AdminPrizeController(CampaignRepository campaignRepository,
                                PrizeRepository prizeRepository,
                                StoreRepository storeRepository,
                                StorePrizeInventoryRepository storePrizeInventoryRepository,
                                PrizeService prizeService,
                                CampaignStoreRepository campaignStoreRepository,
                                PrizeExcelService prizeExcelService) {
        this.campaignRepository = campaignRepository;
        this.prizeRepository = prizeRepository;
        this.storeRepository = storeRepository;
        this.storePrizeInventoryRepository = storePrizeInventoryRepository;
        this.prizeService = prizeService;
        this.campaignStoreRepository = campaignStoreRepository;
        this.prizeExcelService = prizeExcelService;
    }

    @GetMapping
    public String listPrizes(
            @RequestParam(required = false, defaultValue = "prizes") String tab, 
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "campaign", required = false) String campaign,
            @RequestParam(name = "prize", required = false) String prizeParam,
            Model model) {
            
        String maStore = (store != null && !store.equals("all") && !store.trim().isEmpty()) ? store : null;
        String maChienDich = (campaign != null && !campaign.equals("all") && !campaign.trim().isEmpty()) ? campaign : null;
        String maGiaiThuong = (prizeParam != null && !prizeParam.equals("all") && !prizeParam.trim().isEmpty()) ? prizeParam : null;

        List<PrizeListDto> prizes = prizeRepository.getPrizeList();
        
        // Filter and sort prizes by Campaign
        java.util.stream.Stream<PrizeListDto> prizeStream = prizes.stream();
        if ("prizes".equals(tab) && maChienDich != null) {
            prizeStream = prizeStream.filter(p -> maChienDich.equals(p.getMaChienDich()));
        }
        prizes = prizeStream.sorted(java.util.Comparator
                .comparing(PrizeListDto::getTenChienDich, java.util.Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(PrizeListDto::getTenGiai, java.util.Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(java.util.stream.Collectors.toList());

        List<Campaign> campaigns = campaignRepository.findAll();
        List<Store> stores = storeRepository.findAll();
        List<StoreInventoryDto> allocations = storePrizeInventoryRepository.getStoreInventory(maStore, maChienDich, maGiaiThuong);
        List<CampaignStore> campaignStores = campaignStoreRepository.findAll();

        model.addAttribute("prizes", prizes);
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("stores", stores);
        model.addAttribute("allocations", allocations);
        model.addAttribute("campaignStores", campaignStores);
        model.addAttribute("activeTab", tab);
        
        // Return filter selections back to view
        model.addAttribute("selectedStore", store);
        model.addAttribute("selectedCampaign", campaign);
        model.addAttribute("selectedPrize", prizeParam);

        return "admin/prize-list";
    }

    @PostMapping("/allocate")
    public String allocatePrize(@RequestParam List<String> storeMas,
                                @RequestParam String maGiaiThuong,
                                @RequestParam int soLuongCap,
                                RedirectAttributes redirectAttributes) {
        try {
            if (storeMas == null || storeMas.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 cửa hàng.");
            }
            for (String maStore : storeMas) {
                prizeService.allocatePrizeToStore(maStore, maGiaiThuong, soLuongCap);
            }
            redirectAttributes.addFlashAttribute("successMsg", "Phân bổ thành công cho " + storeMas.size() + " cửa hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=allocations";
    }

    @PostMapping("/allocations/update")
    public String updateAllocation(@RequestParam String maStore,
                                   @RequestParam String maGiaiThuong,
                                   @RequestParam int newTongLuongCap,
                                   RedirectAttributes redirectAttributes) {
        try {
            prizeService.updateAllocation(maStore, maGiaiThuong, newTongLuongCap);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật số lượng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=allocations";
    }

    @PostMapping("/allocations/revoke")
    public String revokeAllocation(@RequestParam String maStore,
                                   @RequestParam String maGiaiThuong,
                                   RedirectAttributes redirectAttributes) {
        try {
            com.bitis.luckydraw.model.StorePrizeInventory inv = storePrizeInventoryRepository.findByMaStoreAndMaGiaiThuong(maStore, maGiaiThuong).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phân bổ"));
            prizeService.updateAllocation(maStore, maGiaiThuong, inv.getDaPhat());
            redirectAttributes.addFlashAttribute("successMsg", "Thu hồi thành công số lượng tồn kho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=allocations";
    }

    @PostMapping("/import")
    public String importPrizes(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Vui lòng chọn file Excel!");
            return "redirect:/admin/prizes";
        }
        try {
            List<com.bitis.luckydraw.model.Prize> prizesToImport = prizeExcelService.parseExcelFile(file);
            
            // Lấy tất cả mã chiến dịch đang có trong database để kiểm tra
            java.util.List<String> validCampaignCodes = campaignRepository.findAll().stream()
                .map(Campaign::getMaChienDich)
                .collect(java.util.stream.Collectors.toList());

            int countNew = 0;
            int countUpdate = 0;
            for (com.bitis.luckydraw.model.Prize p : prizesToImport) {
                // Kiểm tra ràng buộc: chiến dịch phải được tạo trước
                if (!validCampaignCodes.contains(p.getMaChienDich())) {
                    throw new RuntimeException("Mã chiến dịch '" + p.getMaChienDich() + "' (tại giải thưởng '" + p.getMaGiaiThuong() + "') chưa tồn tại. Vui lòng tạo chiến dịch trước khi import!");
                }
                
                java.util.Optional<com.bitis.luckydraw.model.Prize> existingOpt = prizeRepository.findByMaGiaiThuong(p.getMaGiaiThuong());
                if (existingOpt.isPresent()) {
                    com.bitis.luckydraw.model.Prize existing = existingOpt.get();
                    existing.setTenGiai(p.getTenGiai());
                    existing.setMaChienDich(p.getMaChienDich());
                    existing.setLoaiGiai(p.getLoaiGiai());
                    existing.setXacSuat(p.getXacSuat());
                    existing.setTonKhoToanHeThong(p.getTonKhoToanHeThong());
                    existing.setLaGiaiThuong(p.getLaGiaiThuong());
                    prizeRepository.save(existing);
                    countUpdate++;
                } else {
                    prizeRepository.save(p);
                    countNew++;
                }
            }
            redirectAttributes.addFlashAttribute("successMsg", "Import thành công: " + countNew + " giải mới, cập nhật " + countUpdate + " giải.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi import: " + e.getMessage());
        }
        return "redirect:/admin/prizes";
    }

    @GetMapping("/export-excel")
    public void exportExcel(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<com.bitis.luckydraw.dto.PrizeListDto> prizes = prizeRepository.getPrizeList();
            String[] headers = {"Mã Chiến Dịch", "Tên Chiến Dịch", "Mã Quà", "Tên Quà", "Xác Suất", "Loại Quà", "Là Quà Tặng?", "Tồn Kho Tổng", "Giới hạn/Người"};
            List<String[]> data = prizes.stream().map(p -> new String[]{
                p.getMaChienDich(),
                p.getTenChienDich() != null ? p.getTenChienDich() : "",
                p.getMaGiaiThuong(),
                p.getTenGiai(),
                String.valueOf(p.getXacSuat()),
                p.getLoaiGiai() != null && p.getLoaiGiai() == 1 ? "Hiện vật" : "Voucher",
                p.getLaGiaiThuong() != null && p.getLaGiaiThuong() ? "Có" : "Không (Giải trượt)",
                p.getTonKhoToanHeThong() != null ? (p.getTonKhoToanHeThong() == -1 ? "Không giới hạn" : String.valueOf(p.getTonKhoToanHeThong())) : "0",
                p.getGioiHanTrungMoiCustomer() != null ? String.valueOf(p.getGioiHanTrungMoiCustomer()) : "Không giới hạn"
            }).collect(java.util.stream.Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachQuaTang", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/save")
    public String savePrize(Prize prize, RedirectAttributes redirectAttributes) {
        try {
            Boolean laGiaiThuong = prize.getLaGiaiThuong();
            if (laGiaiThuong == null) {
                laGiaiThuong = false;
            }

            // Check if exists
            java.util.Optional<Prize> existingOpt = prizeRepository.findByMaGiaiThuong(prize.getMaGiaiThuong());
            if (existingOpt.isPresent()) {
                // Update
                Prize existing = existingOpt.get();
                existing.setTenGiai(prize.getTenGiai());
                existing.setLoaiGiai(prize.getLoaiGiai());
                existing.setGioiHanTrungMoiCustomer(prize.getGioiHanTrungMoiCustomer());
                existing.setMaChienDich(prize.getMaChienDich());
                existing.setTonKhoToanHeThong(prize.getTonKhoToanHeThong());
                existing.setXacSuat(prize.getXacSuat());
                existing.setLaGiaiThuong(laGiaiThuong);
                prizeRepository.save(existing);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giải thưởng thành công.");
            } else {
                // Create
                prize.setLaGiaiThuong(laGiaiThuong);
                prizeRepository.save(prize);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm giải thưởng mới thành công.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi lưu giải thưởng: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=prizes";
    }

    @PostMapping("/delete")
    public String deletePrize(@RequestParam("maGiaiThuong") String maGiaiThuong, RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Prize> prizeOpt = prizeRepository.findByMaGiaiThuong(maGiaiThuong);
            if (prizeOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giải thưởng.");
                return "redirect:/admin/prizes?tab=prizes";
            }
            // Simple constraint check
            java.util.Optional<com.bitis.luckydraw.model.StorePrizeInventory> invOpt = 
                storePrizeInventoryRepository.findAll().stream()
                .filter(inv -> inv.getMaGiaiThuong().equals(maGiaiThuong))
                .findFirst();
            if (invOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xoá giải thưởng đã được phân bổ cho cửa hàng. Bạn chỉ có thể thu hồi.");
            } else {
                prizeRepository.delete(prizeOpt.get());
                redirectAttributes.addFlashAttribute("successMessage", "Xoá giải thưởng thành công.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xoá giải thưởng: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=prizes";
    }
}
