package com.example.ecommerce_accounting_api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyJsonDTO {
    private String company_id;
    private String company_name;
    private List<CategoryJsonDTO> categories;
}
