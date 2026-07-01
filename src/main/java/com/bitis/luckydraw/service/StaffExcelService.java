package com.bitis.luckydraw.service;

import com.bitis.luckydraw.model.Staff;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service
public class StaffExcelService {

    public List<Staff> parseExcelFile(MultipartFile file) {
        List<Staff> staffs = new ArrayList<>();
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

                Staff staff = new Staff();
                staff.setMaNhanVien("NV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                
                Iterator<Cell> cellsInRow = currentRow.iterator();
                int cellIdx = 0;
                while (cellsInRow.hasNext() && cellIdx < 5) {
                    Cell currentCell = cellsInRow.next();
                    String cellValue = "";
                    if (currentCell.getCellType() == CellType.STRING) {
                        cellValue = currentCell.getStringCellValue().trim();
                    } else if (currentCell.getCellType() == CellType.NUMERIC) {
                        cellValue = String.valueOf((long) currentCell.getNumericCellValue());
                    }

                    switch (cellIdx) {
                        case 0: // Username
                            staff.setUsername(cellValue);
                            break;
                        case 1: // Tên nhân viên
                            staff.setTenNhanVien(cellValue);
                            break;
                        case 2: // Role ID
                            if (cellValue.equalsIgnoreCase("admin") || cellValue.equalsIgnoreCase("ROLE_ADMIN")) {
                                staff.setRoleId("ADMIN");
                            } else {
                                staff.setRoleId(cellValue.toUpperCase());
                            }
                            break;
                        case 3: // Mã Store
                            staff.setMaStore(cellValue);
                            break;
                        case 4: // Trạng Thái
                            if (cellValue.equals("1") || cellValue.equalsIgnoreCase("Hoạt động") || cellValue.equalsIgnoreCase("True")) {
                                staff.setTrangThai(1);
                            } else {
                                staff.setTrangThai(0);
                            }
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }

                if (staff.getUsername() != null && !staff.getUsername().isEmpty() && 
                    staff.getTenNhanVien() != null && !staff.getTenNhanVien().isEmpty() &&
                    staff.getRoleId() != null && !staff.getRoleId().isEmpty()) {
                    staffs.add(staff);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel nhân viên: " + e.getMessage());
        }
        return staffs;
    }
}
