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
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_store", nullable = false)
    private String maStore;

    @Column(name = "ma_giai_thuong", nullable = false)
    private String maGiaiThuong;

    @Column(name = "ton_kho", nullable = false)
    private Integer tonKho;
}
