package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.CustomerTurn;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerTurnRepository extends JpaRepository<CustomerTurn, Long> {

    /**
     * Tìm ví lượt quay của khách hàng theo chiến dịch.
     * Sử dụng Pessimistic Lock (SELECT ... WITH (UPDLOCK)) để chống Race Condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CustomerTurn> findByIdKhachHangAndIdChienDich(Long idKhachHang, Long idChienDich);
}
