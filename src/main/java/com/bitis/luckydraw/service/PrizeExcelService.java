package com.bitis.luckydraw.service;

import com.bitis.luckydraw.model.Prize;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class PrizeExcelService {

    public List<Prize> parseExcelFile(MultipartFile file) {
        List<Prize> prizes = new ArrayList<>();
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

                Prize prize = new Prize();

                for (int cellIdx = 0; cellIdx < 9; cellIdx++) {
                    Cell currentCell = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                    switch (cellIdx) {
                        case 0: // Mã Chiến Dịch
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setMaChienDich(String.valueOf((long) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                prize.setMaChienDich(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 1: // Tên Chiến Dịch (bỏ qua, chỉ để hiển thị)
                            break;
                        case 2: // Mã Quà
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setMaGiaiThuong(String.valueOf((long) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                prize.setMaGiaiThuong(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 3: // Tên Quà
                            if (currentCell.getCellType() == CellType.STRING) {
                                prize.setTenGiai(currentCell.getStringCellValue().trim());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setTenGiai(String.valueOf(currentCell.getNumericCellValue()));
                            }
                            break;
                        case 4: // Xác Suất
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setXacSuat(currentCell.getNumericCellValue());
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                try { prize.setXacSuat(Double.parseDouble(currentCell.getStringCellValue().trim())); } catch(Exception e){ prize.setXacSuat(0.0); }
                            } else {
                                prize.setXacSuat(0.0);
                            }
                            break;
                        case 5: // Loại Quà
                            if (currentCell.getCellType() == CellType.STRING) {
                                String val = currentCell.getStringCellValue().trim();
                                if (val.equalsIgnoreCase("Voucher")) prize.setLoaiGiai(0);
                                else prize.setLoaiGiai(1); // Mặc định là Hiện vật
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setLoaiGiai((int) currentCell.getNumericCellValue());
                            } else {
                                prize.setLoaiGiai(1); // Default
                            }
                            break;
                        case 6: // Là Quà Tặng?
                            if (currentCell.getCellType() == CellType.STRING) {
                                String val = currentCell.getStringCellValue().trim();
                                prize.setLaGiaiThuong(val.equalsIgnoreCase("Có") || val.equalsIgnoreCase("Co") || val.equalsIgnoreCase("Yes") || val.equalsIgnoreCase("1") || val.equalsIgnoreCase("true"));
                            } else if (currentCell.getCellType() == CellType.BOOLEAN) {
                                prize.setLaGiaiThuong(currentCell.getBooleanCellValue());
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setLaGiaiThuong(currentCell.getNumericCellValue() > 0);
                            } else {
                                prize.setLaGiaiThuong(true); // Default
                            }
                            break;
                        case 7: // Tồn Kho Tổng
                            if (currentCell.getCellType() == CellType.STRING) {
                                String val = currentCell.getStringCellValue().trim();
                                if (val.equalsIgnoreCase("Không giới hạn") || val.isEmpty()) {
                                    prize.setTonKhoToanHeThong(-1);
                                } else {
                                    try { prize.setTonKhoToanHeThong(Integer.parseInt(val)); } catch(Exception e){ prize.setTonKhoToanHeThong(0); }
                                }
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setTonKhoToanHeThong((int) currentCell.getNumericCellValue());
                            } else {
                                prize.setTonKhoToanHeThong(0);
                            }
                            break;
                        case 8: // Giới hạn/Người
                            if (currentCell.getCellType() == CellType.STRING) {
                                String val = currentCell.getStringCellValue().trim();
                                if (val.equalsIgnoreCase("Không giới hạn") || val.isEmpty()) {
                                    prize.setGioiHanTrungMoiCustomer(null);
                                } else {
                                    try { prize.setGioiHanTrungMoiCustomer(Integer.parseInt(val)); } catch(Exception e){}
                                }
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setGioiHanTrungMoiCustomer((int) currentCell.getNumericCellValue());
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (prize.getMaGiaiThuong() != null && !prize.getMaGiaiThuong().trim().isEmpty() &&
                    prize.getMaChienDich() != null && !prize.getMaChienDich().trim().isEmpty()) {
                    prizes.add(prize);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage());
        }
        return prizes;
    }
}
