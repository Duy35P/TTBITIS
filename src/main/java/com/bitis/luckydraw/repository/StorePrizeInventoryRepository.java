package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.StorePrizeInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import com.bitis.luckydraw.dto.StoreInventoryDto;

@Repository
public interface StorePrizeInventoryRepository extends JpaRepository<StorePrizeInventory, Long> {
    List<StorePrizeInventory> findByMaStore(String maStore);
    List<StorePrizeInventory> findByMaGiaiThuong(String maGiaiThuong);
    Optional<StorePrizeInventory> findByMaStoreAndMaGiaiThuong(String maStore, String maGiaiThuong);

    @Query(value = "SELECT COALESCE(SUM(da_phat), 0) FROM store_prize_inventory", nativeQuery = true)
    Long sumDaPhat();

    @Query(value = "SELECT COALESCE(SUM(ton_kho), 0) FROM store_prize_inventory", nativeQuery = true)
    Long sumTonKho();

    @Query(value = "SELECT COALESCE(SUM(ton_kho), 0) FROM store_prize_inventory WHERE ma_giai_thuong = :maGiaiThuong", nativeQuery = true)
    Long sumTonKhoByMaGiaiThuong(@Param("maGiaiThuong") String maGiaiThuong);

    @Query(value = "SELECT c.ma_chien_dich AS maChienDich, c.ten_chien_dich AS tenChienDich, " +
                   "COALESCE(SUM(spi.tong_luong_cap), 0) AS tongLuongCap, " +
                   "COALESCE(SUM(spi.da_phat), 0) AS daPhat, " +
                   "COALESCE(SUM(spi.ton_kho), 0) AS tonKho " +
                   "FROM store_prize_inventory spi " +
                   "JOIN prize p ON spi.ma_giai_thuong = p.ma_giai_thuong " +
                   "JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich " +
                   "GROUP BY c.ma_chien_dich, c.ten_chien_dich", nativeQuery = true)
    List<java.util.Map<String, Object>> getCampaignRewardStats();

    @Query(value = "SELECT * FROM vw_store_prize_inventory " +
                   "WHERE (:maStore IS NULL OR maStore = :maStore) " +
                   "AND (:maChienDich IS NULL OR maChienDich = :maChienDich) " +
                   "AND (:maGiaiThuong IS NULL OR maGiaiThuong = :maGiaiThuong)", 
           nativeQuery = true)
    List<StoreInventoryDto> getStoreInventory(@Param("maStore") String maStore, 
                                              @Param("maChienDich") String maChienDich, 
                                              @Param("maGiaiThuong") String maGiaiThuong);

    @Modifying
    @Query(value = "EXEC sp_AllocatePrizeToStore @maStore = :maStore, @maGiaiThuong = :maGiaiThuong, @quantity = :quantity", nativeQuery = true)
    void allocatePrizeToStore(@Param("maStore") String maStore, 
                              @Param("maGiaiThuong") String maGiaiThuong, 
                              @Param("quantity") int quantity);

    @Modifying
    @Query(value = "EXEC sp_UpdateStorePrizeInventory @maStore = :maStore, @maGiaiThuong = :maGiaiThuong, @newTongLuongCap = :newTongLuongCap", nativeQuery = true)
    void updateStorePrizeInventory(@Param("maStore") String maStore, 
                                   @Param("maGiaiThuong") String maGiaiThuong, 
                                   @Param("newTongLuongCap") int newTongLuongCap);

}
