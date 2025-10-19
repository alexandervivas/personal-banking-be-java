package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.shared.application.port.in.QueryUseCase;
import com.eureckah.banking.tenants.application.queries.GetUserQuery;
import com.eureckah.banking.tenants.domain.model.User;

import java.util.Optional;

public interface GetUserUseCase extends QueryUseCase<GetUserQuery, Optional<User>> {}
