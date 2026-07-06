package com.bitis.luckydraw.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceListDto {
    private String maHoaDon;
    private String tenCuaHang;
    private String khachHangSdt;
    private Double tongTien;
    private String phuongThucTt;
    private Boolean daXuLy;
    private LocalDateTime ngayTao;
    private String sanPhamJson;
    private List<String> chiTietCapLuot;
    private String gameAccessToken;
    private Boolean tokenDaSuDung;
    private java.time.LocalDateTime tokenHetHan;
    private java.time.LocalDateTime tokenNgaySuDung;
}
