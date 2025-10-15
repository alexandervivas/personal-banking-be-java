package com.eureckah.banking.tenants.infrastructure.adapter.out.messaging;

import com.eureckah.banking.tenants.application.dto.TenantView;
import com.eureckah.banking.tenants.application.dto.UserView;
import com.eureckah.banking.tenants.application.port.out.messaging.TenantEventsPublisher;
import com.eureckah.banking.tenants.domain.events.TenantCreated;
import com.eureckah.banking.tenants.domain.events.TenantEvent;
import com.eureckah.banking.tenants.domain.events.UserCreated;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class TenantRabbitMQPublisher implements TenantEventsPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitMQConfig config;

    @Value("${messaging.events.routing-key:events}")
    private String defaultRoutingKey;

    public TenantRabbitMQPublisher(
            RabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin, RabbitMQConfig config) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.config = config;
    }

    @Override
    public void publishTenantEvent(TenantEvent event) {
        EventPayload payload = toAvroPayload(event);
        String exchange = config.exchangePrefix() + payload.name;

        // declare exchange if not exists
        TopicExchange ex = new TopicExchange(exchange, true, false);
        rabbitAdmin.declareExchange(ex);

        rabbitTemplate.convertAndSend(
                exchange,
                defaultRoutingKey,
                payload.bytes,
                message -> {
                    // CloudEvents binary content mode over AMQP 0.9.1 (RabbitMQ) via headers
                    message.getMessageProperties().setContentType("application/avro");

                    Map<String, Object> headers = new HashMap<>();
                    headers.put("ce_specversion", "1.0");
                    headers.put("ce_type", payload.name);
                    headers.put("ce_source", "tenants-service");
                    headers.put("ce_id", payload.id);
                    headers.put("ce_time", payload.time);
                    if (payload.subject != null) headers.put("ce_subject", payload.subject);
                    headers.put("ce_datacontenttype", "application/avro");
                    headers.put("ce_dataschema", "urn:avro:schema:" + payload.schema.getFullName());

                    // Keep schema as non-CE helper header for convenience
                    headers.put("schema", payload.schema.toString());

                    message.getMessageProperties().getHeaders().putAll(headers);
                    return message;
                });
    }

    private EventPayload toAvroPayload(TenantEvent event) {
        try {
            if (event instanceof TenantCreated tc) {
                Schema schema = loadSchema("contracts/events/v1/tenant-created.avsc");
                GenericRecord record = new GenericData.Record(schema);
                TenantView tv = tc.tenant();
                String id = java.util.UUID.randomUUID().toString();
                String time = java.time.Instant.now().toString();
                record.put("eventId", id);
                record.put("occurredAt", time);
                record.put("tenantId", tv.id().toString());
                record.put("name", tv.name());
                record.put("ownerId", tv.ownerId().toString());
                byte[] bytes = serialize(schema, record);
                return new EventPayload(
                        "tenant.created.v1", schema, bytes, id, time, tv.id().toString());
            } else if (event instanceof UserCreated uc) {
                Schema schema = loadSchema("contracts/events/v1/user-created.avsc");
                GenericRecord record = new GenericData.Record(schema);
                UserView uv = uc.userView();
                String id = java.util.UUID.randomUUID().toString();
                String time = java.time.Instant.now().toString();
                record.put("eventId", id);
                record.put("occurredAt", time);
                record.put("userId", uv.id().toString());
                record.put("name", uv.name());
                record.put("email", uv.email());
                byte[] bytes = serialize(schema, record);
                return new EventPayload(
                        "user.created.v1", schema, bytes, id, time, uv.id().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize event to Avro", e);
        }
        throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
    }

    private Schema loadSchema(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (java.io.InputStream is = resource.getInputStream()) {
            String schemaStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new Schema.Parser().parse(schemaStr);
        }
    }

    private byte[] serialize(Schema schema, GenericRecord record) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        DatumWriter<GenericRecord> writer = new SpecificDatumWriter<>(schema);
        writer.write(record, encoder);
        encoder.flush();
        return out.toByteArray();
    }

    private record EventPayload(
            String name, Schema schema, byte[] bytes, String id, String time, String subject) {}
}
