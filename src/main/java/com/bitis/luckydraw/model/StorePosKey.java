package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "store_pos_key")
public class StorePosKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ma_store", nullable = false)
    private String maStore;

    @Column(name = "api_key_hash", nullable = false, length = 255)
    private String apiKeyHash;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "thoi_gian_tao", nullable = false, updatable = false, insertable = false)
    private LocalDateTime thoiGianTao;

    @Column(name = "thoi_gian_het_han")
    private LocalDateTime thoiGianHetHan;
}
