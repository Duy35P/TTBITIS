package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "store_prize_inventory")
public class StorePrizeInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "id_cua_hang", nullable = false)
    private Long idCuaHang;

    @Column(name = "id_giai_thuong", nullable = false)
    private Long idGiaiThuong;

    @Column(name = "ton_kho", nullable = false)
    private Integer tonKho;
}
