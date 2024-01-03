package com.thecodinginterface.aws.productsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    static final Logger log = LoggerFactory.getLogger(ProductRepository.class);

    @Cacheable(value = "products")
    public List<Product> list() {
        log.info("*** fetching fresh list (no caching)");
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return List.of(
            new Product("Mega Slinky", "A large slinky.", 10_00L),
            new Product("Chia Pet", "A simple to grow novelty toy.", 5_00L),
            new Product("Stress Ball", "A squishy hand sized foam ball.", 2_00L),
            new Product("Silly String", "An aerosol colored foam spray.", 3_00L)
        );
    }

    public Optional<Product> findByName(String name) {
        return list().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }
}
