import { useEffect, useMemo, useState } from "react";
import { api } from "../api";
import type { Bundle, Offering, Partner, ProfitShareReport, Subscription, Transaction, Vendor } from "../types";
import { ErrorBanner, StatusBadge, shortId } from "../components";
import { breakdown, myr, priceLabel } from "../pricing";

export function TransactionsPage() {
  const [partners, setPartners] = useState<Partner[]>([]);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [offerings, setOfferings] = useState<Offering[]>([]);
  const [subs, setSubs] = useState<Subscription[]>([]);
  const [bundles, setBundles] = useState<Record<string, Bundle>>({});
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [customerName, setCustomerName] = useState("");
  const [customerPhone, setCustomerPhone] = useState("");
  const [customerEmail, setCustomerEmail] = useState("");
  const [partnerId, setPartnerId] = useState("");
  const [subscriptionId, setSubscriptionId] = useState("");
  const [premium, setPremium] = useState("");
  const [sumInsured, setSumInsured] = useState("");
  const [policyNumber, setPolicyNumber] = useState("");

  const [periodStart, setPeriodStart] = useState(() => new Date(Date.now() - 29 * 864e5).toISOString().slice(0, 10));
  const [periodEnd, setPeriodEnd] = useState(() => new Date().toISOString().slice(0, 10));
  const [report, setReport] = useState<ProfitShareReport | null>(null);

  const loadTransactions = () => api.transactions.list().then(setTransactions).catch((e) => setError(String(e)));

  useEffect(() => {
    api.partners.list().then(setPartners).catch((e) => setError(String(e)));
    loadTransactions();
    api.vendors
      .list()
      .then(async (vs) => {
        setVendors(vs);
        setOfferings((await Promise.all(vs.map((v) => api.offerings.listByVendor(v.id)))).flat());
      })
      .catch((e) => setError(String(e)));
  }, []);

  // A customer takes one of the selected partner's programmes – its whole bundle.
  useEffect(() => {
    setSubscriptionId("");
    setSubs([]);
    if (!partnerId) return;
    api.subscriptions
      .listByPartner(partnerId)
      .then(async (list) => {
        const sellable = list.filter((s) => s.status === "ACTIVE" || s.status === "PENDING_RECONSENT");
        const loaded = await Promise.all(sellable.map((s) => api.bundles.get(s.bundleId)));
        setBundles((prev) => ({ ...prev, ...Object.fromEntries(loaded.map((b) => [b.id, b])) }));
        setSubs(sellable);
      })
      .catch((e) => setError(String(e)));
  }, [partnerId]);

  const sub = subs.find((s) => s.id === subscriptionId);
  const bundle = sub ? bundles[sub.bundleId] : undefined;
  const bundleOfferings = useMemo(
    () => (bundle ? bundle.offeringIds.map((id) => offerings.find((o) => o.id === id)).filter((o): o is Offering => !!o) : []),
    [bundle, offerings],
  );
  const includesInsurance = bundleOfferings.some((o) => o.offeringType === "INSURANCE");
  const preview = sub && sub.subscriptionPrice > 0 ? breakdown(sub.subscriptionPrice, sub.partnerShareType, sub.partnerShareValue, bundleOfferings) : null;

  async function recordTransaction(e: React.FormEvent) {
    e.preventDefault();
    if (!sub) return;
    setError(null);
    setBusy(true);
    try {
      await api.transactions.record({
        customerName: customerName.trim(),
        customerPhone: customerPhone.trim(),
        customerEmail: customerEmail.trim(),
        subscriptionId: sub.id,
        ...(includesInsurance && policyNumber
          ? { premium: Number(premium), sumInsured: Number(sumInsured), policyNumber: policyNumber.trim() }
          : {}),
      });
      await loadTransactions();
      setCustomerName("");
      setCustomerPhone("");
      setCustomerEmail("");
      setPremium("");
      setSumInsured("");
      setPolicyNumber("");
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function runProfitShare(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      setReport(await api.profitShare.run(periodStart, periodEnd));
      await loadTransactions();
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  const partnerName = (id: string) => partners.find((p) => p.id === id)?.name ?? shortId(id);
  const vendorName = (id: string) => vendors.find((v) => v.id === id)?.name ?? shortId(id);

  return (
    <div className="panel">
      <div className="card">
        <h2>Register a customer</h2>
        <form onSubmit={recordTransaction}>
          <label>
            Customer name
            <input value={customerName} onChange={(e) => setCustomerName(e.target.value)} required />
          </label>
          <label>
            Contact number
            <input value={customerPhone} onChange={(e) => setCustomerPhone(e.target.value)} type="tel" required />
          </label>
          <label>
            Email
            <input value={customerEmail} onChange={(e) => setCustomerEmail(e.target.value)} type="email" required />
          </label>
          <label>
            Partner
            <select value={partnerId} onChange={(e) => setPartnerId(e.target.value)} required>
              <option value="">Select…</option>
              {partners.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Partner's bundle (taken as a whole)
            <select value={subscriptionId} onChange={(e) => setSubscriptionId(e.target.value)} required disabled={!partnerId}>
              <option value="">{partnerId ? (subs.length ? "Select…" : "No subscriptions") : "Pick a partner first"}</option>
              {subs.map((s) =>
                bundles[s.bundleId] ? (
                  <option key={s.id} value={s.id}>
                    {bundles[s.bundleId].name} (v{bundles[s.bundleId].version}) · {myr(s.subscriptionPrice)}
                    {s.status === "PENDING_RECONSENT" ? " · pending re-consent" : ""}
                  </option>
                ) : null,
              )}
            </select>
          </label>

          {sub && preview && (
            <div className="mono" style={{ fontSize: "0.8rem" }}>
              <div>Customer pays: {myr(sub.subscriptionPrice)}</div>
              {preview.lines.map((l) => (
                <div key={l.offering.id}>
                  · {l.offering.name} ({priceLabel(l.offering.priceType, l.offering.priceValue)}): {myr(l.amount)}
                </div>
              ))}
              <div>Partner ({priceLabel(sub.partnerShareType, sub.partnerShareValue)}): {myr(preview.partner)}</div>
              <div style={{ color: preview.company < 0 ? "var(--danger)" : undefined }}>Company keeps: {myr(preview.company)}</div>
              <div>These terms are recorded with the transaction.</div>
            </div>
          )}
          {sub && !preview && <p className="error">This programme has no bundle cost set, so it can't take customers yet.</p>}

          {includesInsurance && (
            <>
              <label>
                Policy number (insurance in this bundle)
                <input value={policyNumber} onChange={(e) => setPolicyNumber(e.target.value)} />
              </label>
              {policyNumber && (
                <>
                  <label>
                    Premium (MYR)
                    <input value={premium} onChange={(e) => setPremium(e.target.value)} type="number" step="0.01" min="0" required />
                  </label>
                  <label>
                    Sum insured (MYR)
                    <input value={sumInsured} onChange={(e) => setSumInsured(e.target.value)} type="number" step="0.01" min="0" required />
                  </label>
                </>
              )}
            </>
          )}
          <button className="primary" disabled={busy || !sub || !preview || preview.company < 0}>
            Record transaction
          </button>
        </form>

        <h2 style={{ marginTop: 24 }}>Run profit-share calculation</h2>
        <form onSubmit={runProfitShare}>
          <label>
            Period start
            <input value={periodStart} onChange={(e) => setPeriodStart(e.target.value)} type="date" />
          </label>
          <label>
            Period end
            <input value={periodEnd} onChange={(e) => setPeriodEnd(e.target.value)} type="date" />
          </label>
          <button className="primary" disabled={busy}>
            Run (reporting-only, no payment execution)
          </button>
        </form>
        {report && (
          <div style={{ fontSize: "0.85rem", marginTop: 8 }}>
            <p>
              Report <span className="mono">{shortId(report.id)}</span> <StatusBadge status={report.status} /> ·{" "}
              {report.transactionCount} transaction(s) · {report.periodStart} – {report.periodEnd}
            </p>
            <div className="mono">
              <div>Total: {myr(report.totalAmount)}</div>
              <div>Vendors: {myr(report.vendorTotal)}</div>
              {report.vendors.map((v) => (
                <div key={v.vendorId}>· {vendorName(v.vendorId)}: {myr(v.amount)}</div>
              ))}
              <div>Partners: {myr(report.partnerTotal)}</div>
              <div>Company: {myr(report.companyTotal)}</div>
            </div>
          </div>
        )}

        <ErrorBanner message={error} />
      </div>

      <div className="card">
        <h2>Transactions</h2>
        {transactions.length === 0 && <p className="empty">Nothing recorded yet.</p>}
        {transactions.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Customer</th>
                <th>Partner · bundle</th>
                <th>Paid</th>
                <th>Breakdown at purchase</th>
                <th>Report</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((t) => (
                <tr key={t.id}>
                  <td>
                    {t.customerName}
                    <div className="mono">{t.customerEmail}</div>
                    <div className="mono">{t.customerPhone}</div>
                  </td>
                  <td>
                    {partnerName(t.partnerId)}
                    <div className="mono">
                      {t.bundleName ?? shortId(t.bundleId)}
                      {t.bundleVersion ? ` v${t.bundleVersion}` : ""}
                    </div>
                  </td>
                  <td>{myr(t.amount)}</td>
                  <td className="mono">
                    {t.offerings.length === 0 && <div>No snapshot (legacy)</div>}
                    {t.offerings.map((o) => (
                      <div key={o.offeringId}>
                        {o.offeringName}: {myr(o.amount)}
                      </div>
                    ))}
                    {t.offerings.length > 0 && (
                      <>
                        <div>Partner: {myr(t.partnerAmount)}</div>
                        <div>Company: {myr(t.companyAmount)}</div>
                      </>
                    )}
                  </td>
                  <td>{t.includedInReportId ? <span className="mono">{shortId(t.includedInReportId)}</span> : "Pending"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
