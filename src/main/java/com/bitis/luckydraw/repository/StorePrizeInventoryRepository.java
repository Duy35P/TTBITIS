package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.StorePrizeInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorePrizeInventoryRepository extends JpaRepository<StorePrizeInventory, Long> {
    List<StorePrizeInventory> findByIdCuaHang(Long idCuaHang);
    Optional<StorePrizeInventory> findByIdCuaHangAndIdGiaiThuong(Long idCuaHang, Long idGiaiThuong);
}
