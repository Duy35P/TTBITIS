package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.CustomerTurn;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerTurnRepository extends JpaRepository<CustomerTurn, Long> {

    /**
     * Tìm ví lượt quay của khách hàng theo chiến dịch.
     * Sử dụng Pessimistic Lock (SELECT ... WITH (UPDLOCK)) để chống Race Condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CustomerTurn c WHERE c.maKhachHang = :maKhachHang AND c.maChienDich = :maChienDich")
    Optional<CustomerTurn> findByMaKhachHangAndMaChienDich(@Param("maKhachHang") String maKhachHang, @Param("maChienDich") String maChienDich);

    @Modifying
    @Query(value = "EXEC sp_AddCustomerTurns_Safe :maKhachHang, :maChienDich, :soLuongCong, :nguonThamChieu", nativeQuery = true)
    void addCustomerTurnsSafe(@Param("maKhachHang") String maKhachHang, 
                              @Param("maChienDich") String maChienDich, 
                              @Param("soLuongCong") Integer soLuongCong, 
                              @Param("nguonThamChieu") String nguonThamChieu);
}
