package com.eureckah.banking.api.config;

import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientsConfig {

    @Bean
    public TenantsServiceGrpc.TenantsServiceBlockingStub tenantsStub(
            @Value("${grpc.client.tenants.address}") String address) {
        String target = address;
        if (target.startsWith("static://")) {
            target = target.substring("static://".length());
        }
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        return TenantsServiceGrpc.newBlockingStub(channel);
    }
}
