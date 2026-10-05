import { useEffect, useState } from "react";
import { api } from "../api";
import type { Bundle, EcoSystem, Offering, Partner, PriceKind, Subscription } from "../types";
import { ErrorBanner, StatusBadge, shortId } from "../components";
import { breakdown, myr, priceLabel } from "../pricing";

export function PartnersPage() {
  const [partners, setPartners] = useState<Partner[]>([]);
  const [selected, setSelected] = useState<Partner | null>(null);
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [partnerName, setPartnerName] = useState("");
  const [onboardedBy, setOnboardedBy] = useState<"ADMIN" | "SALES">("ADMIN");

  const [ecoSystems, setEcoSystems] = useState<EcoSystem[]>([]);
  const [subEcoId, setSubEcoId] = useState("");
  const [subBundles, setSubBundles] = useState<Bundle[]>([]);
  const [subBundleId, setSubBundleId] = useState("");
  const [price, setPrice] = useState("");
  const [shareType, setShareType] = useState<PriceKind>("PERCENTAGE");
  const [shareValue, setShareValue] = useState("");
  const [offerings, setOfferings] = useState<Offering[]>([]);

  const loadPartners = () => api.partners.list().then(setPartners).catch((e) => setError(String(e)));
  const loadSubscriptions = (partnerId: string) =>
    api.subscriptions.listByPartner(partnerId).then(setSubscriptions).catch((e) => setError(String(e)));

  useEffect(() => {
    loadPartners();
    api.ecoSystems.list().then(setEcoSystems).catch((e) => setError(String(e)));
    // Unit prices of every offering, to preview how a bundle's cost would be split.
    api.vendors
      .list()
      .then((vs) => Promise.all(vs.map((v) => api.offerings.listByVendor(v.id))))
      .then((lists) => setOfferings(lists.flat()))
      .catch((e) => setError(String(e)));
  }, []);

  useEffect(() => {
    if (!selected) {
      setSubscriptions([]);
      return;
    }
    loadSubscriptions(selected.id);
  }, [selected]);

  useEffect(() => {
    if (!subEcoId) {
      setSubBundles([]);
      return;
    }
    api.bundles
      .listByEcoSystem(subEcoId)
      .then((all) => setSubBundles(all.filter((b) => b.status === "PUBLISHED")))
      .catch((e) => setError(String(e)));
  }, [subEcoId]);

  const chosenBundle = subBundles.find((b) => b.id === subBundleId);
  const preview =
    chosenBundle && Number(price) > 0 && shareValue !== ""
      ? breakdown(
          Number(price),
          shareType,
          Number(shareValue),
          chosenBundle.offeringIds.map((id) => offerings.find((o) => o.id === id)).filter((o): o is Offering => !!o),
        )
      : null;

  async function createPartner(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.partners.create({ name: partnerName, onboardedBy });
      setPartnerName("");
      await loadPartners();
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function subscribe(e: React.FormEvent) {
    e.preventDefault();
    if (!selected || !subBundleId) return;
    const bundle = subBundles.find((b) => b.id === subBundleId);
    if (!bundle) return;
    setError(null);
    setBusy(true);
    try {
      await api.subscriptions.subscribe({
        partnerId: selected.id,
        bundleId: bundle.id,
        bundleVersion: bundle.version,
        subscriptionPrice: Number(price),
        partnerShareType: shareType,
        partnerShareValue: Number(shareValue),
      });
      setPrice("");
      setShareValue("");
      setSubBundleId("");
      await loadSubscriptions(selected.id);
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function reconsent(sub: Subscription) {
    if (!sub.pendingBundleId || !selected) return;
    setError(null);
    try {
      const pendingBundle = await api.bundles.get(sub.pendingBundleId);
      await api.subscriptions.reconsent(sub.id, pendingBundle.version);
      await loadSubscriptions(selected.id);
    } catch (e) {
      setError(String(e));
    }
  }

  return (
    <div className="panel">
      <div className="card">
        <h2>New partner</h2>
        <form onSubmit={createPartner}>
          <label>
            Name
            <input value={partnerName} onChange={(e) => setPartnerName(e.target.value)} required />
          </label>
          <label>
            Onboarded by
            <select value={onboardedBy} onChange={(e) => setOnboardedBy(e.target.value as typeof onboardedBy)}>
              <option value="ADMIN">Admin</option>
              <option value="SALES">Sales</option>
            </select>
          </label>
          <button className="primary" disabled={busy}>
            Create partner
          </button>
        </form>

        {selected && (
          <>
            <h2 style={{ marginTop: 24 }}>Subscribe {selected.name} to a bundle</h2>
            <form onSubmit={subscribe}>
              <label>
                Eco-system
                <select value={subEcoId} onChange={(e) => setSubEcoId(e.target.value)}>
                  <option value="">Select eco-system…</option>
                  {ecoSystems.map((e) => (
                    <option key={e.id} value={e.id}>
                      {e.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Published bundle
                <select value={subBundleId} onChange={(e) => setSubBundleId(e.target.value)}>
                  <option value="">Select bundle…</option>
                  {subBundles.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.name} (v{b.version})
                    </option>
                  ))}
                </select>
              </label>
              {subBundleId && (
                <>
                  <label>
                    Cost of the bundle for this partner (MYR)
                    <input value={price} onChange={(e) => setPrice(e.target.value)} type="number" step="0.01" min="0.01" required />
                  </label>
                  <label>
                    Partner's share is…
                    <select value={shareType} onChange={(e) => setShareType(e.target.value as PriceKind)}>
                      <option value="PERCENTAGE">A percentage of the cost</option>
                      <option value="FIXED">A fixed amount (MYR)</option>
                    </select>
                  </label>
                  <label>
                    {shareType === "FIXED" ? "Partner's share (MYR)" : "Partner's share (%)"}
                    <input
                      value={shareValue}
                      onChange={(e) => setShareValue(e.target.value)}
                      type="number"
                      step="0.01"
                      min="0"
                      max={shareType === "PERCENTAGE" ? 100 : undefined}
                      required
                    />
                  </label>
                  {preview && (
                    <div className="mono" style={{ fontSize: "0.8rem" }}>
                      {preview.lines.map((l) => (
                        <div key={l.offering.id}>
                          {l.offering.name}: {myr(l.amount)}
                        </div>
                      ))}
                      <div>Vendors: {myr(preview.vendorTotal)}</div>
                      <div>Partner: {myr(preview.partner)}</div>
                      <div style={{ color: preview.company < 0 ? "var(--danger)" : undefined }}>Company keeps: {myr(preview.company)}</div>
                    </div>
                  )}
                  {preview && preview.company < 0 && (
                    <p className="error">The vendors' and partner's shares exceed the bundle cost.</p>
                  )}
                </>
              )}
              <button className="primary" disabled={busy || !subBundleId || (preview ? preview.company < 0 : false)}>
                Subscribe (whole bundle)
              </button>
            </form>
          </>
        )}

        <ErrorBanner message={error} />
      </div>

      <div className="card">
        <h2>Partners</h2>
        {partners.length === 0 && <p className="empty">No partners yet.</p>}
        {partners.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Onboarded by</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {partners.map((p) => (
                <tr
                  key={p.id}
                  onClick={() => setSelected(p)}
                  style={{ cursor: "pointer", background: selected?.id === p.id ? "var(--bg-subtle)" : undefined }}
                >
                  <td>{p.name}</td>
                  <td>{p.onboardedBy}</td>
                  <td>
                    <StatusBadge status={p.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {selected && (
          <>
            <h2 style={{ marginTop: 20 }}>Subscriptions – {selected.name}</h2>
            {subscriptions.length === 0 && <p className="empty">No subscriptions yet.</p>}
            {subscriptions.length > 0 && (
              <table>
                <thead>
                  <tr>
                    <th>Bundle</th>
                    <th>Version</th>
                    <th>Cost · partner share</th>
                    <th>Status</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {subscriptions.map((s) => (
                    <tr key={s.id}>
                      <td className="mono">{shortId(s.bundleId)}</td>
                      <td>{s.bundleVersionAtSubscription}</td>
                      <td>
                        {myr(s.subscriptionPrice)}
                        <div className="mono">{priceLabel(s.partnerShareType, s.partnerShareValue)}</div>
                      </td>
                      <td>
                        <StatusBadge status={s.status} />
                      </td>
                      <td>
                        {s.status === "PENDING_RECONSENT" && (
                          <button className="secondary" onClick={() => reconsent(s)}>
                            Re-consent to {shortId(s.pendingBundleId!)}
                          </button>
                        )}
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
