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

                Iterator<Cell> cellsInRow = currentRow.iterator();
                int cellIdx = 0;
                while (cellsInRow.hasNext() && cellIdx < 7) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0: // MaGiaiThuong
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setMaGiaiThuong(String.valueOf((int) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                prize.setMaGiaiThuong(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 1: // TenGiai
                            if (currentCell.getCellType() == CellType.STRING) {
                                prize.setTenGiai(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 2: // MaChienDich
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setMaChienDich(String.valueOf((int) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                prize.setMaChienDich(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 3: // LoaiGiai
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setLoaiGiai((int) currentCell.getNumericCellValue());
                            }
                            break;
                        case 4: // XacSuat
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setXacSuat(currentCell.getNumericCellValue());
                            }
                            break;
                        case 5: // TonKhoToanHeThong
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setTonKhoToanHeThong((int) currentCell.getNumericCellValue());
                            }
                            break;
                        case 6: // LaGiaiThuong
                            if (currentCell.getCellType() == CellType.BOOLEAN) {
                                prize.setLaGiaiThuong(currentCell.getBooleanCellValue());
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                prize.setLaGiaiThuong(Boolean.parseBoolean(currentCell.getStringCellValue().trim()));
                            } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                prize.setLaGiaiThuong(currentCell.getNumericCellValue() > 0);
                            }
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
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
