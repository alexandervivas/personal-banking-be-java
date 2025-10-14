package com.eureckah.banking.tenants.application.mappers;

import com.eureckah.banking.tenants.application.dto.UserView;
import com.eureckah.banking.tenants.domain.model.User;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class UserMapper {

    public static UserView toView(User user) {
        return new UserView(user.getId(), user.getName(), user.getEmail());
    }
}
