📊 이커머스 경영 경리 자동 분류 시스템

1. 실행 및 테스트 가이드 (Quick Start)

   프로젝트 실행 방법 (Docker 기반)

    1) 전체 클론
       git clone https://github.com/kwonsik/ecommerce_accounting_api.git
       
    3) Docker + Docker Compose 설치

    4) 실행
       docker-compose up --build

       Spring Boot: http://localhost:7070

       MySQL: 내부적으로 localhost:3306

    5) API 테스트 (Postman)

       -1 거래내역 업로드
       POST "http://localhost:7070/api/v1/accounting/process"
       Form-data
       Key	      Value
       csvFile	   File bank_transactions.csv
       jsonFile	File rules.json

       -2 사업체별 거래내역 조회
       GET "http://localhost:7070/api/v1/accounting/records?companyId=?"


2. 설계 및 보안 아키텍처 기술서

   A. 시스템 아키텍처

        언어: Java 17
        프레임워크: Spring Boot 3.2.5
        빠른 개발, JPA 기반 ORM, RESTful 지원이 용이하여 채택
        데이터베이스: MySQL 8
        트랜잭션 강력, 범용성 높고 운영 편의성이 뛰어남
        환경: Docker Compose
        개발/배포 환경 통일, 설치 편의성 제공
        DB 스키마

        CREATE TABLE companies (
            company_id     VARCHAR(50) PRIMARY KEY,
            company_name   VARCHAR(100) NOT NULL,
            created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE categories (
            category_id    VARCHAR(50) PRIMARY KEY,
            company_id     VARCHAR(50) NOT NULL,
            category_name  VARCHAR(100) NOT NULL,
            keywords       TEXT,
            FOREIGN KEY (company_id) REFERENCES companies(company_id)
        );

        CREATE TABLE transactions (
            transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
            transaction_time DATETIME NOT NULL,
            description      VARCHAR(255) NOT NULL,
            deposit_amount   DECIMAL(15,2) DEFAULT 0,
            withdraw_amount  DECIMAL(15,2) DEFAULT 0,
            balance_amount   DECIMAL(15,2) NOT NULL,
            branch           VARCHAR(100),
            company_id       VARCHAR(50),
            category_id      VARCHAR(50),
            created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (company_id) REFERENCES companies(company_id),
            FOREIGN KEY (category_id) REFERENCES categories(category_id)
        );

        companies: 회사별 식별자 및 기본 정보
        categories: 회사별 계정과목 및 키워드 기본 규칙 보유
        transactions: CSV 기본 거래내역 저장 및 분류 결과 저장

   B. 핵심 자동 분류 로직
   rules.json을 파싱해 회사 및 카테고리 정보를 DB에 저장
   bank_transactions.csv에서 거래 적요(description)를 기준으로 키워드를 매칭
   키워드가 일치하는 회사와 카테고리로 거래내역을 매칭해서 저장
   매칭되지 않는 경우, "com_unknown", "cat_unknown" 항목으로 분류
