package com.eureckah.banking.api.bdd;

import io.cucumber.spring.CucumberContextConfiguration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "grpc.client.tenants.address=static://localhost:" + FakeTenantsServer.PORT,
            "grpc.client.tenants.negotiationType=PLAINTEXT"
        })
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {

    @BeforeAll
    static void beforeAll() {
        FakeTenantsServer.start();
        FakeTenantsServer.clear();
    }

    @AfterAll
    static void afterAll() {
        FakeTenantsServer.stop();
    }
}
