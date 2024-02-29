package com.valberoliveira.crud.Services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valberoliveira.crud.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer>  {
    
}
