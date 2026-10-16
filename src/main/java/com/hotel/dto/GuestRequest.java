package com.hotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GuestRequest(
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 120, message = "El nombre admite hasta 120 caracteres.") String name,
        @NotBlank(message = "El documento es obligatorio.") @Size(max = 40, message = "El documento admite hasta 40 caracteres.") String document,
        @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no es válido.") @Size(max = 254, message = "El correo es demasiado largo.") String email,
        @NotBlank(message = "El teléfono es obligatorio.") @Pattern(regexp = "[+0-9() .-]{7,25}", message = "El teléfono debe contener entre 7 y 25 caracteres válidos.") String phone) {}
