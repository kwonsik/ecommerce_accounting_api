package com.example.ecommerce_accounting_api.controller;

import com.example.ecommerce_accounting_api.service.TransactionService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/accounting")
public class AccountingController {
    @Resource
    private TransactionService transactionService;

    /**
     * 거래 내역 처리 및 DB 저장 API
     * @param csvFile 거래 내역 CSV 파일
     * @param jsonFile 거래 내역 규칙 JSON 파일
     * @return 처리된 결과 메시지
     */
    @PostMapping("/process")
    public ResponseEntity<String> processAccountingData(
            @RequestParam("csvFile") MultipartFile csvFile,
            @RequestParam("jsonFile") MultipartFile jsonFile) {

        // 서비스 호출하여 거래 내역 처리
        String result = transactionService.processTransactions(csvFile, jsonFile);

        // 처리 결과에 따른 응답 반환
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);  // 성공적인 처리
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 실패 처리
        }
    }

    @GetMapping("/records")
    public ResponseEntity<?> getRecordsByCompany(@RequestParam("companyId") String companyId) {
        try {
            return ResponseEntity.ok(transactionService.getTransactionsByCompany(companyId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching records: " + e.getMessage());
        }
    }
}
