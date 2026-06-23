package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chuc_nang")
public class ChucNang {

    @Id
    @Column(name = "ma_chuc_nang", nullable = false, length = 50)
    private String maChucNang;

    @Column(name = "ten_chuc_nang", nullable = false, length = 255)
    private String tenChucNang;

    @Column(name = "nhom", length = 100)
    private String nhom;
}
