package com.eureckah.banking.tenants.api;

import com.eureckah.banking.tenants.api.requests.CreateTenantRequest;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommandHandler;
import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/tenants")
public final class TenantController {

    private final CreateTenantCommandHandler createTenantCommandHandler;

    public TenantController(CreateTenantCommandHandler createTenantCommandHandler) {
        this.createTenantCommandHandler = createTenantCommandHandler;
    }

    @PostMapping
    public ResponseEntity<Void> createTenant(@RequestBody CreateTenantRequest request) {
        CreateTenantCommand command = new CreateTenantCommand(request.name());

        try {
            UUID tenantId = createTenantCommandHandler.handle(command);

            URI newResourceLocation = getResourceUri(tenantId);

            return ResponseEntity.created(newResourceLocation).build();

        } catch (InvalidCommandException exception) {
            log.error(
                    "Error creating the tenant {} because of {}",
                    request.name(),
                    exception.getReason(),
                    exception);
            return ResponseEntity.badRequest().build();

        } catch (FailedCommandException exception) {
            log.error("Error creating the tenant {}", request.name(), exception);
            return ResponseEntity.internalServerError().build();
        }
    }

    private static URI getResourceUri(UUID tenantId) {
        return ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(tenantId)
                .toUri();
    }
}
