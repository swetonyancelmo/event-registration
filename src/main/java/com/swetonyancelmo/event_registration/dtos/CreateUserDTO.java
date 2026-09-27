package com.swetonyancelmo.event_registration.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateUserDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,

    @Email(message = "Email no formato inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Length(min = 3, max = 60)
    String password
) {
}
