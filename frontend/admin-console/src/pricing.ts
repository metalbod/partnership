import type { Offering, PriceKind } from "./types";

const r2 = (n: number) => Math.round(n * 100) / 100;

export const amountOf = (price: number, kind: PriceKind, value: number) =>
  r2(kind === "FIXED" ? value : (price * value) / 100);

export interface Breakdown {
  lines: { offering: Offering; amount: number }[];
  vendorTotal: number;
  partner: number;
  company: number;
}

/** Mirrors transaction-profitshare-service's PricingCalculator, for previews only. */
export function breakdown(price: number, partnerKind: PriceKind, partnerValue: number, offerings: Offering[]): Breakdown {
  const lines = offerings.map((o) => ({ offering: o, amount: amountOf(price, o.priceType, o.priceValue) }));
  const vendorTotal = r2(lines.reduce((a, l) => a + l.amount, 0));
  const partner = amountOf(price, partnerKind, partnerValue);
  return { lines, vendorTotal, partner, company: r2(price - vendorTotal - partner) };
}

export const myr = (n: number) => `RM ${n.toLocaleString("en-MY", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
export const priceLabel = (kind: PriceKind, value: number) => (kind === "FIXED" ? myr(value) : `${value}% of bundle cost`);
