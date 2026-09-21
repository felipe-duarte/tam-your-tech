package com.redhat.techlab.catalog.model;

import java.math.BigDecimal;
import java.time.Instant;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "products")
public class ProductMongo extends PanacheMongoEntity {

    public Long numericId;
    public String sku;
    public String name;
    public String description;
    public BigDecimal price;
    public String category;
    public Instant createdAt;
    public Instant updatedAt;

    public ProductMongo() {
    }

    /**
     * Converts this MongoDB document to a Product POJO for the API layer.
     */
    public Product toProduct() {
        Product p = new Product();
        p.setSku(this.sku);
        p.setName(this.name);
        p.setDescription(this.description);
        p.setPrice(this.price);
        p.setCategory(this.category);
        p.setCreatedAt(this.createdAt);
        p.setUpdatedAt(this.updatedAt);
        p.setId(this.numericId);
        return p;
    }

    /**
     * Creates a MongoDB document from a Product POJO.
     */
    public static ProductMongo fromProduct(Product product) {
        ProductMongo doc = new ProductMongo();
        doc.sku = product.getSku();
        doc.name = product.getName();
        doc.description = product.getDescription();
        doc.price = product.getPrice();
        doc.category = product.getCategory();
        doc.numericId = product.getId();
        doc.createdAt = product.getCreatedAt();
        doc.updatedAt = product.getUpdatedAt();
        return doc;
    }
}
