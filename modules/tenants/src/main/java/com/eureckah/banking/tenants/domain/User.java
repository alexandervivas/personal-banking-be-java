package com.eureckah.banking.tenants.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private UserId id;
    private String name;
    private String email;

}
