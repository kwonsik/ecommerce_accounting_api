package com.example.ecommerce_accounting_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private LocalDateTime transactionTime;
    private String description;
    private BigDecimal depositAmount;
    private BigDecimal withdrawAmount;
    private BigDecimal balanceAmount;
    private String branch;
    private String categoryId;
    private String categoryName;
}
