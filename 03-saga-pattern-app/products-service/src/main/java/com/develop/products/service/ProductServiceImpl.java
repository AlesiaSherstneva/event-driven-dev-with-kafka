package com.develop.products.service;

import com.develop.core.dto.Product;
import com.develop.core.exceptions.ProductInsufficientQuantityException;
import com.develop.products.dao.jpa.entity.ProductEntity;
import com.develop.products.dao.jpa.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Product reserve(Product desiredProduct, UUID orderId) {
        ProductEntity productEntity = productRepository.findById(desiredProduct.getId()).orElseThrow();
        if (desiredProduct.getQuantity() > productEntity.getQuantity()) {
            throw new ProductInsufficientQuantityException(productEntity.getId(), orderId);
        }

        productEntity.setQuantity(productEntity.getQuantity() - desiredProduct.getQuantity());
        productRepository.save(productEntity);

        Product reservedProduct = new Product();
        BeanUtils.copyProperties(productEntity, reservedProduct);
        reservedProduct.setQuantity(desiredProduct.getQuantity());
        return reservedProduct;
    }

    @Override
    public void cancelReservation(Product productToCancel, UUID orderId) {
        ProductEntity productEntity = productRepository.findById(productToCancel.getId()).orElseThrow();
        productEntity.setQuantity(productEntity.getQuantity() + productToCancel.getQuantity());
        productRepository.save(productEntity);
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = ProductEntity.builder()
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
        productRepository.save(productEntity);

        return Product.builder()
                .id(productEntity.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll().stream()
                .map(entity -> Product.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .price(entity.getPrice())
                        .quantity(entity.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }
}