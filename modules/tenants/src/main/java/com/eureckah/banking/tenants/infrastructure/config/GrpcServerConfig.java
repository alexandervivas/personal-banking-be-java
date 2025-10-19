package com.eureckah.banking.tenants.infrastructure.config;

import com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.IdempotencyServerInterceptor;

import io.grpc.ServerInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    public ServerInterceptor idempotencyServerInterceptor() {
        return new IdempotencyServerInterceptor();
    }
}
