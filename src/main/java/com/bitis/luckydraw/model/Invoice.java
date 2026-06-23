package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "ma_hoa_don", nullable = false, length = 255)
    private String maHoaDon;

    @Column(name = "ma_hoa_don_goc", length = 255)
    private String maHoaDonGoc;

    @Column(name = "id_cua_hang", nullable = false)
    private Long idCuaHang;

    @Column(name = "id_khach_hang")
    private Long idKhachHang;

    @Column(name = "tong_tien", nullable = false)
    private Double tongTien;

    @Column(name = "phuong_thuc_tt", length = 50)
    private String phuongThucTt;

    @Column(name = "san_pham_json", columnDefinition = "NVARCHAR(MAX)")
    private String sanPhamJson;

    @Column(name = "da_xu_ly", nullable = false)
    private Boolean daXuLy;
}
