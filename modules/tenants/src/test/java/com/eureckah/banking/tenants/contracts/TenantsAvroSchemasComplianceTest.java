package com.eureckah.banking.tenants.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class TenantsAvroSchemasComplianceTest {

    private static final String REQUIRED_NAMESPACE = "com.eureckah.banking.events.v1";

    @Test
    void docs_schemas_are_cloudevents_aligned_and_namespaced() throws IOException {
        Path contractsDir = findContractsDir();
        List<Path> files =
                List.of(
                        contractsDir.resolve("tenant-created.avsc"),
                        contractsDir.resolve("user-created.avsc"),
                        contractsDir.resolve("account-updated.avsc"),
                        contractsDir.resolve("transaction-imported.avsc"));

        for (Path p : files) {
            String schemaStr = Files.readString(p);
            Schema schema = new Schema.Parser().parse(schemaStr);

            assertThat(schema.getNamespace())
                    .as("namespace for %s", p)
                    .isEqualTo(REQUIRED_NAMESPACE);

            // All events should include eventId and occurredAt to align with CE id/time
            assertThat(schema.getField("eventId")).as("eventId field in %s", p).isNotNull();
            assertThat(schema.getField("occurredAt")).as("occurredAt field in %s", p).isNotNull();
        }
    }

    private static Path findContractsDir() {
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 5 && dir != null; i++) {
            Path candidate = dir.resolve("docs/contracts/events/v1");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException(
                "Could not locate docs/contracts/events/v1 directory from "
                        + Path.of("").toAbsolutePath());
    }
}
