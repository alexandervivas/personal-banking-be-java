package com.eureckah.banking.tenants.api;

import com.eureckah.banking.tenants.api.responses.CreateTenantResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tenants")
public class TenantController {

    public TenantController() {

    }

    @PostMapping
    public ResponseEntity<CreateTenantResponse> createTenant() {
        return ResponseEntity.ok(new CreateTenantResponse());
    }
}
