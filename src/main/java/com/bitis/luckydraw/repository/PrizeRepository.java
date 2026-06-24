package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.bitis.luckydraw.dto.PrizeListDto;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
    Optional<Prize> findByMaGiaiThuong(String maGiaiThuong);

    @Query(value = "SELECT * FROM vw_prize_list", nativeQuery = true)
    List<PrizeListDto> getPrizeList();
}
