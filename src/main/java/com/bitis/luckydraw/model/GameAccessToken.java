package com.bitis.luckydraw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_access_token")
public class GameAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "ma_hoa_don", nullable = false)
    private String maHoaDon;

    @Column(name = "so_luong_luot_thuong", nullable = false)
    private Integer soLuongLuotThuong;

    @Column(name = "da_su_dung", nullable = false)
    private Boolean daSuDung;

    @Column(name = "ma_khach_hang_kich_hoat")
    private String maKhachHangKichHoat;

    @Column(name = "het_han_luc", nullable = false)
    private LocalDateTime hetHanLuc;
}
