package com.company.partnership.partnersubscription.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * An organisation that subscribes to whole bundles to extend offerings to its own
 * consumer population (e.g. a higher learning institution). BRD Section 4/5.1.
 */
@Entity
@Table(name = "partner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Partner {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String contactEmail;
    private String contactPhone;

    @Column(length = 2000)
    private String commercialTerms;

    /** FR-ADM-02 / FR-SAL-01: who onboarded this partner, for Sales KPI reporting. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardedBy onboardedBy = OnboardedBy.ADMIN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerStatus status = PartnerStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum PartnerStatus {
        ACTIVE, INACTIVE
    }

    public enum OnboardedBy {
        ADMIN, SALES
    }
}
