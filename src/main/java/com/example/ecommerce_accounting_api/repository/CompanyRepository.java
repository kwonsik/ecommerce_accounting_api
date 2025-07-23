package com.example.ecommerce_accounting_api.repository;

import com.example.ecommerce_accounting_api.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    // 추가적인 쿼리가 필요하면 여기에 작성
}
