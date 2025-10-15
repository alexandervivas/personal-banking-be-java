package com.eureckah.banking.api.config;

import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import io.grpc.ManagedChannel;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.channel.GrpcChannelFactory;

@Configuration
public class GrpcClientsConfig {

    @Bean
    public ManagedChannel tenantsManagedChannel(GrpcChannelFactory channelFactory) {
        return channelFactory.createChannel("tenants");
    }

    @Bean
    public TenantsServiceGrpc.TenantsServiceBlockingStub tenantsStub(
            @Qualifier("tenantsManagedChannel") ManagedChannel tenantsManagedChannel) {
        return TenantsServiceGrpc.newBlockingStub(tenantsManagedChannel);
    }
}
