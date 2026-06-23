package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByMaStore(String maStore);
}
