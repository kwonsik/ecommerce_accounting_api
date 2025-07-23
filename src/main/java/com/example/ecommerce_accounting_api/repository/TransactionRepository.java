package com.example.ecommerce_accounting_api.repository;

import com.example.ecommerce_accounting_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    // 추가적인 쿼리가 필요하면 여기에 작성
    List<Transaction> findByCompanyCompanyId(String companyId);
}
