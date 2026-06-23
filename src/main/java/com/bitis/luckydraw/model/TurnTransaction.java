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
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "id_khach_hang", nullable = false)
    private Long idKhachHang;

    @Column(name = "id_chien_dich", nullable = false)
    private Long idChienDich;

    @Column(name = "loai", nullable = false)
    private Integer loai;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "nguon_tham_chieu", length = 255)
    private String nguonThamChieu;
}
