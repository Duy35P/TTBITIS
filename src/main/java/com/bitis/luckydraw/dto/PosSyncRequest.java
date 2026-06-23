package com.bitis.luckydraw.dto;

import lombok.Data;
import java.util.List;

@Data
public class PosSyncRequest {
    private Long storeId;
    private String soDienThoai;
    private String tenKhachHang;
    private Double tongTien;
    private String phuongThucThanhToan;
    private List<String> danhSachSku;
}
