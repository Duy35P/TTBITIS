package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_turn")
public class CustomerTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ma_khach_hang", nullable = false)
    private String maKhachHang;

    @Column(name = "ma_chien_dich", nullable = false)
    private String maChienDich;

    @Column(name = "luot_con_lai", nullable = false)
    private Integer luotConLai;
}
