package com.eureckah.banking.tenants.api;

import com.eureckah.banking.tenants.api.requests.CreateTenantRequest;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommandHandler;
import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/tenants")
public final class TenantController {

    @Value("${hostname}")
    private static String hostname;

    private static final String BASE_PATH = hostname + "/api/v1/tenants/";
    private final CreateTenantCommandHandler createTenantCommandHandler;

    public TenantController(CreateTenantCommandHandler createTenantCommandHandler) {
        this.createTenantCommandHandler = createTenantCommandHandler;
    }

    @PostMapping
    public ResponseEntity<URI> createTenant(@RequestBody CreateTenantRequest request) {
        CreateTenantCommand command = new CreateTenantCommand(request.name());

        try {

            UUID tenantId = createTenantCommandHandler.handle(command);
            String resourceCreated = BASE_PATH + tenantId.toString();

            return ResponseEntity.created(new URI(resourceCreated)).build();

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

        } catch (URISyntaxException exception) {
            log.error("Error creating URI for tenant {}", request.name(), exception);
            return ResponseEntity.internalServerError().build();
        }
    }
}
