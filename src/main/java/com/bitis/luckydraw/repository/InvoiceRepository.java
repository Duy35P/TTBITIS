package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByMaHoaDon(String maHoaDon);
}
