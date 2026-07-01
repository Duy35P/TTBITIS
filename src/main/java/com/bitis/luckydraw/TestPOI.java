package com.bitis.luckydraw;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestPOI {
    public static void main(String[] args) {
        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            System.out.println("XSSFWorkbook instantiated successfully.");
            wb.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
