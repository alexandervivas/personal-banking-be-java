package com.eureckah.banking.tenants.infrastructure.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

class TenantRabbitMQPublisherCloudEventsTest {

    @Test
    void buildCloudEventHeaders_produces_required_attributes() {
        // arrange
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
        RabbitMQConfig cfg = new RabbitMQConfig();
        TenantRabbitMQPublisher publisher =
                new TenantRabbitMQPublisher(rabbitTemplate, rabbitAdmin, cfg);

        String schemaStr =
                "{\n"
                        + "  \"type\": \"record\",\n"
                        + "  \"name\": \"TenantCreated\",\n"
                        + "  \"namespace\": \"com.eureckah.banking.events.v1\",\n"
                        + "  \"fields\": [ {\"name\": \"tenantId\", \"type\": \"string\" } ]\n"
                        + "}";
        Schema schema = new Schema.Parser().parse(schemaStr);

        String name = "tenant.created.v1";
        String id = java.util.UUID.randomUUID().toString();
        String time = java.time.Instant.now().toString();
        String subject = "1234";

        // act
        Map<String, Object> headers =
                publisher.buildCloudEventHeaders(name, schema, id, time, subject);

        // assert
        assertThat(headers)
                .containsEntry("ce_specversion", "1.0")
                .containsEntry("ce_type", name)
                .containsEntry("ce_source", "tenants-service")
                .containsEntry("ce_id", id)
                .containsEntry("ce_time", time)
                .containsEntry("ce_subject", subject)
                .containsEntry("ce_datacontenttype", "application/avro")
                .containsEntry(
                        "ce_dataschema",
                        "urn:avro:schema:com.eureckah.banking.events.v1.TenantCreated");
    }

    @Test
    void buildCloudEventHeaders_omits_optional_subject_when_null() {
        // arrange
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
        RabbitMQConfig cfg = new RabbitMQConfig();
        TenantRabbitMQPublisher publisher =
                new TenantRabbitMQPublisher(rabbitTemplate, rabbitAdmin, cfg);

        String schemaStr =
                "{\n"
                        + "  \"type\": \"record\",\n"
                        + "  \"name\": \"UserCreated\",\n"
                        + "  \"namespace\": \"com.eureckah.banking.events.v1\",\n"
                        + "  \"fields\": [ {\"name\": \"userId\", \"type\": \"string\" } ]\n"
                        + "}";
        Schema schema = new Schema.Parser().parse(schemaStr);

        String name = "user.created.v1";
        String id = java.util.UUID.randomUUID().toString();
        String time = java.time.Instant.now().toString();

        // act
        Map<String, Object> headers =
                publisher.buildCloudEventHeaders(name, schema, id, time, null);

        // assert
        assertThat(headers)
                .containsEntry("ce_specversion", "1.0")
                .containsEntry("ce_type", name)
                .containsEntry("ce_source", "tenants-service")
                .containsEntry("ce_id", id)
                .containsEntry("ce_time", time)
                .containsEntry("ce_datacontenttype", "application/avro")
                .containsEntry(
                        "ce_dataschema",
                        "urn:avro:schema:com.eureckah.banking.events.v1.UserCreated");
        assertThat(headers).doesNotContainKey("ce_subject");
    }
}
