package com.eureckah.banking.api.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class TenantsSteps {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    private String simulatedUserId;
    private MvcResult response;

    @Given("simulated auth")
    public void simulated_auth() {
        simulatedUserId = UUID.randomUUID().toString();
        FakeTenantsServer.clear();
    }

    @When("POST /v1/tenants")
    public void post_v1_tenants() throws Exception {
        String payload = "{\"name\":\"Acme Corp\"}";
        response =
                mockMvc.perform(
                                post("/v1/tenants")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .header("X-User-Id", simulatedUserId)
                                        .content(payload))
                        .andExpect(status().isCreated())
                        .andReturn();
    }

    @Then("a tenant UUID is created and the caller is the owner")
    public void a_tenant_uuid_is_created_and_the_caller_is_the_owner() throws Exception {
        String body = response.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode json = objectMapper.readTree(body);
        String tenantId = json.get("tenantId").asText();
        assertThat(
                "tenantId should be a valid UUID", tenantId, matchesPattern("[0-9a-fA-F\\-]{36}"));

        var lastReq = FakeTenantsServer.getLastCreateRequest();
        assertThat("gRPC server should have received a request", lastReq, notNullValue());
        assertThat(lastReq.getUserId(), is(simulatedUserId));
    }
}
