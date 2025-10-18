package com.eureckah.banking.tenants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TenantsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TenantsApplication.class, args);
    }
}
