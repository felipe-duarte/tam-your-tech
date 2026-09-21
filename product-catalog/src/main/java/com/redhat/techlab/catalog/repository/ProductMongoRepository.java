package com.redhat.techlab.catalog.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.redhat.techlab.catalog.model.Product;
import com.redhat.techlab.catalog.model.ProductMongo;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@LookupIfProperty(name = "app.repository.type", stringValue = "mongodb")
public class ProductMongoRepository implements ProductRepository {

    @Override
    public List<Product> listAll() {
        return ProductMongo.<ProductMongo>listAll()
                .stream()
                .map(ProductMongo::toProduct)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return ProductMongo.<ProductMongo>find("numericId", id)
                .firstResultOptional()
                .map(doc -> ((ProductMongo) doc).toProduct());
    }

    @Override
    public List<Product> findByCategory(String category) {
        return ProductMongo.<ProductMongo>list("category", category)
                .stream()
                .map(ProductMongo::toProduct)
                .collect(Collectors.toList());
    }

    @Override
    public Product create(Product product) {
        ProductMongo doc = ProductMongo.fromProduct(product);
        doc.createdAt = Instant.now();
        doc.updatedAt = Instant.now();
        doc.numericId = System.nanoTime();
        doc.persist();
        return doc.toProduct();
    }

    @Override
    public Product update(Product product) {
        ProductMongo doc = (ProductMongo) ProductMongo.find("numericId", product.getId()).firstResult();
        if (doc != null) {
            doc.sku = product.getSku();
            doc.name = product.getName();
            doc.description = product.getDescription();
            doc.price = product.getPrice();
            doc.category = product.getCategory();
            doc.updatedAt = Instant.now();
            doc.update();
            return doc.toProduct();
        }
        return create(product);
    }

    @Override
    public void delete(Long id) {
        ProductMongo.delete("numericId", id);
    }
}
