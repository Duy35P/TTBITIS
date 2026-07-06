package com.bitis.luckydraw.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnHistoryDto {
    private Long id;
    private LocalDateTime thoiGianTao;
    private String soDienThoai;
    private String tenKhachHang;
    private String tenChienDich;
    private Integer loai;
    private Integer soLuong;
    private String nguonThamChieu;
}
