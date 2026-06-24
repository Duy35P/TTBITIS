package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "turn_transaction")
public class TurnTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_khach_hang", nullable = false)
    private String maKhachHang;

    @Column(name = "ma_chien_dich", nullable = false)
    private String maChienDich;

    @Column(name = "loai", nullable = false)
    private Integer loai;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "nguon_tham_chieu", length = 255)
    private String nguonThamChieu;
}
