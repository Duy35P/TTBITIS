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
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "phone", nullable = false, length = 15, unique = true)
    private String phone;

    @Column(name = "zalo_id", length = 255)
    private String zaloId;

    @Column(name = "ten_khach", length = 255)
    private String tenKhach;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
