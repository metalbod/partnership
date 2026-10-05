import { useEffect, useMemo, useState } from "react";
import { api } from "../api";
import type { Bundle, Offering, Partner, ProfitShareReport, Subscription, Transaction } from "../types";
import { ErrorBanner, StatusBadge, shortId } from "../components";

export function TransactionsPage() {
  const [partners, setPartners] = useState<Partner[]>([]);
  const [offerings, setOfferings] = useState<Offering[]>([]);
  const [subs, setSubs] = useState<Subscription[]>([]);
  const [bundles, setBundles] = useState<Record<string, Bundle>>({});
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [customerName, setCustomerName] = useState("");
  const [customerPhone, setCustomerPhone] = useState("");
  const [customerEmail, setCustomerEmail] = useState("");
  const [partnerId, setPartnerId] = useState("");
  const [bundleId, setBundleId] = useState("");
  const [amount, setAmount] = useState("");
  const [premium, setPremium] = useState("");
  const [sumInsured, setSumInsured] = useState("");
  const [policyNumber, setPolicyNumber] = useState("");

  // No list endpoint exists on transaction-profitshare-service yet – this is a
  // client-side record of what's been submitted this session, not a persisted view.
  const [recorded, setRecorded] = useState<Transaction[]>([]);

  const [periodStart, setPeriodStart] = useState(() => new Date(Date.now() - 29 * 864e5).toISOString().slice(0, 10));
  const [periodEnd, setPeriodEnd] = useState(() => new Date().toISOString().slice(0, 10));
  const [report, setReport] = useState<ProfitShareReport | null>(null);

  useEffect(() => {
    api.partners.list().then(setPartners).catch((e) => setError(String(e)));
    // Offering types tell us whether a bundle includes insurance (policy details apply).
    api.vendors
      .list()
      .then((vs) => Promise.all(vs.map((v) => api.offerings.listByVendor(v.id))))
      .then((lists) => setOfferings(lists.flat()))
      .catch((e) => setError(String(e)));
  }, []);

  // A customer subscribes to one of the selected partner's bundles – as a whole.
  useEffect(() => {
    setBundleId("");
    setSubs([]);
    if (!partnerId) return;
    api.subscriptions
      .listByPartner(partnerId)
      .then(async (list) => {
        const loaded = await Promise.all(list.map((s) => api.bundles.get(s.bundleId)));
        setBundles((prev) => ({ ...prev, ...Object.fromEntries(loaded.map((b) => [b.id, b])) }));
        setSubs(list);
      })
      .catch((e) => setError(String(e)));
  }, [partnerId]);

  const selectedBundle = bundleId ? bundles[bundleId] : undefined;
  const includesInsurance = useMemo(() => {
    if (!selectedBundle) return false;
    const insurance = new Set(offerings.filter((o) => o.offeringType === "INSURANCE").map((o) => o.id));
    return selectedBundle.offeringIds.some((id) => insurance.has(id));
  }, [selectedBundle, offerings]);

  async function recordTransaction(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedBundle || !partnerId) return;
    setError(null);
    setBusy(true);
    try {
      const tx = await api.transactions.record({
        customerName: customerName.trim(),
        customerPhone: customerPhone.trim(),
        customerEmail: customerEmail.trim(),
        partnerId,
        bundleId: selectedBundle.id,
        ecoSystemId: selectedBundle.ecoSystemId,
        amount: Number(amount),
        ...(includesInsurance
          ? { premium: Number(premium), sumInsured: Number(sumInsured), policyNumber: policyNumber.trim() }
          : {}),
      });
      setRecorded((prev) => [tx, ...prev]);
      setCustomerName("");
      setCustomerPhone("");
      setCustomerEmail("");
      setAmount("");
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
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  const partnerName = (id: string) => partners.find((p) => p.id === id)?.name ?? shortId(id);
  const bundleLabel = (id: string) => (bundles[id] ? `${bundles[id].name} (v${bundles[id].version})` : shortId(id));

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
            Bundle (subscribed as a whole)
            <select value={bundleId} onChange={(e) => setBundleId(e.target.value)} required disabled={!partnerId}>
              <option value="">{partnerId ? (subs.length ? "Select…" : "No subscriptions") : "Pick a partner first"}</option>
              {subs.map((s) =>
                bundles[s.bundleId] ? (
                  <option key={s.id} value={s.bundleId}>
                    {bundleLabel(s.bundleId)}
                    {s.status === "PENDING_RECONSENT" ? " · pending re-consent" : ""}
                  </option>
                ) : null,
              )}
            </select>
          </label>
          <label>
            Amount (MYR)
            <input value={amount} onChange={(e) => setAmount(e.target.value)} type="number" step="0.01" min="0.01" required />
          </label>
          {includesInsurance && (
            <>
              <label>
                Premium (MYR)
                <input value={premium} onChange={(e) => setPremium(e.target.value)} type="number" step="0.01" min="0" required />
              </label>
              <label>
                Sum insured (MYR)
                <input value={sumInsured} onChange={(e) => setSumInsured(e.target.value)} type="number" step="0.01" min="0" required />
              </label>
              <label>
                Policy number
                <input value={policyNumber} onChange={(e) => setPolicyNumber(e.target.value)} required />
              </label>
            </>
          )}
          <button className="primary" disabled={busy || !bundleId}>
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
          <p style={{ fontSize: "0.85rem", marginTop: 8 }}>
            Report <span className="mono">{shortId(report.id)}</span> <StatusBadge status={report.status} /> generated
            for {report.periodStart} – {report.periodEnd}.
          </p>
        )}

        <ErrorBanner message={error} />
      </div>

      <div className="card">
        <h2>Customers registered this session</h2>
        {recorded.length === 0 && <p className="empty">Nothing recorded yet.</p>}
        {recorded.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Customer</th>
                <th>Partner · bundle</th>
                <th>Amount</th>
                <th>Recorded at</th>
              </tr>
            </thead>
            <tbody>
              {recorded.map((t) => (
                <tr key={t.id}>
                  <td>
                    {t.customerName}
                    <div className="mono">{t.customerEmail}</div>
                    <div className="mono">{t.customerPhone}</div>
                  </td>
                  <td>
                    {partnerName(t.partnerId)}
                    <div className="mono">{bundleLabel(t.bundleId)}</div>
                  </td>
                  <td>{t.amount}</td>
                  <td>{new Date(t.transactionTimestamp).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
