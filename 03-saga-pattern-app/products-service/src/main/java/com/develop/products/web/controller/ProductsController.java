package com.develop.products.web.controller;

import com.develop.core.dto.Product;
import com.develop.products.dto.ProductCreationRequest;
import com.develop.products.dto.ProductCreationResponse;
import com.develop.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {
    private final ProductService productService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Product> findAll() {
        return productService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCreationResponse save(@RequestBody @Valid ProductCreationRequest request) {
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        Product result = productService.save(product);

        ProductCreationResponse productCreationResponse = new ProductCreationResponse();
        BeanUtils.copyProperties(result, productCreationResponse);
        return productCreationResponse;
    }
}