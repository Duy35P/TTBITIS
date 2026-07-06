package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.TurnTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TurnTransactionRepository extends JpaRepository<TurnTransaction, Long> {
    List<TurnTransaction> findByNguonThamChieu(String nguonThamChieu);
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.soLuong), 0) FROM TurnTransaction t WHERE t.loai = 0")
    long sumSpins();

    @org.springframework.data.jpa.repository.Query(value = "SELECT CONVERT(varchar, thoi_gian_tao, 23) as spinDate, SUM(so_luong) as spinCount FROM turn_transaction WHERE loai = 0 GROUP BY CONVERT(varchar, thoi_gian_tao, 23) ORDER BY spinDate ASC", nativeQuery = true)
    List<Object[]> getSpinsPerDay();

    @org.springframework.data.jpa.repository.Query("SELECT new com.bitis.luckydraw.dto.TurnHistoryDto(t.id, t.thoiGianTao, c.phone, c.tenKhach, cam.tenChienDich, t.loai, t.soLuong, t.nguonThamChieu) " +
           "FROM TurnTransaction t " +
           "JOIN com.bitis.luckydraw.model.Customer c ON t.maKhachHang = c.maKhachHang " +
           "JOIN com.bitis.luckydraw.model.Campaign cam ON t.maChienDich = cam.maChienDich " +
           "WHERE (:phone IS NULL OR :phone = '' OR c.phone LIKE %:phone%) " +
           "AND (:loai IS NULL OR t.loai = :loai) " +
           "AND (:maChienDich IS NULL OR :maChienDich = '' OR t.maChienDich = :maChienDich) " +
           "ORDER BY t.id DESC")
    List<com.bitis.luckydraw.dto.TurnHistoryDto> findTurnHistory(@org.springframework.data.repository.query.Param("phone") String phone, 
                                                                 @org.springframework.data.repository.query.Param("loai") Integer loai, 
                                                                 @org.springframework.data.repository.query.Param("maChienDich") String maChienDich);
}
