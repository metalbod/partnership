package com.company.partnership.ecosystembundle.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A themed collection/compilation of vendors and offerings (e.g. "Education",
 * "Automotive", "Healthcare", "Travel"). May exist with assigned offerings but
 * no Bundles configured yet \u2013 BRD Section 5.1 (FR-ECO-02).
 */
@Entity
@Table(name = "eco_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EcoSystem {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EcoSystemStatus status = EcoSystemStatus.ACTIVE;

    /**
     * Offerings assigned to this eco-system, referenced by ID from vendor-offering-service
     * (no FK across services). EAGER for the same reason as Bundle.offeringIds: always
     * needed for API serialization, and open-in-view is off.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "eco_system_offering", joinColumns = @JoinColumn(name = "eco_system_id"))
    @Column(name = "offering_id")
    private List<UUID> assignedOfferingIds = new ArrayList<>();

    @OneToMany(mappedBy = "ecoSystem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bundle> bundles = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum EcoSystemStatus {
        ACTIVE, INACTIVE
    }
}
