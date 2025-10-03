package com.eureckah.banking.tenants.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    private ProfileId id;
    private Tenant tenant;
    private User user;
    private Role role;

}
