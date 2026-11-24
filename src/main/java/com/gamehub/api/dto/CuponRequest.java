package com.gamehub.api.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public record CuponRequest(@NotBlank String codigo, @NotNull @Min(1) @Max(100) Integer porcentaje, @PositiveOrZero Double tope, Boolean activo, @NotNull @Future LocalDateTime expiraEn) {}
