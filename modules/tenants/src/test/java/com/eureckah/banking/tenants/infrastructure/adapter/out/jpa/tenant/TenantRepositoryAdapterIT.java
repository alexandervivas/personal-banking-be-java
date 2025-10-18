package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.eureckah.banking.tenants.domain.model.Tenant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@Import(TenantRepositoryAdapter.class)
class TenantRepositoryAdapterIT {

    @Autowired private TenantRepositoryAdapter adapter;

    @Test
    void save_and_findById_and_delete_and_findAll() {
        // initially empty
        List<Tenant> initial = adapter.findAll();
        assertThat(initial).isEmpty();

        // save
        Tenant toCreate = Tenant.create("Acme Corp");
        UUID id = adapter.save(toCreate);
        assertThat(id).isNotNull();

        // findById
        Optional<Tenant> loaded = adapter.findById(id);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(id);
        assertThat(loaded.get().getName()).isEqualTo("Acme Corp");

        // findAll contains one
        List<Tenant> all = adapter.findAll();
        assertThat(all).extracting(Tenant::getId).containsExactly(id);

        // delete
        adapter.delete(id);
        assertThat(adapter.findById(id)).isEmpty();
        assertThat(adapter.findAll()).isEmpty();
    }
}
