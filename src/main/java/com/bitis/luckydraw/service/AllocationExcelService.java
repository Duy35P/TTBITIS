package com.bitis.luckydraw.service;

import com.bitis.luckydraw.model.StorePrizeInventory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class AllocationExcelService {

    public List<StorePrizeInventory> parseExcelFile(MultipartFile file) {
        List<StorePrizeInventory> allocations = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                StorePrizeInventory allocation = new StorePrizeInventory();

                for (int cellIdx = 0; cellIdx < 7; cellIdx++) {
                    Cell currentCell = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                    switch (cellIdx) {
                        case 0: // Mã Cửa Hàng
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                allocation.setMaStore(String.valueOf((long) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                allocation.setMaStore(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 2: // Mã Giải Thưởng
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                allocation.setMaGiaiThuong(String.valueOf((long) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                allocation.setMaGiaiThuong(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 6: // Số Lượng Phân Bổ (Tổng Cấp)
                            if (currentCell.getCellType() == CellType.STRING) {
                                String val = currentCell.getStringCellValue().trim();
                                if (val.equalsIgnoreCase("Không giới hạn") || val.isEmpty()) {
                                    allocation.setTongLuongCap(-1);
                                } else {
                                    try { allocation.setTongLuongCap(Integer.parseInt(val)); } catch(Exception e){ allocation.setTongLuongCap(0); }
                                }
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                allocation.setTongLuongCap((int) currentCell.getNumericCellValue());
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (allocation.getMaStore() != null && !allocation.getMaStore().isEmpty() &&
                    allocation.getMaGiaiThuong() != null && !allocation.getMaGiaiThuong().isEmpty() &&
                    allocation.getTongLuongCap() > 0) {
                    allocations.add(allocation);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage());
        }
        return allocations;
    }
}
