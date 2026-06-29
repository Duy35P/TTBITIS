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
}
