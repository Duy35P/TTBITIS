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
    @Column(name = "turn_id", nullable = false)
    private Long turnId;

    @Column(name = "id_khach_hang", nullable = false)
    private Long idKhachHang;

    @Column(name = "id_chien_dich", nullable = false)
    private Long idChienDich;

    @Column(name = "luot_con_lai", nullable = false)
    private Integer luotConLai;
}
