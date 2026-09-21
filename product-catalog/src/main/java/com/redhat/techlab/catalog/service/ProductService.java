package com.redhat.techlab.catalog.service;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.techlab.catalog.model.Product;
import com.redhat.techlab.catalog.repository.ProductRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductService {

    @Inject
    Instance<ProductRepository> productRepositoryInstance;

    @Inject
    CacheService cacheService;

    @Inject
    ObjectMapper objectMapper;

    private ProductRepository getRepository() {
        return productRepositoryInstance.get();
    }

    public List<Product> listAll() {
        return getRepository().listAll();
    }

    public List<Product> findByCategory(String category) {
        return getRepository().findByCategory(category);
    }

    public Optional<Product> findById(Long id) {
        // Check cache first
        try {
            String cached = cacheService.get(id.toString());
            if (cached != null) {
                Product product = objectMapper.readValue(cached, Product.class);
                return Optional.of(product);
            }
        } catch (Exception e) {
            // Cache miss or error, fall through to DB
        }

        // Query database
        Optional<Product> product = getRepository().findById(id);

        // Cache the result
        product.ifPresent(p -> {
            try {
                String json = objectMapper.writeValueAsString(p);
                cacheService.set(id.toString(), json);
            } catch (JsonProcessingException e) {
                // Log and continue without caching
            }
        });

        return product;
    }

    public Product create(Product product) {
        Product created = getRepository().create(product);

        // Cache the new product
        if (created.getId() != null) {
            try {
                String json = objectMapper.writeValueAsString(created);
                cacheService.set(created.getId().toString(), json);
            } catch (JsonProcessingException e) {
                // Log and continue
            }
        }

        return created;
    }

    public Product update(Product product) {
        Product updated = getRepository().update(product);

        // Invalidate and re-cache
        if (updated.getId() != null) {
            try {
                cacheService.delete(updated.getId().toString());
                String json = objectMapper.writeValueAsString(updated);
                cacheService.set(updated.getId().toString(), json);
            } catch (JsonProcessingException e) {
                // Log and continue
            }
        }

        return updated;
    }

    public void delete(Long id) {
        getRepository().delete(id);

        // Invalidate cache
        cacheService.delete(id.toString());
    }
}
