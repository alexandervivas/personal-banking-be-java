Feature: Tenants management
  Scenario: Create tenant via HTTP
    Given simulated auth
    When POST /v1/tenants
    Then a tenant UUID is created and the caller is the owner
