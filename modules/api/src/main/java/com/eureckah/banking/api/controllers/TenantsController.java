package com.eureckah.banking.api.controllers;

import com.eureckah.banking.api.controllers.support.AbstractIdempotentGrpcController;
import com.eureckah.banking.api.requests.CreateTenantHttpRequest;
import com.eureckah.banking.api.responses.CreateTenantHttpResponse;
import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;
import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tenants")
public class TenantsController extends AbstractIdempotentGrpcController {

    private final TenantsServiceGrpc.TenantsServiceBlockingStub tenantsStub;

    public TenantsController(TenantsServiceGrpc.TenantsServiceBlockingStub tenantsStub) {
        this.tenantsStub = tenantsStub;
    }

    @PostMapping
    public ResponseEntity<CreateTenantHttpResponse> createTenant(
            @RequestBody CreateTenantHttpRequest body,
            @RequestHeader(name = "X-User-Id") String userId,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey) {
        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName(body.name()).build();

        TenantsServiceGrpc.TenantsServiceBlockingStub callStub =
                attachHeaders(tenantsStub, buildIdempotencyMetadata(userId, idempotencyKey));

        CreateTenantResponse response = callStub.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateTenantHttpResponse(response.getTenantId()));
    }
}
