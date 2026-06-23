package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vai_tro")
public class VaiTro {

    @Id
    @Column(name = "role_id", nullable = false, length = 20)
    private String roleId;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "mo_ta", length = 500)
    private String moTa;
}
