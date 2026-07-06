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
    private final com.bitis.luckydraw.repository.PrizeCodeRepository prizeCodeRepository;

    public AdminPrizeController(CampaignRepository campaignRepository,
                                PrizeRepository prizeRepository,
                                StoreRepository storeRepository,
                                StorePrizeInventoryRepository storePrizeInventoryRepository,
                                PrizeService prizeService,
                                CampaignStoreRepository campaignStoreRepository,
                                PrizeExcelService prizeExcelService,
                                com.bitis.luckydraw.repository.PrizeCodeRepository prizeCodeRepository) {
        this.campaignRepository = campaignRepository;
        this.prizeRepository = prizeRepository;
        this.storeRepository = storeRepository;
        this.storePrizeInventoryRepository = storePrizeInventoryRepository;
        this.prizeService = prizeService;
        this.campaignStoreRepository = campaignStoreRepository;
        this.prizeExcelService = prizeExcelService;
        this.prizeCodeRepository = prizeCodeRepository;
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

        // Calculate unused code count
        java.util.Map<String, Long> codeCountMap = new java.util.HashMap<>();
        for (PrizeListDto p : prizes) {
            long c = prizeCodeRepository.countByMaGiaiThuongAndIsUsed(p.getMaGiaiThuong(), false);
            codeCountMap.put(p.getMaGiaiThuong(), c);
        }

        model.addAttribute("prizes", prizes);
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("stores", stores);
        model.addAttribute("allocations", allocations);
        model.addAttribute("campaignStores", campaignStores);
        model.addAttribute("codeCountMap", codeCountMap);
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
            redirectAttributes.addFlashAttribute("successMessage", "Phân bổ thành công cho " + storeMas.size() + " cửa hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số lượng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("successMessage", "Thu hồi thành công số lượng tồn kho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=allocations";
    }

    @PostMapping("/allocations/reclaim-unredeemed")
    public String reclaimUnredeemed(@RequestParam(required = false) String maChienDich, RedirectAttributes redirectAttributes) {
        try {
            prizeService.reclaimUnredeemedVouchers(maChienDich);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thu hồi thành công các quà chưa đổi thuộc chiến dịch hết hạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thu hồi: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=allocations";
    }

    @PostMapping("/import")
    public String importPrizes(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel!");
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
                
                // Kiểm tra ràng buộc: quà tặng thật không thể có số lượng -1
                if (Boolean.TRUE.equals(p.getLaGiaiThuong()) && p.getTonKhoToanHeThong() != null && p.getTonKhoToanHeThong() < 0) {
                    throw new RuntimeException("Giải thưởng '" + p.getMaGiaiThuong() + "' là quà tặng thật nên không thể có số lượng vô hạn (-1). Vui lòng nhập số lượng >= 0.");
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
            redirectAttributes.addFlashAttribute("successMessage", "Import thành công: " + countNew + " giải mới, cập nhật " + countUpdate + " giải.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi import: " + e.getMessage());
        }
        return "redirect:/admin/prizes";
    }

    @PostMapping("/import-codes")
    public String importCodes(@RequestParam("maGiaiThuong") String maGiaiThuong, 
                              @RequestParam("codesText") String codesText, 
                              RedirectAttributes redirectAttributes) {
        try {
            if (codesText == null || codesText.trim().isEmpty()) {
                throw new IllegalArgumentException("Danh sách mã trống!");
            }
            // Parse by newline or comma
            String[] codes = codesText.split("[\r\n,]+");
            int successCount = 0;
            int duplicateCount = 0;
            for (String code : codes) {
                code = code.trim();
                if (code.isEmpty()) continue;
                
                if (prizeCodeRepository.existsByCode(code)) {
                    duplicateCount++;
                    continue;
                }
                com.bitis.luckydraw.model.PrizeCode prizeCode = new com.bitis.luckydraw.model.PrizeCode();
                prizeCode.setMaGiaiThuong(maGiaiThuong);
                prizeCode.setCode(code);
                prizeCode.setIsUsed(false);
                prizeCodeRepository.save(prizeCode);
                successCount++;
            }
            
            String msg = "Đã nạp " + successCount + " mã thành công.";
            if (duplicateCount > 0) {
                msg += " Đã bỏ qua " + duplicateCount + " mã trùng lặp.";
            }
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nạp mã: " + e.getMessage());
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

            if (laGiaiThuong && prize.getTonKhoToanHeThong() != null && prize.getTonKhoToanHeThong() < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Giải thưởng là quà tặng thật không thể có số lượng vô hạn (-1). Vui lòng nhập số lượng >= 0.");
                return "redirect:/admin/prizes?tab=prizes";
            }

            // Ponytail logic: Kiểm tra tổng tỷ lệ trúng thưởng & Tự động tính giải trượt
            List<Prize> existingPrizes = prizeRepository.findByMaChienDich(prize.getMaChienDich());
            
            // 1. Chỉ cho phép 1 giải trượt
            if (!laGiaiThuong) {
                long countTruot = existingPrizes.stream().filter(p -> !Boolean.TRUE.equals(p.getLaGiaiThuong()) && !p.getMaGiaiThuong().equals(prize.getMaGiaiThuong())).count();
                if (countTruot > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Mỗi chiến dịch chỉ được phép có TỐI ĐA 1 giải trượt!");
                    return "redirect:/admin/prizes?tab=prizes";
                }
            }

            // 2. Tính tổng tỷ lệ các giải thật
            double totalReal = 0;
            for (Prize p : existingPrizes) {
                if (Boolean.TRUE.equals(p.getLaGiaiThuong()) && !p.getMaGiaiThuong().equals(prize.getMaGiaiThuong())) {
                    totalReal += (p.getXacSuat() != null ? p.getXacSuat() : 0);
                }
            }

            if (laGiaiThuong) {
                totalReal += (prize.getXacSuat() != null ? prize.getXacSuat() : 0);
                if (totalReal > 100) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Tổng tỷ lệ trúng thưởng của các giải thật đã vượt quá 100% (" + totalReal + "%). Vui lòng giảm bớt!");
                    return "redirect:/admin/prizes?tab=prizes";
                }
            } else {
                // Tự động ép tỷ lệ giải trượt = 100 - tổng giải thật
                prize.setXacSuat(100.0 - totalReal);
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
                existing.setIsPreGeneratedCode(prize.getIsPreGeneratedCode() != null ? prize.getIsPreGeneratedCode() : false);
                prizeRepository.save(existing);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giải thưởng thành công. Giải trượt đã được tự động cập nhật!");
            } else {
                // Create
                prize.setLaGiaiThuong(laGiaiThuong);
                if (prize.getIsPreGeneratedCode() == null) prize.setIsPreGeneratedCode(false);
                prizeRepository.save(prize);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm giải thưởng mới thành công. Giải trượt đã được tự động cập nhật!");
            }

            // 3. Tự động cập nhật lại tỷ lệ giải trượt nếu vừa sửa giải thật
            if (laGiaiThuong) {
                prizeRepository.recalibrateDummyPrize(prize.getMaChienDich());
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
            Prize prizeToDelete = prizeOpt.get();
            // Simple constraint check
            java.util.Optional<com.bitis.luckydraw.model.StorePrizeInventory> invOpt = 
                storePrizeInventoryRepository.findAll().stream()
                .filter(inv -> inv.getMaGiaiThuong().equals(maGiaiThuong))
                .findFirst();
            if (invOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xoá giải thưởng đã được phân bổ cho cửa hàng. Bạn chỉ có thể thu hồi.");
            } else {
                prizeRepository.delete(prizeToDelete);
                
                // Tự động cập nhật lại tỷ lệ giải trượt nếu vừa xóa giải thật
                if (Boolean.TRUE.equals(prizeToDelete.getLaGiaiThuong())) {
                    prizeRepository.recalibrateDummyPrize(prizeToDelete.getMaChienDich());
                    redirectAttributes.addFlashAttribute("successMessage", "Xoá giải thưởng thành công. Giải trượt đã được tự động cập nhật!");
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", "Xoá giải thưởng thành công.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xoá giải thưởng: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=prizes";
    }

    @GetMapping("/allocations/export-excel")
    public void exportAllocationsExcel(
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "campaign", required = false) String campaign,
            @RequestParam(name = "prize", required = false) String prizeParam,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<StoreInventoryDto> allocations = storePrizeInventoryRepository.getStoreInventory(
                    (store != null && !store.isEmpty()) ? store : null,
                    (campaign != null && !campaign.isEmpty()) ? campaign : null,
                    (prizeParam != null && !prizeParam.isEmpty()) ? prizeParam : null
            );
            
            String[] headers = {"Mã Cửa Hàng", "Tên Cửa Hàng", "Mã Quà", "Tên Quà", "Mã Chiến Dịch", "Tên Chiến Dịch", "Tổng Cấp", "Đã Phát", "Tồn Kho"};
            List<String[]> data = allocations.stream().map(a -> new String[]{
                a.getMaStore(),
                a.getTenCuaHang(),
                a.getMaGiaiThuong(),
                a.getTenGiai(),
                a.getMaChienDich(),
                a.getTenChienDich(),
                a.getTongLuongCap() == -1 ? "Không giới hạn" : String.valueOf(a.getTongLuongCap()),
                String.valueOf(a.getDaPhat()),
                a.getTonKho() == -1 ? "Không giới hạn" : String.valueOf(a.getTonKho())
            }).collect(java.util.stream.Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "PhanBoCuaHang", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/allocations/import")
    public String importAllocations(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel!");
            return "redirect:/admin/prizes?tab=allocations";
        }
        try {
            // Because we didn't wire the bean via constructor (to avoid changing signature too much), we fetch it from application context or just instantiate it. Wait, no, we must wire it or just instantiate it since it has no dependencies!
            com.bitis.luckydraw.service.AllocationExcelService allocationExcelService = new com.bitis.luckydraw.service.AllocationExcelService();
            List<com.bitis.luckydraw.model.StorePrizeInventory> allocsToImport = allocationExcelService.parseExcelFile(file);
            
            int countSuccess = 0;
            int countError = 0;
            StringBuilder errors = new StringBuilder();
            
            for (com.bitis.luckydraw.model.StorePrizeInventory alloc : allocsToImport) {
                try {
                    // Cập nhật phân bổ (Nếu chưa có thì nó tự add)
                    java.util.Optional<com.bitis.luckydraw.model.StorePrizeInventory> existingOpt = storePrizeInventoryRepository.findByMaStoreAndMaGiaiThuong(alloc.getMaStore(), alloc.getMaGiaiThuong());
                    if (existingOpt.isPresent()) {
                        prizeService.updateAllocation(alloc.getMaStore(), alloc.getMaGiaiThuong(), alloc.getTongLuongCap());
                    } else {
                        prizeService.allocatePrizeToStore(alloc.getMaStore(), alloc.getMaGiaiThuong(), alloc.getTongLuongCap());
                    }
                    countSuccess++;
                } catch (Exception ex) {
                    countError++;
                    errors.append(" Lỗi dòng CH ").append(alloc.getMaStore()).append(" Quà ").append(alloc.getMaGiaiThuong()).append(": ").append(ex.getMessage()).append(";");
                }
            }
            
            if (countError > 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Import thành công " + countSuccess + ". Lỗi " + countError + ": " + errors.toString());
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Import thành công " + countSuccess + " phân bổ.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi import: " + e.getMessage());
        }
        return "redirect:/admin/prizes?tab=allocations";
    }
}
