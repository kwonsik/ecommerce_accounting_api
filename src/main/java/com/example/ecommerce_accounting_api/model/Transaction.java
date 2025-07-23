package com.example.ecommerce_accounting_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(precision = 15, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal withdrawAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAmount;

    @Column(length = 100)
    private String branch;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;           // 카테고리 (외래키)

    public Transaction(String description, BigDecimal depositAmount, BigDecimal withdrawAmount, BigDecimal balanceAmount, String branch) {
        this.description = description;
        this.depositAmount = depositAmount;
        this.withdrawAmount = withdrawAmount;
        this.balanceAmount = balanceAmount;
        this.branch = branch;
    }

}
