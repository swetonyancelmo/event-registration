package com.swetonyancelmo.event_registration.service;

import com.swetonyancelmo.event_registration.dtos.CreateUserDTO;
import com.swetonyancelmo.event_registration.dtos.UserResponseDTO;
import com.swetonyancelmo.event_registration.exception.EmailAlreadyExistsException;
import com.swetonyancelmo.event_registration.exception.ResourceNotFoundException;
import com.swetonyancelmo.event_registration.model.User;
import com.swetonyancelmo.event_registration.model.enums.UserRole;
import com.swetonyancelmo.event_registration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO register(CreateUserDTO dto) {
        return UserResponseDTO.from(createUser(dto.name(), dto.email(), dto.password(), UserRole.USER));
    }

    @Transactional
    public boolean createAdminIfAbsent(String name, String email, String password) {
        if (userRepository.existsByEmail(normalize(email))) {
            return false;
        }
        createUser(name == null ? "Admin" : name, email, password, UserRole.ADMIN);
        return true;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return UserResponseDTO.from(findUser(id));
    }

    @Transactional
    public UserResponseDTO updateRole(UUID id, UserRole role) {
        User user = findUser(id);
        user.setRole(role);
        return UserResponseDTO.from(user);
    }

    private User createUser(String name, String email, String password, UserRole role) {
        String normalizedEmail = normalize(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        try {
            // flush imediato para que a violação da UNIQUE (cadastro concorrente) aconteça aqui
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
    }

    private User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }

}
