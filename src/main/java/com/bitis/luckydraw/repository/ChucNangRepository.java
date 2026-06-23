package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.ChucNang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChucNangRepository extends JpaRepository<ChucNang, String> {
}
