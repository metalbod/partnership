// Direct browser -> service calls, one base URL per backend service (each is its own
// deployable with its own port – see workspace-root CLAUDE.md). Production target is
// all four behind a single API Gateway/ALB (TDD Section 4.1); until that exists, each
// service also needs the CORS config added alongside this frontend to allow the
// Vite dev origin (see each service's config/CorsConfig.java).

const VENDOR_OFFERING = "http://localhost:8081";
const ECOSYSTEM_BUNDLE = "http://localhost:8082";
const PARTNER_SUBSCRIPTION = "http://localhost:8083";
const TRANSACTION_PROFITSHARE = "http://localhost:8084";

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    ...options,
    headers: { "Content-Type": "application/json", ...options?.headers },
  });
  if (!res.ok) {
    const body = await res.text();
    throw new Error(`${options?.method ?? "GET"} ${url} -> ${res.status}: ${body}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

const get = <T,>(url: string) => request<T>(url);
const post = <T,>(url: string, body: unknown) =>
  request<T>(url, { method: "POST", body: JSON.stringify(body) });

export const api = {
  vendors: {
    list: () => get<import("./types").Vendor[]>(`${VENDOR_OFFERING}/v1/vendors`),
    create: (body: { name: string; contactEmail?: string; contactPhone?: string }) =>
      post<import("./types").Vendor>(`${VENDOR_OFFERING}/v1/vendors`, body),
  },
  offerings: {
    listByVendor: (vendorId: string) =>
      get<import("./types").Offering[]>(`${VENDOR_OFFERING}/v1/offerings?vendorId=${vendorId}`),
    create: (body: { vendorId: string; name: string; offeringType: "INSURANCE" | "NON_INSURANCE" }) =>
      post<import("./types").Offering>(`${VENDOR_OFFERING}/v1/offerings`, body),
  },
  ecoSystems: {
    list: () => get<import("./types").EcoSystem[]>(`${ECOSYSTEM_BUNDLE}/v1/eco-systems`),
    create: (body: { name: string; theme?: string }) =>
      post<import("./types").EcoSystem>(`${ECOSYSTEM_BUNDLE}/v1/eco-systems`, body),
    assignOffering: (ecoSystemId: string, offeringId: string) =>
      post<import("./types").EcoSystem>(
        `${ECOSYSTEM_BUNDLE}/v1/eco-systems/${ecoSystemId}/offerings/${offeringId}`,
        {},
      ),
  },
  bundles: {
    listByEcoSystem: (ecoSystemId: string) =>
      get<import("./types").Bundle[]>(`${ECOSYSTEM_BUNDLE}/v1/bundles?ecoSystemId=${ecoSystemId}`),
    get: (id: string) => get<import("./types").Bundle>(`${ECOSYSTEM_BUNDLE}/v1/bundles/${id}`),
    create: (body: { ecoSystemId: string; name: string; offeringIds: string[] }) =>
      post<import("./types").Bundle>(`${ECOSYSTEM_BUNDLE}/v1/bundles`, body),
    publish: (id: string) => post<import("./types").Bundle>(`${ECOSYSTEM_BUNDLE}/v1/bundles/${id}/publish`, {}),
    newVersion: (id: string, body: { ecoSystemId: string; name: string; offeringIds: string[] }) =>
      post<import("./types").Bundle>(`${ECOSYSTEM_BUNDLE}/v1/bundles/${id}/new-version`, body),
  },
  partners: {
    list: () => get<import("./types").Partner[]>(`${PARTNER_SUBSCRIPTION}/v1/partners`),
    create: (body: { name: string; onboardedBy: "ADMIN" | "SALES" }) =>
      post<import("./types").Partner>(`${PARTNER_SUBSCRIPTION}/v1/partners`, body),
  },
  subscriptions: {
    listByPartner: (partnerId: string) =>
      get<import("./types").Subscription[]>(`${PARTNER_SUBSCRIPTION}/v1/subscriptions?partnerId=${partnerId}`),
    subscribe: (body: { partnerId: string; bundleId: string; bundleVersion: number }) =>
      post<import("./types").Subscription>(`${PARTNER_SUBSCRIPTION}/v1/subscriptions`, body),
    reconsent: (id: string, newBundleVersion: number) =>
      post<import("./types").Subscription>(
        `${PARTNER_SUBSCRIPTION}/v1/subscriptions/${id}/reconsent?newBundleVersion=${newBundleVersion}`,
        {},
      ),
  },
  transactions: {
    record: (body: {
      consumerEnrolmentId: string;
      offeringId: string;
      bundleId: string;
      ecoSystemId: string;
      partnerId: string;
      vendorId: string;
      amount: number;
      isInsuranceOffering: boolean;
    }) => post<import("./types").Transaction>(`${TRANSACTION_PROFITSHARE}/v1/transactions`, body),
  },
  profitShare: {
    run: (periodStart: string, periodEnd: string) =>
      post<import("./types").ProfitShareReport>(
        `${TRANSACTION_PROFITSHARE}/v1/profit-share/run?periodStart=${periodStart}&periodEnd=${periodEnd}`,
        {},
      ),
  },
};
