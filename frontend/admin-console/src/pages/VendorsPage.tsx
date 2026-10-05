import { useEffect, useState } from "react";
import { api } from "../api";
import type { Offering, Vendor } from "../types";
import { ErrorBanner, StatusBadge, shortId } from "../components";
import { priceLabel } from "../pricing";

export function VendorsPage() {
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [selected, setSelected] = useState<Vendor | null>(null);
  const [offerings, setOfferings] = useState<Offering[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [vendorName, setVendorName] = useState("");
  const [vendorEmail, setVendorEmail] = useState("");
  const [offeringName, setOfferingName] = useState("");
  const [offeringType, setOfferingType] = useState<"INSURANCE" | "NON_INSURANCE">("NON_INSURANCE");
  const [priceType, setPriceType] = useState<"FIXED" | "PERCENTAGE">("FIXED");
  const [priceValue, setPriceValue] = useState("");
  const [busy, setBusy] = useState(false);

  const loadVendors = () => api.vendors.list().then(setVendors).catch((e) => setError(String(e)));

  useEffect(() => {
    loadVendors();
  }, []);

  useEffect(() => {
    if (!selected) {
      setOfferings([]);
      return;
    }
    api.offerings.listByVendor(selected.id).then(setOfferings).catch((e) => setError(String(e)));
  }, [selected]);

  async function createVendor(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.vendors.create({ name: vendorName, contactEmail: vendorEmail || undefined });
      setVendorName("");
      setVendorEmail("");
      await loadVendors();
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function createOffering(e: React.FormEvent) {
    e.preventDefault();
    if (!selected) return;
    setError(null);
    setBusy(true);
    try {
      await api.offerings.create({ vendorId: selected.id, name: offeringName, offeringType, priceType, priceValue: Number(priceValue) });
      setOfferingName("");
      setPriceValue("");
      const refreshed = await api.offerings.listByVendor(selected.id);
      setOfferings(refreshed);
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="panel">
      <div className="card">
        <h2>New vendor</h2>
        <form onSubmit={createVendor}>
          <label>
            Name
            <input value={vendorName} onChange={(e) => setVendorName(e.target.value)} required />
          </label>
          <label>
            Contact email
            <input value={vendorEmail} onChange={(e) => setVendorEmail(e.target.value)} type="email" />
          </label>
          <button className="primary" disabled={busy}>
            Create vendor
          </button>
        </form>

        {selected && (
          <>
            <h2 style={{ marginTop: 24 }}>New offering for {selected.name}</h2>
            <form onSubmit={createOffering}>
              <label>
                Name
                <input value={offeringName} onChange={(e) => setOfferingName(e.target.value)} required />
              </label>
              <label>
                Type
                <select value={offeringType} onChange={(e) => setOfferingType(e.target.value as typeof offeringType)}>
                  <option value="NON_INSURANCE">Non-insurance</option>
                  <option value="INSURANCE">Insurance</option>
                </select>
              </label>
              <label>
                Unit price is…
                <select value={priceType} onChange={(e) => setPriceType(e.target.value as typeof priceType)}>
                  <option value="FIXED">A fixed amount (MYR)</option>
                  <option value="PERCENTAGE">A percentage of the bundle cost</option>
                </select>
              </label>
              <label>
                {priceType === "FIXED" ? "Amount (MYR)" : "Percentage of bundle cost (%)"}
                <input
                  value={priceValue}
                  onChange={(e) => setPriceValue(e.target.value)}
                  type="number"
                  step="0.01"
                  min="0"
                  max={priceType === "PERCENTAGE" ? 100 : undefined}
                  required
                />
              </label>
              <button className="primary" disabled={busy}>
                Add offering
              </button>
            </form>
          </>
        )}

        <ErrorBanner message={error} />
      </div>

      <div className="card">
        <h2>Vendors</h2>
        {vendors.length === 0 && <p className="empty">No vendors yet.</p>}
        {vendors.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Contact</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {vendors.map((v) => (
                <tr
                  key={v.id}
                  onClick={() => setSelected(v)}
                  style={{ cursor: "pointer", background: selected?.id === v.id ? "var(--bg-subtle)" : undefined }}
                >
                  <td>{v.name}</td>
                  <td>{v.contactEmail ?? "—"}</td>
                  <td>
                    <StatusBadge status={v.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {selected && (
          <>
            <h2 style={{ marginTop: 20 }}>Offerings – {selected.name}</h2>
            {offerings.length === 0 && <p className="empty">No offerings yet.</p>}
            {offerings.length > 0 && (
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Unit price</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {offerings.map((o) => (
                    <tr key={o.id}>
                      <td className="mono">{shortId(o.id)}</td>
                      <td>{o.name}</td>
                      <td>{o.offeringType}</td>
                      <td>{priceLabel(o.priceType, o.priceValue)}</td>
                      <td>
                        <StatusBadge status={o.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </>
        )}
      </div>
    </div>
  );
}
