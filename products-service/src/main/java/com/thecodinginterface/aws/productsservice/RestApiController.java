package com.thecodinginterface.aws.productsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class RestApiController {

    static final Logger log = LoggerFactory.getLogger(RestApiController.class);

    ProductRepository repository;
    public RestApiController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Product> products() {
        log.info("*** requesting products endpoint");
        return repository.list();
    }

    @GetMapping("{name}")
    public Product product(@PathVariable String name) {
        return repository.findByName(name).get();
    }
}
