package com.eureckah.banking.api.config;

import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GrpcClientsConfig {

    @Bean
    public TenantsServiceGrpc.TenantsServiceBlockingStub tenantsStub(
            DiscoveryClient discoveryClient) {
        // Lookup the tenants service from Eureka
        List<ServiceInstance> instances = discoveryClient.getInstances("tenants");

        if (instances.isEmpty()) {
            throw new IllegalStateException(
                    "No instances of 'tenants' service found in service registry");
        }

        // Use the first available instance
        ServiceInstance instance = instances.getFirst();

        // Build the gRPC channel directly
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress(instance.getHost(), instance.getPort())
                        .usePlaintext()
                        .keepAliveWithoutCalls(true)
                        .build();

        return TenantsServiceGrpc.newBlockingStub(channel);
    }
}
