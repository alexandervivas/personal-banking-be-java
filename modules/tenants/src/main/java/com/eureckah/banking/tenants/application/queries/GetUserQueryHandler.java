package com.eureckah.banking.tenants.application.queries;

import com.eureckah.banking.tenants.application.port.in.GetUserUseCase;
import com.eureckah.banking.tenants.application.port.out.storage.UserRepository;
import com.eureckah.banking.tenants.domain.model.User;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetUserQueryHandler implements GetUserUseCase {
    private final UserRepository userRepository;

    public GetUserQueryHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserQuery query) {
        return userRepository.findById(query.id());
    }
}
