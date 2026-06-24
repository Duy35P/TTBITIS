package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * Tìm chiến dịch đang hoạt động (trang_thai = 1).
     */
    @Query("SELECT c FROM Campaign c WHERE c.id = :id AND c.trangThai = 1")
    Optional<Campaign> findActiveById(@Param("id") Long id);

    /**
     * Lấy tất cả chiến dịch đang hoạt động.
     */
    List<Campaign> findByTrangThai(Integer trangThai);

    /**
     * Tìm chiến dịch theo slug.
     */
    Optional<Campaign> findByDuongDanSlug(String duongDanSlug);
    Optional<Campaign> findByMaChienDich(String maChienDich);
}
