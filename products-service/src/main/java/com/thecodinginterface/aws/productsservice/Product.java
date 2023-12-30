package com.thecodinginterface.aws.productsservice;

import java.io.Serializable;

public class Product implements Serializable {

    private String name;
    private String description;
    private Long priceInCents;

    public Product() {}

    public Product(String name, String description, Long priceInCents) {
        this.name = name;
        this.description = description;
        this.priceInCents = priceInCents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPriceInCents() {
        return priceInCents;
    }

    public void setPriceInCents(Long priceInCents) {
        this.priceInCents = priceInCents;
    }
}
