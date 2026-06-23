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
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "username", nullable = false, length = 255, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "ten_nhan_vien", nullable = false, length = 255)
    private String tenNhanVien;

    @Column(name = "role_id", nullable = false, length = 20)
    private String roleId;

    @Column(name = "id_cua_hang")
    private Long idCuaHang;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
