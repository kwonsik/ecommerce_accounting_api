package com.example.ecommerce_accounting_api.service;

import com.example.ecommerce_accounting_api.model.Category;
import com.example.ecommerce_accounting_api.model.Company;
import com.example.ecommerce_accounting_api.model.Transaction;
import com.example.ecommerce_accounting_api.model.TransactionDTO;
import com.example.ecommerce_accounting_api.repository.CategoryRepository;
import com.example.ecommerce_accounting_api.repository.CompanyRepository;
import com.example.ecommerce_accounting_api.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Resource
    private TransactionRepository transactionRepository;

    @Resource
    private CompanyRepository companyRepository;

    @Resource
    private CategoryRepository categoryRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 거래 내역 처리 및 DB 저장
     * @param csvFile 거래 내역 CSV 파일
     * @param jsonFile 거래 내역 규칙 JSON 파일
     * @return 처리된 결과 메시지
     */
    public String processTransactions(MultipartFile csvFile, MultipartFile jsonFile) {
        try {
            // 1. JSON 파싱 및 DB 저장
            List<Company> companies = parseJsonAndSave(jsonFile);

            // 2. CSV 파싱 → Transaction 객체 생성
            List<Transaction> transactions = parseCsvToTransactions(csvFile);

            // 3. 각 거래에 JSON 기준으로 회사/카테고리 매핑
            for (Transaction transaction : transactions) {
                Company company = getCompanyByDescription(transaction.getDescription(), companies);
                Category category = getCategoryByDescription(transaction.getDescription(), company);
                transaction.setCompany(company);
                transaction.setCategory(category);
            }

            // 4. DB에 INSERT
            transactionRepository.saveAll(transactions);

            return "Transactions processed and saved successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing files.";
        }
    }

    /**
     * JSON 파일을 파싱하여 Company & Category DB 저장
     */
    private List<Company> parseJsonAndSave(MultipartFile jsonFile) {
        try {
            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(jsonFile.getInputStream());
            JsonNode companiesNode = rootNode.get("companies");

            List<Company> companies = new ArrayList<>();

            if (companiesNode.isArray()) {
                for (JsonNode companyNode : companiesNode) {
                    Company company = new Company();
                    company.setCompanyId(companyNode.get("company_id").asText());
                    company.setCompanyName(companyNode.get("company_name").asText());

                    companyRepository.save(company);

                    // Category 리스트 추출
                    List<Category> categories = new ArrayList<>();
                    JsonNode categoriesNode = companyNode.get("categories");

                    if (categoriesNode.isArray()) {
                        for (JsonNode categoryNode : categoriesNode) {
                            Category category = new Category();
                            category.setCategoryId(categoryNode.get("category_id").asText());
                            category.setCategoryName(categoryNode.get("category_name").asText());
                            category.setKeywords(
                                    String.join(",", objectMapper.convertValue(categoryNode.get("keywords"), List.class))
                            );

                            category.setCompany(company);


                            categories.add(category);
                        }
                    }
                    categoryRepository.saveAll(categories); // 카테고리 저장
                    companies.add(company);
                }
            }
            // 🔹 Unknown Company 추가
            Company unknownCompany = new Company();
            unknownCompany.setCompanyId("com_unknown");
            unknownCompany.setCompanyName("Unknown");
            companyRepository.save(unknownCompany);
            companies.add(unknownCompany);

            // 🔹 Unknown Category 추가
            Category unknownCategory = new Category();
            unknownCategory.setCategoryId("cat_unknown");
            unknownCategory.setCategoryName("Unknown");
            unknownCategory.setKeywords("");
            unknownCategory.setCompany(unknownCompany); // 연결 추가
            categoryRepository.save(unknownCategory);

            companyRepository.saveAll(companies); // 회사 저장
            return companies;

        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * CSV 파일을 파싱하여 거래 리스트로 변환
     */
    private List<Transaction> parseCsvToTransactions(MultipartFile csvFile) {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;  // 첫 번째 라인은 헤더이므로 건너뜀
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;  // 헤더 건너뛰기
                }
                String[] columns = line.split(",");

                // CSV 컬럼: 거래일시, 적요, 입금액, 출금액, 거래후잔액, 거래점
                String transactionTimeStr = columns[0].trim();
                String description = columns[1].trim();
                BigDecimal depositAmount = new BigDecimal(columns[2].trim());
                BigDecimal withdrawAmount = new BigDecimal(columns[3].trim());
                BigDecimal balanceAmount = new BigDecimal(columns[4].trim());
                String branch = columns[5].trim();

                // Transaction 객체 생성
                Transaction transaction = new Transaction(
                        description,
                        depositAmount,
                        withdrawAmount,
                        balanceAmount,
                        branch
                );

                // 거래일시를 LocalDateTime으로 변환
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                transaction.setTransactionTime(LocalDateTime.parse(transactionTimeStr, formatter));

                transactions.add(transaction);
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV 파싱 중 오류 발생: " + e.getMessage(), e);
        }
        return transactions;
    }

    /**
     * 거래 내역 적요로 회사 찾기
     */
    private Company getCompanyByDescription(String description, List<Company> companies) {
        for (Company company : companies) {
            List<Category> categories = categoryRepository.findByCompanyCompanyId(company.getCompanyId());
            for (Category category : categories) {
                String keywords = category.getKeywords();
                if (keywords == null || keywords.isBlank()) continue;

                for (String keyword : keywords.split(",")) {
                    if (description.contains(keyword.trim())) {
                        return company;
                    }
                }
            }
        }
        // 어떤 회사와도 매칭되지 않음 → 기타 회사 반환
        return companies.stream()
                .filter(c -> "com_unknown".equals(c.getCompanyId()))
                .findFirst()
                .orElse(companyRepository.findById("com_unknown").orElse(null));
    }

    /**
     * 거래 내역 적요로 카테고리 찾기
     */
    private Category getCategoryByDescription(String description, Company company) {
        List<Category> categories = categoryRepository.findByCompanyCompanyId(company.getCompanyId());
        for (Category category : categories) {
            String keywords = category.getKeywords();
            if (keywords == null || keywords.isBlank()) continue;

            for (String keyword : keywords.split(",")) {
                if (description.contains(keyword.trim())) {
                    return category;
                }
            }
        }
        // 미분류 카테고리 반환 (ID 기준)
        return categories.stream()
                .filter(cat -> "cat_unknown".equals(cat.getCategoryId()))
                .findFirst()
                .orElse(null);
    }

    public List<TransactionDTO> getTransactionsByCompany(String companyId) {
        return transactionRepository.findByCompanyCompanyId(companyId)
                .stream()
                .map(ts -> new TransactionDTO(
                        ts.getTransactionTime(),
                        ts.getDescription(),
                        ts.getDepositAmount(),
                        ts.getWithdrawAmount(),
                        ts.getBalanceAmount(),
                        ts.getBranch(),
                        ts.getCategory() != null ? ts.getCategory().getCategoryId() : "cat_unknown",
                        ts.getCategory() != null ? ts.getCategory().getCategoryName() : "미분류"
                ))
                .toList();
    }

}



