package com.swetonyancelmo.event_registration.service;

import com.swetonyancelmo.event_registration.dtos.CreateUserDTO;
import com.swetonyancelmo.event_registration.dtos.UserResponseDTO;
import com.swetonyancelmo.event_registration.exception.EmailAlreadyExistsException;
import com.swetonyancelmo.event_registration.model.User;
import com.swetonyancelmo.event_registration.model.enums.UserRole;
import com.swetonyancelmo.event_registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDTO createUserWithRoleUser(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email já existente");
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(dto.password()));

        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(savedUser.getId(), savedUser.getName(), savedUser.getEmail(),  savedUser.getRole());
    }

    @Transactional
    public UserResponseDTO createUserWithRoleAdmin(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email já existente");
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(dto.password()));

        user.setRole(UserRole.ADMIN);

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(savedUser.getId(), savedUser.getName(), savedUser.getEmail(),  savedUser.getRole());
    }
}
