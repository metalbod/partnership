// Mirrors the four backend services' DTOs. See each service's dto/ package for the
// source of truth (com.company.partnership.*.dto.*) – keep these in sync by hand for
// now, same as /api-contracts is hand-kept in sync with the event schemas.

export type UUID = string;
export type PriceKind = "FIXED" | "PERCENTAGE";

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
  /** The vendor's unit price inside a bundle: a MYR amount, or a % of the bundle cost. */
  priceType: PriceKind;
  priceValue: number;
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
  /** What the bundle costs this partner's customers (MYR), and the partner's share of it. */
  subscriptionPrice: number;
  partnerShareType: PriceKind;
  partnerShareValue: number;
}

// --- transaction-profitshare-service (8084) ---

export interface TransactionOffering {
  offeringId: UUID;
  offeringName: string;
  vendorId: UUID;
  priceType: PriceKind;
  priceValue: number;
  amount: number;
}

/** A customer taking a partner's whole bundle. The offerings and breakdown are a purchase-time snapshot. */
export interface Transaction {
  id: UUID;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  subscriptionId: UUID | null;
  partnerId: UUID;
  bundleId: UUID;
  bundleName: string | null;
  bundleVersion: number | null;
  ecoSystemId: UUID;
  amount: number;
  offerings: TransactionOffering[];
  vendorTotal: number;
  partnerAmount: number;
  companyAmount: number;
  premium: number | null;
  sumInsured: number | null;
  policyNumber: string | null;
  transactionTimestamp: string;
  includedInReportId: UUID | null;
}

export interface ProfitShareReport {
  id: UUID;
  periodStart: string;
  periodEnd: string;
  generatedAt: string;
  exportFileReference: string | null;
  status: "GENERATED" | "EXPORTED";
  transactionCount: number;
  totalAmount: number;
  vendorTotal: number;
  partnerTotal: number;
  companyTotal: number;
  vendors: { vendorId: UUID; amount: number }[];
}
