package com.company.partnership.ecosystembundle.dto;

import jakarta.validation.constraints.NotBlank;

public record EcoSystemRequest(@NotBlank String name, String theme) {
}
