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

import com.bitis.luckydraw.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/quanly/prizes")
public class AdminPrizeController {

    private final PrizeRepository prizeRepository;
    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final StorePrizeInventoryRepository storePrizeInventoryRepository;
    private final PrizeService prizeService;
    private final PrizeExcelService prizeExcelService;
    private final com.bitis.luckydraw.repository.PrizeCodeRepository prizeCodeRepository;
    private final CampaignStoreRepository campaignStoreRepository;

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
            Model model,
            org.springframework.security.core.Authentication auth) {
            
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = false;
        boolean hasQlGiaiThuong = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("QL_GIAITHUONG"));
        
        String maStore = (store != null && !store.equals("all") && !store.trim().isEmpty()) ? store : null;
        
        if (!hasQlGiaiThuong && !isAdmin && "prizes".equals(tab)) {
            tab = "allocations";
        }
        
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        
        String maChienDich = (campaign != null && !campaign.equals("all") && !campaign.trim().isEmpty()) ? campaign : null;
        String maGiaiThuong = (prizeParam != null && !prizeParam.equals("all") && !prizeParam.trim().isEmpty()) ? prizeParam : null;

        List<PrizeListDto> prizes = prizeRepository.getPrizeList();
        
        List<PrizeListDto> allPrizes = prizeRepository.getPrizeList().stream().sorted(java.util.Comparator
                .comparing(PrizeListDto::getTenChienDich, java.util.Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(PrizeListDto::getTenGiai, java.util.Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("allPrizes", allPrizes);
        
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
        List<StoreInventoryDto> rawAllocations = storePrizeInventoryRepository.getStoreInventory(maStore, maChienDich, maGiaiThuong);
        List<StoreInventoryDto> allocations = rawAllocations;

        // LÀM ĐƠN GIẢN: Filter allocations based on user details
        if (!isAdmin && auth.getPrincipal() instanceof com.bitis.luckydraw.security.CustomUserDetails) {
            com.bitis.luckydraw.security.CustomUserDetails userDetails = (com.bitis.luckydraw.security.CustomUserDetails) auth.getPrincipal();
            if (userDetails.getAssignedStores() != null && !userDetails.getAssignedStores().isEmpty()) {
                allocations = rawAllocations.stream()
                    .filter(a -> userDetails.getAssignedStores().contains(a.getMaStore()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (userDetails.getMaStore() != null) {
                allocations = rawAllocations.stream()
                    .filter(a -> userDetails.getMaStore().equals(a.getMaStore()))
                    .collect(java.util.stream.Collectors.toList());
            }
        }

        if (!isAdmin) {
            java.util.Set<String> validCampaigns = allocations.stream().map(StoreInventoryDto::getMaChienDich).collect(java.util.stream.Collectors.toSet());
            prizes = prizes.stream().filter(p -> validCampaigns.contains(p.getMaChienDich())).collect(java.util.stream.Collectors.toList());
        }
        List<Store> stores = storeRepository.findAll();
        if (!isAdmin && auth.getPrincipal() instanceof com.bitis.luckydraw.security.CustomUserDetails) {
            com.bitis.luckydraw.security.CustomUserDetails userDetails = (com.bitis.luckydraw.security.CustomUserDetails) auth.getPrincipal();
            if (userDetails.getAssignedStores() != null && !userDetails.getAssignedStores().isEmpty()) {
                stores = stores.stream()
                    .filter(s -> userDetails.getAssignedStores().contains(s.getMaStore()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (userDetails.getMaStore() != null) {
                stores = stores.stream()
                    .filter(s -> userDetails.getMaStore().equals(s.getMaStore()))
                    .collect(java.util.stream.Collectors.toList());
            }
        }
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

        return "quanly/prize-list";
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_PHANBO_ADD')")
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
            String msg = e.getMessage();
            if (msg != null && (msg.toLowerCase().contains("tồn kho") || msg.contains("Tá»“n kho") || msg.contains("sp_AllocatePrizeToStore"))) {
                redirectAttributes.addFlashAttribute("errorMessage", "SỐ LƯỢNG KHÔNG ĐỦ ĐỂ PHÂN BỔ");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + msg);
            }
        }
        return "redirect:/quanly/prizes?tab=allocations";
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_PHANBO_EDIT')")

    @PostMapping("/allocations/update")
    public String updateAllocation(@RequestParam String maStore,
                                   @RequestParam String maGiaiThuong,
                                   @RequestParam int newTongLuongCap,
                                   RedirectAttributes redirectAttributes) {
        try {
            prizeService.updateAllocation(maStore, maGiaiThuong, newTongLuongCap);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số lượng thành công!");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.toLowerCase().contains("tồn kho") || msg.contains("Tá»“n kho") || msg.contains("sp_AllocatePrizeToStore"))) {
                redirectAttributes.addFlashAttribute("errorMessage", "SỐ LƯỢNG KHÔNG ĐỦ ĐỂ PHÂN BỔ");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + msg);
            }
        }
        return "redirect:/quanly/prizes?tab=allocations";
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_PHANBO_REVOKE')")

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
        return "redirect:/quanly/prizes?tab=allocations";
    }


    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_GIAITHUONG_IMPORT')")

    @PostMapping("/import")
    public String importPrizes(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel!");
            return "redirect:/quanly/prizes";
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
                
                System.out.println("[DEBUG IMPORT] Prize: " + p.getMaGiaiThuong() + " | laGiaiThuong=" + p.getLaGiaiThuong() + " | tonKho=" + p.getTonKhoToanHeThong());
                
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
        return "redirect:/quanly/prizes";
    }

    @PostMapping("/import-codes-excel")
    public String importCodesExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn file Excel!");
            }
            int successCount = 0;
            int duplicateCount = 0;
            java.util.Set<String> affectedPrizes = new java.util.HashSet<>();
            try (org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                for (org.apache.poi.ss.usermodel.Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Skip header
                    
                    org.apache.poi.ss.usermodel.Cell cellMaGiai = row.getCell(0);
                    org.apache.poi.ss.usermodel.Cell cellCode = row.getCell(1);
                    org.apache.poi.ss.usermodel.Cell cellNgayTao = row.getCell(2);
                    org.apache.poi.ss.usermodel.Cell cellNgayHetHan = row.getCell(3);
                    
                    if (cellMaGiai == null || cellCode == null) continue;
                    
                    String maGiaiThuong = getCellValueAsString(cellMaGiai).trim();
                    String code = getCellValueAsString(cellCode).trim();
                    
                    if (maGiaiThuong.isEmpty() || code.isEmpty()) continue;
                    
                    com.bitis.luckydraw.model.PrizeCode prizeCode = prizeCodeRepository.findByCode(code).orElse(new com.bitis.luckydraw.model.PrizeCode());
                    prizeCode.setMaGiaiThuong(maGiaiThuong);
                    prizeCode.setCode(code);
                    if (prizeCode.getId() == null) prizeCode.setIsUsed(false);
                    
                    java.time.LocalDateTime ngayTao = parseExcelDate(cellNgayTao);
                    if (ngayTao != null) prizeCode.setNgayTao(ngayTao);
                    else prizeCode.setNgayTao(java.time.LocalDateTime.now());
                    
                    prizeCode.setNgayHetHan(parseExcelDate(cellNgayHetHan));
                    
                    prizeCodeRepository.save(prizeCode);
                    affectedPrizes.add(maGiaiThuong);
                    successCount++;
                }
            }
            
            for (String maGiai : affectedPrizes) {
                syncPrizeInventory(maGiai);
            }
            
            String msg = "Đã nạp " + successCount + " mã thành công từ file Excel.";
            if (duplicateCount > 0) {
                msg += " Đã bỏ qua " + duplicateCount + " mã trùng lặp.";
            }
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi import: " + e.getMessage());
        }
        return "redirect:/quanly/prizes";
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            return String.valueOf((long)cell.getNumericCellValue());
        }
        return "";
    }

    private java.time.LocalDateTime parseExcelDate(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        } else {
            String str = getCellValueAsString(cell).trim();
            if (str.isEmpty()) return null;
            try {
                if (str.contains(":")) {
                    return java.time.LocalDateTime.parse(str, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                } else {
                    return java.time.LocalDate.parse(str, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
                }
            } catch (Exception e) {
                return null; // Ignore parse error, return null ponytail style
            }
        }
    }
    
    @GetMapping("/codes/{maGiaiThuong}")
    public String viewCodes(@org.springframework.web.bind.annotation.PathVariable String maGiaiThuong, org.springframework.ui.Model model, @RequestParam(defaultValue = "0") int page) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 50, org.springframework.data.domain.Sort.by("id").descending());
        org.springframework.data.domain.Page<com.bitis.luckydraw.model.PrizeCode> codesPage = prizeCodeRepository.findByMaGiaiThuong(maGiaiThuong, pageable);
        model.addAttribute("codesPage", codesPage);
        model.addAttribute("maGiaiThuong", maGiaiThuong);
        prizeRepository.findByMaGiaiThuong(maGiaiThuong).ifPresent(p -> model.addAttribute("prizeName", p.getTenGiai()));
        return "quanly/prize-code-list";
    }



    private void syncPrizeInventory(String maGiaiThuong) {
        prizeRepository.findByMaGiaiThuong(maGiaiThuong).ifPresent(p -> {
            if (Boolean.TRUE.equals(p.getLaGiaiThuong())) {
                long totalUnusedCodes = prizeCodeRepository.countByMaGiaiThuongAndIsUsed(maGiaiThuong, false);
                Long sumAllocated = storePrizeInventoryRepository.sumTonKhoByMaGiaiThuong(maGiaiThuong);
                int totalAllocated = sumAllocated != null ? sumAllocated.intValue() : 0;
                
                int remainingGlobal = (int) totalUnusedCodes - totalAllocated;
                if (remainingGlobal < 0) remainingGlobal = 0; // Prevent negative inventory just in case
                
                p.setTonKhoToanHeThong(remainingGlobal);
                prizeRepository.save(p);
            }
        });
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_GIAITHUONG_CODE_EXPORT')")
    @GetMapping("/codes/export-excel")
    public void exportCodesExcel(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<com.bitis.luckydraw.model.PrizeCode> codes = prizeCodeRepository.findAll();
            String[] headers = {"Mã Giải Thưởng", "Code", "Trạng Thái", "Ngày Tạo", "Ngày Hết Hạn"};
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            List<String[]> data = codes.stream().map(c -> new String[]{
                c.getMaGiaiThuong(),
                c.getCode(),
                c.getIsUsed() != null && c.getIsUsed() ? "Đã cấp" : "Chưa phát",
                c.getNgayTao() != null ? c.getNgayTao().format(formatter) : "",
                c.getNgayHetHan() != null ? c.getNgayHetHan().format(formatter) : ""
            }).collect(java.util.stream.Collectors.toList());
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachCodeQuaTang", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_GIAITHUONG_EXPORT')")
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_GIAITHUONG_ADD') or hasAuthority('ACT_GIAITHUONG_EDIT')")

    @PostMapping("/save")
    public String savePrize(Prize prize, RedirectAttributes redirectAttributes) {
        try {
            Boolean laGiaiThuong = prize.getLaGiaiThuong();
            if (laGiaiThuong == null) {
                laGiaiThuong = false;
            }

            if (laGiaiThuong && prize.getTonKhoToanHeThong() != null && prize.getTonKhoToanHeThong() < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Giải thưởng là quà tặng thật không thể có số lượng vô hạn (-1). Vui lòng nhập số lượng >= 0.");
                return "redirect:/quanly/prizes?tab=prizes";
            }

            // Ponytail logic: Kiểm tra tổng tỷ lệ trúng thưởng & Tự động tính giải trượt
            List<Prize> existingPrizes = prizeRepository.findByMaChienDich(prize.getMaChienDich());
            
            // 1. Chỉ cho phép 1 giải trượt
            if (!laGiaiThuong) {
                long countTruot = existingPrizes.stream().filter(p -> !Boolean.TRUE.equals(p.getLaGiaiThuong()) && !p.getMaGiaiThuong().equals(prize.getMaGiaiThuong())).count();
                if (countTruot > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Mỗi chiến dịch chỉ được phép có TỐI ĐA 1 giải trượt!");
                    return "redirect:/quanly/prizes?tab=prizes";
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
                    return "redirect:/quanly/prizes?tab=prizes";
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
                prizeRepository.save(existing);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giải thưởng thành công. Giải trượt đã được tự động cập nhật!");
            } else {
                // Create
                prize.setLaGiaiThuong(laGiaiThuong);
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
        return "redirect:/quanly/prizes?tab=prizes";
    }

    @GetMapping("/bulk-rates")
    public String showBulkRatesPage(@RequestParam("campaignId") String campaignId, org.springframework.ui.Model model) {
        List<Prize> prizes = prizeRepository.findByMaChienDich(campaignId);
        com.bitis.luckydraw.model.Campaign campaign = campaignRepository.findByMaChienDich(campaignId).orElse(null);
        model.addAttribute("prizes", prizes);
        model.addAttribute("campaign", campaign);
        model.addAttribute("campaignId", campaignId);
        return "quanly/bulk-rates";
    }

    @PostMapping("/bulk-rates/save")
    public String saveBulkRates(@RequestParam(value = "ids", required = false) String[] ids,
                                @RequestParam(value = "rates", required = false) Double[] rates,
                                @RequestParam("campaignId") String campaignId,
                                RedirectAttributes redirectAttributes) {
        if (ids != null && rates != null && ids.length == rates.length) {
            double totalReal = 0;
            // First pass: validation
            for (int i = 0; i < ids.length; i++) {
                Prize p = prizeRepository.findByMaGiaiThuong(ids[i]).orElse(null);
                if (p != null && Boolean.TRUE.equals(p.getLaGiaiThuong())) {
                    totalReal += (rates[i] != null ? rates[i] : 0);
                }
            }
            if (totalReal > 100) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Tổng tỉ lệ các giải thưởng thực tế vượt quá 100% (" + totalReal + "%). Vui lòng điều chỉnh lại.");
                return "redirect:/quanly/prizes/bulk-rates?campaignId=" + campaignId;
            }

            // Second pass: save
            for (int i = 0; i < ids.length; i++) {
                Prize p = prizeRepository.findByMaGiaiThuong(ids[i]).orElse(null);
                if (p != null) {
                    p.setXacSuat(rates[i] != null ? rates[i] : 0.0);
                    prizeRepository.save(p);
                }
            }
            prizeRepository.recalibrateDummyPrize(campaignId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tỉ lệ hàng loạt thành công!");
        }
        return "redirect:/quanly/prizes/bulk-rates?campaignId=" + campaignId;
    }

    @PostMapping("/delete")
    public String deletePrize(@RequestParam("maGiaiThuong") String maGiaiThuong, RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Prize> prizeOpt = prizeRepository.findByMaGiaiThuong(maGiaiThuong);
            if (prizeOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giải thưởng.");
                return "redirect:/quanly/prizes?tab=prizes";
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
        return "redirect:/quanly/prizes?tab=prizes";
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_PHANBO_EXPORT')")
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
            
            boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (!isAdmin && principal instanceof com.bitis.luckydraw.security.CustomUserDetails) {
                com.bitis.luckydraw.security.CustomUserDetails userDetails = (com.bitis.luckydraw.security.CustomUserDetails) principal;
                if (userDetails.getAssignedStores() != null && !userDetails.getAssignedStores().isEmpty()) {
                    allocations = allocations.stream()
                        .filter(a -> userDetails.getAssignedStores().contains(a.getMaStore()))
                        .collect(java.util.stream.Collectors.toList());
                } else if (userDetails.getMaStore() != null) {
                    allocations = allocations.stream()
                        .filter(a -> userDetails.getMaStore().equals(a.getMaStore()))
                        .collect(java.util.stream.Collectors.toList());
                }
            }

            
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
            return "redirect:/quanly/prizes?tab=allocations";
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
        return "redirect:/quanly/prizes?tab=allocations";
    }
}
