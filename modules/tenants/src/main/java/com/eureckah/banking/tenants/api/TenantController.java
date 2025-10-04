package com.eureckah.banking.tenants.api;

import com.eureckah.banking.tenants.api.responses.CreateTenantResponse;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommandHandler;
import com.eureckah.banking.tenants.application.views.TenantView;
import com.eureckah.banking.tenants.domain.TenantId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping("api/v1/tenants")
public class TenantController {

    private CreateTenantCommandHandler createTenantCommandHandler;

    @Value("${hostname}")
    private static String hostname;

    private static final String BASE_PATH = hostname + "/api/v1/tenants/";

    public TenantController(CreateTenantCommandHandler createTenantCommandHandler) {
        this.createTenantCommandHandler = createTenantCommandHandler;
    }

    @PostMapping
    public ResponseEntity<URI> createTenant(@RequestBody CreateTenantRequest request) {
        CreateTenantCommand command = new CreateTenantCommand(request.name());
        TenantId tenantId = createTenantCommandHandler.handle(command);

        try {
            return ResponseEntity.created(new URI(BASE_PATH + tenantId.id())).build();
        } catch (URISyntaxException exception) {
            log.error("Error creating URI for tenant", exception);
            return ResponseEntity.internalServerError().build();
        }
    }
}
