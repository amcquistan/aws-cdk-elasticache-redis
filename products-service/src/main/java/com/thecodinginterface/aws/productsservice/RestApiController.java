package com.thecodinginterface.aws.productsservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class RestApiController {

    ProductRepository repository;
    public RestApiController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Product> products() {
        return repository.list();
    }

    @GetMapping("{name}")
    public Product product(@PathVariable String name) {
        return repository.findByName(name).get();
    }
}
