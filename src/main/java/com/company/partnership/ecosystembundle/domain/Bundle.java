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
 * A specific mix-and-match selection of vendor Offerings within an EcoSystem.
 *
 * IMPORTANT BUSINESS RULE (BRD Section 5.1 / FR-BUN-03): once a bundle is
 * offered/subscribed, its offering composition is LOCKED. Any change requires
 * creating a NEW BundleVersion (see {@link #version}) and triggering partner
 * re-consent (FR-BUN-04) \u2013 never mutate offeringIds on a PUBLISHED bundle.
 */
@Entity
@Table(name = "bundle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bundle {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eco_system_id", nullable = false)
    private EcoSystem ecoSystem;

    @NotBlank
    @Column(nullable = false)
    private String name;

    /** Version number within this bundle's lineage; incremented on each re-composition. */
    @Column(nullable = false)
    private int version = 1;

    /** If this bundle supersedes an earlier version, points to that prior Bundle's id. */
    private UUID supersedesBundleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BundleStatus status = BundleStatus.DRAFT;

    /** Offering IDs locked into this bundle version (immutable once status = PUBLISHED). */
    @ElementCollection
    @CollectionTable(name = "bundle_offering", joinColumns = @JoinColumn(name = "bundle_id"))
    @Column(name = "offering_id")
    private List<UUID> offeringIds = new ArrayList<>();

    private Instant publishedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum BundleStatus {
        DRAFT, PUBLISHED, SUPERSEDED
    }
}
