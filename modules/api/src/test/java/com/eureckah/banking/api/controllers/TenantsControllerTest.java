package com.eureckah.banking.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;
import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@WebMvcTest(controllers = TenantsController.class)
class TenantsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private TenantsServiceGrpc.TenantsServiceBlockingStub tenantsStub;

    @Test
    void postCreateTenant_returns201AndForwardsToGrpc() throws Exception {
        String tenantId = UUID.randomUUID().toString();
        when(tenantsStub.createTenant(any(CreateTenantRequest.class)))
                .thenReturn(CreateTenantResponse.newBuilder().setTenantId(tenantId).build());

        String userId = UUID.randomUUID().toString();
        String idemKey = UUID.randomUUID().toString();
        mockMvc.perform(
                        post("/v1/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", userId)
                                .header("Idempotency-Key", idemKey)
                                .content("{\"name\":\"Acme Corp\"}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateTenantRequest> captor =
                ArgumentCaptor.forClass(CreateTenantRequest.class);
        verify(tenantsStub, times(1)).createTenant(captor.capture());
        CreateTenantRequest req = captor.getValue();
        assertThat(req.getName()).isEqualTo("Acme Corp");
    }
}
