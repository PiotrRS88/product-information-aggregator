package com.example.productaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductInformationAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductInformationAggregatorApplication.class, args);
    }
}
