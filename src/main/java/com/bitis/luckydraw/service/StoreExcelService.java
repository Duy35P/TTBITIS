package com.bitis.luckydraw.service;

import com.bitis.luckydraw.model.Store;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class StoreExcelService {

    public List<Store> parseExcelFile(MultipartFile file) {
        List<Store> stores = new ArrayList<>();
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

                Iterator<Cell> cellsInRow = currentRow.iterator();
                Store store = new Store();
                store.setTrangThai(0); // Trạng thái mặc định là tạm ngưng khi import

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0: // MaStore
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                store.setMaStore(String.valueOf((int) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                store.setMaStore(currentCell.getStringCellValue().trim());
                            }
                            break;
                        case 1: // TenStore
                            store.setTenCuaHang(currentCell.getStringCellValue());
                            break;
                        case 2: // Diachi
                            store.setDiaChiStore(currentCell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                
                if (store.getMaStore() != null && !store.getMaStore().trim().isEmpty() && 
                    store.getTenCuaHang() != null && !store.getTenCuaHang().trim().isEmpty()) {
                    stores.add(store);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage());
        }
        return stores;
    }
}
