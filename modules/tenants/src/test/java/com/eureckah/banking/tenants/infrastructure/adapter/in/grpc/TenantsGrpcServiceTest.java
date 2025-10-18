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
import com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.handlers.CreateTenantIdempotentHandler;
import com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.idempotency.IdempotencyContext;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency.IdempotencyRecordJpaEntity;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency.IdempotencyRecordJpaRepository;
import com.eureckah.banking.tenants.infrastructure.idempotency.IdempotencyService;
import com.eureckah.banking.tenants.infrastructure.idempotency.IdempotentOperationCoordinator;
import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;

import io.grpc.Context;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class TenantsGrpcServiceTest {

    private CreateTenantUseCase createTenantUseCase;
    private GetUserUseCase getUserUseCase;
    private TenantsGrpcService service;
    private IdempotencyRecordJpaRepository idempotencyRepo;

    @BeforeEach
    void setup() {
        createTenantUseCase = mock(CreateTenantUseCase.class);
        getUserUseCase = mock(GetUserUseCase.class);
        idempotencyRepo = mock(IdempotencyRecordJpaRepository.class);
        var idemSvc = new IdempotencyService(idempotencyRepo);
        var handler =
                new CreateTenantIdempotentHandler(
                        new IdempotentOperationCoordinator(idemSvc),
                        createTenantUseCase,
                        getUserUseCase);
        service = new TenantsGrpcService(handler);
    }

    @Test
    void createTenant_success() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Alice").email("alice@example.com").build();

        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.of(user));
        when(createTenantUseCase.handle(any(CreateTenantCommand.class)))
                .thenReturn(Optional.of(tenantId));

        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme Corp").build();

        AtomicReference<CreateTenantResponse> respRef = new AtomicReference<>();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        String route = "tenants.createTenant.v1";
        when(idempotencyRepo.findByRouteAndKeyAndUserId(route, "test-key", userId))
                .thenReturn(Optional.empty());
        when(idempotencyRepo.save(any(IdempotencyRecordJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Context ctx =
                IdempotencyContext.withValues(Context.current(), "test-key", userId.toString());
        ctx.run(
                () ->
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
                                }));

        assertThat(errRef.get()).isNull();
        assertThat(respRef.get()).isNotNull();
        assertThat(respRef.get().getTenantId()).isEqualTo(tenantId.toString());

        verify(getUserUseCase, times(2)).handle(argThat(q -> q.id().equals(userId)));
        verify(createTenantUseCase)
                .handle(
                        argThat(
                                cmd ->
                                        cmd.name().equals("Acme Corp")
                                                && cmd.user().getId().equals(userId)));
    }

    @Test
    void createTenant_invalidUserId_returnsInvalidArgument() {
        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme Corp").build();

        AtomicReference<Throwable> errRef = new AtomicReference<>();
        Context ctx = IdempotencyContext.withValues(Context.current(), "k", "not-a-uuid");
        ctx.run(
                () ->
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
                                }));

        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    void createTenant_userNotFound_returnsFailedPrecondition() {
        UUID userId = UUID.randomUUID();
        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.empty());
        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme Corp").build();

        AtomicReference<Throwable> errRef = new AtomicReference<>();
        Context ctx = IdempotencyContext.withValues(Context.current(), "key", userId.toString());
        ctx.run(
                () ->
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
                                }));

        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("FAILED_PRECONDITION");
    }

    @Test
    void createTenant_idempotencyReplay_returnsStoredResponseWithoutCallingUseCase() {
        // Arrange metadata via IdempotencyContext by simulating interceptor behavior is tricky in
        // unit test.
        // Instead, we call private static Context keys through setting current context.
        String idemKey = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        String route = "tenants.createTenant.v1";

        var stored =
                IdempotencyRecordJpaEntity.builder()
                        .route(route)
                        .key(idemKey)
                        .userId(userId)
                        .resourceId(UUID.randomUUID().toString())
                        .resourceType("TENANT")
                        .statusCode(201)
                        .build();
        when(idempotencyRepo.findByRouteAndKeyAndUserId(route, idemKey, userId))
                .thenReturn(java.util.Optional.of(stored));

        // Build request
        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme Corp").build();

        // Set up IdempotencyContext by attaching to current gRPC Context
        Context ctx = IdempotencyContext.withValues(Context.current(), idemKey, userId.toString());
        AtomicReference<CreateTenantResponse> respRef = new AtomicReference<>();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        ctx.run(
                () ->
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
                                }));

        // Assert we did not call use cases and replayed stored response
        assertThat(errRef.get()).isNull();
        assertThat(respRef.get()).isNotNull();
        assertThat(respRef.get().getTenantId()).isEqualTo(stored.getResourceId());
        verifyNoInteractions(getUserUseCase);
        verifyNoInteractions(createTenantUseCase);
    }

    @Test
    void createTenant_withIdempotencyKey_persistsPlaceholderAndFinal() {
        // Arrange context and mocks
        String idemKey = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Alice").email("alice@example.com").build();

        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.of(user));
        when(createTenantUseCase.handle(any(CreateTenantCommand.class)))
                .thenReturn(Optional.of(tenantId));

        String route = "tenants.createTenant.v1";
        // First lookup: no record (no replay). Second lookup (during markCreated): existing
        // placeholder
        var placeholder =
                IdempotencyRecordJpaEntity.builder()
                        .route(route)
                        .key(idemKey)
                        .userId(userId)
                        .build();
        //noinspection unchecked
        when(idempotencyRepo.findByRouteAndKeyAndUserId(route, idemKey, userId))
                .thenReturn(Optional.empty(), Optional.of(placeholder));
        // Save returns the same entity passed so we can inspect captured values
        when(idempotencyRepo.save(any(IdempotencyRecordJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Request and observer
        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme Corp").build();
        AtomicReference<CreateTenantResponse> respRef = new AtomicReference<>();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        Context ctx = IdempotencyContext.withValues(Context.current(), idemKey, userId.toString());
        ctx.run(
                () ->
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
                                }));

        // Assert response OK
        assertThat(errRef.get()).isNull();
        assertThat(respRef.get()).isNotNull();
        assertThat(respRef.get().getTenantId()).isEqualTo(tenantId.toString());

        // Verify repo interactions: placeholder then finalization
        ArgumentCaptor<IdempotencyRecordJpaEntity> recCaptor =
                ArgumentCaptor.forClass(IdempotencyRecordJpaEntity.class);
        verify(idempotencyRepo, times(2)).save(recCaptor.capture());
        var savedEntities = recCaptor.getAllValues();
        // First save is placeholder (no status code / resource)
        assertThat(savedEntities.get(0).getStatusCode()).isNull();
        // Second save is final (201 and resource id set)
        assertThat(savedEntities.get(1).getStatusCode()).isEqualTo(201);
        assertThat(savedEntities.get(1).getResourceId()).isEqualTo(tenantId.toString());
    }

    @Test
    void createTenant_useCaseReturnsEmpty_failedPrecondition() {
        String idemKey = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Alice").email("alice@example.com").build();

        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.of(user));
        when(createTenantUseCase.handle(any(CreateTenantCommand.class)))
                .thenReturn(Optional.empty());

        String route = "tenants.createTenant.v1";
        when(idempotencyRepo.findByRouteAndKeyAndUserId(route, idemKey, userId))
                .thenReturn(Optional.empty());
        when(idempotencyRepo.save(any(IdempotencyRecordJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme Corp").build();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        Context ctx = IdempotencyContext.withValues(Context.current(), idemKey, userId.toString());
        ctx.run(
                () ->
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
                                }));

        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("FAILED_PRECONDITION");
        // Placeholder then terminal failure should be saved
        ArgumentCaptor<IdempotencyRecordJpaEntity> recCaptor2 =
                ArgumentCaptor.forClass(IdempotencyRecordJpaEntity.class);
        verify(idempotencyRepo, times(2)).save(recCaptor2.capture());
        var saved = recCaptor2.getAllValues();
        assertThat(saved.get(0).getStatusCode()).isNull(); // placeholder
        assertThat(saved.get(1).getStatusCode()).isEqualTo(412); // terminal failure
        assertThat(saved.get(1).getResourceId()).isNull();
    }

    @Test
    void createTenant_markCreatedFallback_whenNoPlaceholderSaved_savesFinalRecord() {
        // Simulate race on placeholder (save throws duplicate); under new semantics we should not
        // execute
        String idemKey = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Bob").email("bob@example.com").build();

        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.of(user));

        String route = "tenants.createTenant.v1";
        //noinspection unchecked
        when(idempotencyRepo.findByRouteAndKeyAndUserId(route, idemKey, userId))
                .thenReturn(Optional.empty(), Optional.empty());
        // First save (placeholder) throws duplicate
        when(idempotencyRepo.save(any(IdempotencyRecordJpaEntity.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("dup"));

        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Contoso").build();
        AtomicReference<CreateTenantResponse> respRef = new AtomicReference<>();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        Context ctx = IdempotencyContext.withValues(Context.current(), idemKey, userId.toString());
        ctx.run(
                () ->
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
                                }));

        assertThat(respRef.get()).isNull();
        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("ABORTED");
        // Only placeholder attempt occurred; mutation not executed; no final save
        verify(createTenantUseCase, never()).handle(any());
        verify(idempotencyRepo, times(1)).save(any(IdempotencyRecordJpaEntity.class));
    }

    @Test
    void createTenant_useCaseThrowsInvalidCommand_marksFailureAndReturnsInvalidArgument() {
        String idemKey = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Alice").email("alice@example.com").build();

        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.of(user));
        when(createTenantUseCase.handle(any(CreateTenantCommand.class)))
                .thenThrow(
                        new com.eureckah.banking.tenants.application.exceptions
                                .InvalidCommandException(
                                com.eureckah.banking.tenants.application.commands
                                        .CreateTenantCommand.class,
                                "bad"));

        String route = "tenants.createTenant.v1";
        when(idempotencyRepo.findByRouteAndKeyAndUserId(route, idemKey, userId))
                .thenReturn(Optional.empty());
        when(idempotencyRepo.save(any(IdempotencyRecordJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme").build();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        Context ctx = IdempotencyContext.withValues(Context.current(), idemKey, userId.toString());
        ctx.run(
                () ->
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
                                }));

        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("INVALID_ARGUMENT");

        ArgumentCaptor<IdempotencyRecordJpaEntity> captor =
                ArgumentCaptor.forClass(IdempotencyRecordJpaEntity.class);
        verify(idempotencyRepo, times(2)).save(captor.capture());
        var saved = captor.getAllValues();
        assertThat(saved.get(0).getStatusCode()).isNull(); // placeholder
        assertThat(saved.get(1).getStatusCode()).isEqualTo(422); // terminal failure
        assertThat(saved.get(1).getResourceId()).isNull();
    }

    @Test
    void createTenant_useCaseThrowsFailedCommand_marksFailureAndReturnsInternal() {
        String idemKey = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Alice").email("alice@example.com").build();

        when(getUserUseCase.handle(any(GetUserQuery.class))).thenReturn(Optional.of(user));
        when(createTenantUseCase.handle(any(CreateTenantCommand.class)))
                .thenThrow(
                        new com.eureckah.banking.tenants.application.exceptions
                                .FailedCommandException(
                                com.eureckah.banking.tenants.application.commands
                                        .CreateTenantCommand.class,
                                new RuntimeException("oops")));

        String route = "tenants.createTenant.v1";
        when(idempotencyRepo.findByRouteAndKeyAndUserId(route, idemKey, userId))
                .thenReturn(Optional.empty());
        when(idempotencyRepo.save(any(IdempotencyRecordJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateTenantRequest request = CreateTenantRequest.newBuilder().setName("Acme").build();
        AtomicReference<Throwable> errRef = new AtomicReference<>();

        Context ctx = IdempotencyContext.withValues(Context.current(), idemKey, userId.toString());
        ctx.run(
                () ->
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
                                }));

        assertThat(errRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errRef.get();
        assertThat(ex.getStatus().getCode().name()).isEqualTo("INTERNAL");

        ArgumentCaptor<IdempotencyRecordJpaEntity> captor =
                ArgumentCaptor.forClass(IdempotencyRecordJpaEntity.class);
        verify(idempotencyRepo, times(2)).save(captor.capture());
        var saved = captor.getAllValues();
        assertThat(saved.get(0).getStatusCode()).isNull(); // placeholder
        assertThat(saved.get(1).getStatusCode()).isEqualTo(500); // terminal failure
        assertThat(saved.get(1).getResourceId()).isNull();
    }
}
