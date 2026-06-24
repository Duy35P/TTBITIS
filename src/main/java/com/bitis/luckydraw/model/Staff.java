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

    @Column(name = "ma_nhan_vien", unique = true)
    private String maNhanVien;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 255, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "ten_nhan_vien", nullable = false, length = 255)
    private String tenNhanVien;

    @Column(name = "role_id", nullable = false, length = 20)
    private String roleId;

    @Column(name = "ma_store")
    private String maStore;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
