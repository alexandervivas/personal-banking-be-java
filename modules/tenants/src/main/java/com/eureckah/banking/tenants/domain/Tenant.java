package com.eureckah.banking.tenants.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant {

    private TenantId id;
    private String name;
}
