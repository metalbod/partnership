package com.company.partnership.vendoroffering.domain;

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
 * A third-party supplier of one or more Offerings (insurance or non-insurance).
 * See BRD Section 5.1 / SDD Section 5 (Data Model Overview).
 */
@Entity
@Table(name = "vendor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String contactEmail;

    private String contactPhone;

    /** Free-text summary of commercial/profit-share terms agreed with this vendor. */
    @Column(length = 2000)
    private String commercialTerms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status = VendorStatus.ACTIVE;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offering> offerings = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum VendorStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }
}
