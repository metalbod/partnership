// Mirrors the four backend services' DTOs. See each service's dto/ package for the
// source of truth (com.company.partnership.*.dto.*) – keep these in sync by hand for
// now, same as /api-contracts is hand-kept in sync with the event schemas.

export type UUID = string;

// --- vendor-offering-service (8081) ---

export interface Vendor {
  id: UUID;
  name: string;
  contactEmail: string | null;
  contactPhone: string | null;
  status: "ACTIVE" | "INACTIVE";
}

export interface Offering {
  id: UUID;
  vendorId: UUID;
  name: string;
  description: string | null;
  offeringType: "INSURANCE" | "NON_INSURANCE";
  status: "ACTIVE" | "INACTIVE";
}

// --- ecosystem-bundle-service (8082) ---

export interface EcoSystem {
  id: UUID;
  name: string;
  theme: string | null;
  status: "ACTIVE" | "INACTIVE";
  assignedOfferingIds: UUID[];
}

export interface Bundle {
  id: UUID;
  ecoSystemId: UUID;
  name: string;
  version: number;
  supersedesBundleId: UUID | null;
  status: "DRAFT" | "PUBLISHED" | "SUPERSEDED";
  offeringIds: UUID[];
}

// --- partner-subscription-service (8083) ---

export interface Partner {
  id: UUID;
  name: string;
  onboardedBy: "ADMIN" | "SALES";
  status: "ACTIVE" | "INACTIVE";
}

export interface Subscription {
  id: UUID;
  partnerId: UUID;
  bundleId: UUID;
  bundleVersionAtSubscription: number;
  status: "ACTIVE" | "PENDING_RECONSENT" | "CANCELLED";
  pendingBundleId: UUID | null;
}

// --- transaction-profitshare-service (8084) ---

export interface Transaction {
  id: UUID;
  offeringId: UUID;
  bundleId: UUID;
  ecoSystemId: UUID;
  partnerId: UUID;
  vendorId: UUID;
  amount: string;
  isInsuranceOffering: boolean;
  transactionTimestamp: string;
}

export interface ProfitShareReport {
  id: UUID;
  periodStart: string;
  periodEnd: string;
  generatedAt: string;
  exportFileReference: string | null;
  status: "GENERATED" | "EXPORTED";
}
