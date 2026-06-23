package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.TurnTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnTransactionRepository extends JpaRepository<TurnTransaction, Long> {
}
