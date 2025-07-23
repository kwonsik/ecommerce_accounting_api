package com.example.ecommerce_accounting_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Company {
    @Id
    @Column(name = "company_id", length = 50)
    private String companyId;  // 직접 입력된 문자열 ID

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @OneToMany(mappedBy = "company")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "company")
    private List<Category> categories;  // 추가 필요 (categories 테이블 외래키 있음)

}
