package com.swetonyancelmo.event_registration.controller;

import com.swetonyancelmo.event_registration.dtos.CreateUserDTO;
import com.swetonyancelmo.event_registration.dtos.UserResponseDTO;
import com.swetonyancelmo.event_registration.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> creatingUserWithRoleUser(@RequestBody @Valid CreateUserDTO request) {
        UserResponseDTO userResponseDTO = userService.createUserWithRoleUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    @PostMapping("/admin")
    public ResponseEntity<UserResponseDTO> creatingUserWithRoleAdmin(@RequestBody @Valid CreateUserDTO request) {
        UserResponseDTO userResponseDTO = userService.createUserWithRoleAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

}
