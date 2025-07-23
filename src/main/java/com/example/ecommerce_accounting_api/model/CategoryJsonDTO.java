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
public class CategoryJsonDTO {
    private String category_id;
    private String category_name;
    private List<String> keywords;
}
