package com.redhat.techlab.catalog.repository;

import java.util.List;
import java.util.Optional;

import com.redhat.techlab.catalog.model.Product;

import io.quarkus.arc.lookup.LookupUnlessProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@LookupUnlessProperty(name = "app.repository.type", stringValue = "mongodb")
public class ProductJpaRepository implements ProductRepository {

    @Inject
    EntityManager em;

    @Override
    public List<Product> listAll() {
        return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        Product product = em.find(Product.class, id);
        return Optional.ofNullable(product);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return em.createQuery("SELECT p FROM Product p WHERE p.category = :category", Product.class)
                .setParameter("category", category)
                .getResultList();
    }

    @Override
    @Transactional
    public Product create(Product product) {
        em.persist(product);
        return product;
    }

    @Override
    @Transactional
    public Product update(Product product) {
        return em.merge(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = em.find(Product.class, id);
        if (product != null) {
            em.remove(product);
        }
    }
}
