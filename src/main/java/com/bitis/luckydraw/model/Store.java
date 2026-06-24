package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ten_cua_hang", nullable = false, length = 255)
    private String tenCuaHang;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "dia_chi_store", nullable = false, length = 255)
    private String diaChiStore;

    @Column(name = "ma_store", nullable = false, length = 255)
    private String maStore;
}
