package com.company.partnership.ecosystembundle.dto;

import com.company.partnership.ecosystembundle.domain.EcoSystem;

import java.util.List;
import java.util.UUID;

public record EcoSystemResponse(UUID id, String name, String theme,
                                 EcoSystem.EcoSystemStatus status, List<UUID> assignedOfferingIds) {
    public static EcoSystemResponse from(EcoSystem e) {
        return new EcoSystemResponse(e.getId(), e.getName(), e.getTheme(), e.getStatus(), e.getAssignedOfferingIds());
    }
}
