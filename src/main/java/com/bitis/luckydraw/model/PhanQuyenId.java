package com.bitis.luckydraw.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PhanQuyenId implements Serializable {

    @Column(name = "role_id", nullable = false, length = 20)
    private String roleId;

    @Column(name = "ma_chuc_nang", nullable = false, length = 50)
    private String maChucNang;
}
