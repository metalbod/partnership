import { useEffect, useState } from "react";
import { api } from "../api";
import type { Bundle, EcoSystem, Offering, Partner, ProfitShareReport, Transaction, Vendor } from "../types";
import { ErrorBanner, StatusBadge, shortId } from "../components";

export function TransactionsPage() {
  const [ecoSystems, setEcoSystems] = useState<EcoSystem[]>([]);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [partners, setPartners] = useState<Partner[]>([]);
  const [bundles, setBundles] = useState<Bundle[]>([]);
  const [offerings, setOfferings] = useState<Offering[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [ecoId, setEcoId] = useState("");
  const [bundleId, setBundleId] = useState("");
  const [vendorId, setVendorId] = useState("");
  const [offeringId, setOfferingId] = useState("");
  const [partnerId, setPartnerId] = useState("");
  const [amount, setAmount] = useState("100.00");

  // No list endpoint exists on transaction-profitshare-service yet – this is a
  // client-side record of what's been submitted this session, not a persisted view.
  const [recorded, setRecorded] = useState<Transaction[]>([]);

  const [periodStart, setPeriodStart] = useState("2026-01-01");
  const [periodEnd, setPeriodEnd] = useState("2026-12-31");
  const [report, setReport] = useState<ProfitShareReport | null>(null);

  useEffect(() => {
    api.ecoSystems.list().then(setEcoSystems).catch((e) => setError(String(e)));
    api.vendors.list().then(setVendors).catch((e) => setError(String(e)));
    api.partners.list().then(setPartners).catch((e) => setError(String(e)));
  }, []);

  useEffect(() => {
    if (!ecoId) {
      setBundles([]);
      return;
    }
    api.bundles
      .listByEcoSystem(ecoId)
      .then((all) => setBundles(all.filter((b) => b.status === "PUBLISHED")))
      .catch((e) => setError(String(e)));
  }, [ecoId]);

  useEffect(() => {
    if (!vendorId) {
      setOfferings([]);
      return;
    }
    api.offerings.listByVendor(vendorId).then(setOfferings).catch((e) => setError(String(e)));
  }, [vendorId]);

  async function recordTransaction(e: React.FormEvent) {
    e.preventDefault();
    if (!bundleId || !offeringId || !partnerId || !vendorId || !ecoId) return;
    setError(null);
    setBusy(true);
    try {
      const tx = await api.transactions.record({
        consumerEnrolmentId: crypto.randomUUID(),
        offeringId,
        bundleId,
        ecoSystemId: ecoId,
        partnerId,
        vendorId,
        amount: Number(amount),
        isInsuranceOffering: false,
      });
      setRecorded((prev) => [tx, ...prev]);
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

  return (
    <div className="panel">
      <div className="card">
        <h2>Record a transaction</h2>
        <form onSubmit={recordTransaction}>
          <label>
            Eco-system
            <select value={ecoId} onChange={(e) => setEcoId(e.target.value)}>
              <option value="">Select…</option>
              {ecoSystems.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Published bundle
            <select value={bundleId} onChange={(e) => setBundleId(e.target.value)}>
              <option value="">Select…</option>
              {bundles.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name} (v{b.version})
                </option>
              ))}
            </select>
          </label>
          <label>
            Vendor
            <select value={vendorId} onChange={(e) => setVendorId(e.target.value)}>
              <option value="">Select…</option>
              {vendors.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Offering
            <select value={offeringId} onChange={(e) => setOfferingId(e.target.value)}>
              <option value="">Select…</option>
              {offerings.map((o) => (
                <option key={o.id} value={o.id}>
                  {o.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Partner
            <select value={partnerId} onChange={(e) => setPartnerId(e.target.value)}>
              <option value="">Select…</option>
              {partners.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Amount (MYR)
            <input value={amount} onChange={(e) => setAmount(e.target.value)} type="number" step="0.01" />
          </label>
          <button className="primary" disabled={busy || !bundleId || !offeringId || !partnerId}>
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
        <h2>Transactions recorded this session</h2>
        {recorded.length === 0 && <p className="empty">Nothing recorded yet.</p>}
        {recorded.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Amount</th>
                <th>Bundle</th>
                <th>Partner</th>
                <th>Vendor</th>
                <th>Recorded at</th>
              </tr>
            </thead>
            <tbody>
              {recorded.map((t) => (
                <tr key={t.id}>
                  <td>{t.amount}</td>
                  <td className="mono">{shortId(t.bundleId)}</td>
                  <td className="mono">{shortId(t.partnerId)}</td>
                  <td className="mono">{shortId(t.vendorId)}</td>
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
