package com.redhat.techlab.catalog.repository;

import java.util.List;
import java.util.Optional;

import com.redhat.techlab.catalog.model.Product;

public interface ProductRepository {

    List<Product> listAll();

    Optional<Product> findById(Long id);

    List<Product> findByCategory(String category);

    Product create(Product product);

    Product update(Product product);

    void delete(Long id);
}
