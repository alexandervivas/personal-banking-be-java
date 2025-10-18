package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.port.in.CreateTenantUseCase;
import com.eureckah.banking.tenants.application.port.in.GetUserUseCase;
import com.eureckah.banking.tenants.application.queries.GetUserQuery;
import com.eureckah.banking.tenants.domain.model.User;
import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class TenantsGrpcServiceTest {

    private CreateTenantUseCase createTenantUseCase;
    private GetUserUseCase getUserUseCase;
    private TenantsGrpcService service;

    @BeforeEach
    void setup() {
        createTenantUseCase = mock(CreateTenantUseCase.class);
        getUserUseCase = mock(GetUserUseCase.class);
        service = new TenantsGrpcService(createTenantUseCase, getUserUseCase);
    }

    @Test
    void createTenant_success() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Alice").email("alice@example.com").build();

        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.of(user));
        when(createTenantUseCase.handle(any(CreateTenantCommand.class)))
                .thenReturn(Optional.of(tenantId));

        CreateTenantRequest request =
                CreateTenantRequest.newBuilder()
                        .setUserId(userId.toString())
                        .setName("Acme Corp")
                        .build();

        AtomicReference<CreateTenantResponse> respRef = new AtomicReference<>();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        service.createTenant(
                request,
                new StreamObserver<>() {
                    @Override
                    public void onNext(CreateTenantResponse value) {
                        respRef.set(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        errRef.set(t);
                    }

                    @Override
                    public void onCompleted() {}
                });

        assertThat(errRef.get()).isNull();
        assertThat(respRef.get()).isNotNull();
        assertThat(respRef.get().getTenantId()).isEqualTo(tenantId.toString());

        verify(getUserUseCase).handle(argThat(q -> q.id().equals(userId)));
        verify(createTenantUseCase)
                .handle(
                        argThat(
                                cmd ->
                                        cmd.name().equals("Acme Corp")
                                                && cmd.user().getId().equals(userId)));
    }

    @Test
    void createTenant_invalidUserId_returnsInvalidArgument() {
        CreateTenantRequest request =
                CreateTenantRequest.newBuilder()
                        .setUserId("not-a-uuid")
                        .setName("Acme Corp")
                        .build();

        AtomicReference<Throwable> errRef = new AtomicReference<>();
        service.createTenant(
                request,
                new StreamObserver<>() {
                    @Override
                    public void onNext(CreateTenantResponse value) {}

                    @Override
                    public void onError(Throwable t) {
                        errRef.set(t);
                    }

                    @Override
                    public void onCompleted() {}
                });

        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    void createTenant_userNotFound_returnsFailedPrecondition() {
        UUID userId = UUID.randomUUID();
        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.empty());
        CreateTenantRequest request =
                CreateTenantRequest.newBuilder()
                        .setUserId(userId.toString())
                        .setName("Acme Corp")
                        .build();

        AtomicReference<Throwable> errRef = new AtomicReference<>();
        service.createTenant(
                request,
                new StreamObserver<>() {
                    @Override
                    public void onNext(CreateTenantResponse value) {}

                    @Override
                    public void onError(Throwable t) {
                        errRef.set(t);
                    }

                    @Override
                    public void onCompleted() {}
                });

        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("FAILED_PRECONDITION");
    }
}
