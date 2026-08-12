import { useEffect, useState } from "react";
import { api } from "../api";
import type { Bundle, EcoSystem, Offering, Vendor } from "../types";
import { ErrorBanner, StatusBadge, shortId } from "../components";

export function EcoSystemsPage() {
  const [ecoSystems, setEcoSystems] = useState<EcoSystem[]>([]);
  const [selected, setSelected] = useState<EcoSystem | null>(null);
  const [bundles, setBundles] = useState<Bundle[]>([]);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [ecoName, setEcoName] = useState("");
  const [assignVendorId, setAssignVendorId] = useState("");
  const [vendorOfferings, setVendorOfferings] = useState<Offering[]>([]);
  const [assignOfferingId, setAssignOfferingId] = useState("");

  const [bundleName, setBundleName] = useState("");
  const [bundleOfferingIds, setBundleOfferingIds] = useState<string[]>([]);

  const [versioningBundle, setVersioningBundle] = useState<Bundle | null>(null);
  const [versionName, setVersionName] = useState("");
  const [versionOfferingIds, setVersionOfferingIds] = useState<string[]>([]);

  const loadEcoSystems = () => api.ecoSystems.list().then(setEcoSystems).catch((e) => setError(String(e)));
  const loadBundles = (ecoSystemId: string) =>
    api.bundles.listByEcoSystem(ecoSystemId).then(setBundles).catch((e) => setError(String(e)));

  useEffect(() => {
    loadEcoSystems();
    api.vendors.list().then(setVendors).catch((e) => setError(String(e)));
  }, []);

  useEffect(() => {
    if (!selected) {
      setBundles([]);
      return;
    }
    loadBundles(selected.id);
  }, [selected]);

  useEffect(() => {
    if (!assignVendorId) {
      setVendorOfferings([]);
      return;
    }
    api.offerings.listByVendor(assignVendorId).then(setVendorOfferings).catch((e) => setError(String(e)));
  }, [assignVendorId]);

  async function createEcoSystem(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.ecoSystems.create({ name: ecoName });
      setEcoName("");
      await loadEcoSystems();
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function assignOffering(e: React.FormEvent) {
    e.preventDefault();
    if (!selected || !assignOfferingId) return;
    setError(null);
    setBusy(true);
    try {
      const updated = await api.ecoSystems.assignOffering(selected.id, assignOfferingId);
      setSelected(updated);
      setEcoSystems((prev) => prev.map((e) => (e.id === updated.id ? updated : e)));
      setAssignOfferingId("");
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function createBundle(e: React.FormEvent) {
    e.preventDefault();
    if (!selected || bundleOfferingIds.length === 0) return;
    setError(null);
    setBusy(true);
    try {
      await api.bundles.create({ ecoSystemId: selected.id, name: bundleName, offeringIds: bundleOfferingIds });
      setBundleName("");
      setBundleOfferingIds([]);
      await loadBundles(selected.id);
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  async function publish(bundle: Bundle) {
    setError(null);
    try {
      await api.bundles.publish(bundle.id);
      if (selected) await loadBundles(selected.id);
    } catch (e) {
      setError(String(e));
    }
  }

  function startNewVersion(bundle: Bundle) {
    setVersioningBundle(bundle);
    setVersionName(bundle.name);
    setVersionOfferingIds(bundle.offeringIds);
  }

  async function submitNewVersion(e: React.FormEvent) {
    e.preventDefault();
    if (!selected || !versioningBundle || versionOfferingIds.length === 0) return;
    setError(null);
    setBusy(true);
    try {
      await api.bundles.newVersion(versioningBundle.id, {
        ecoSystemId: selected.id,
        name: versionName,
        offeringIds: versionOfferingIds,
      });
      setVersioningBundle(null);
      await loadBundles(selected.id);
    } catch (e) {
      setError(String(e));
    } finally {
      setBusy(false);
    }
  }

  function toggle(list: string[], setList: (v: string[]) => void, id: string) {
    setList(list.includes(id) ? list.filter((x) => x !== id) : [...list, id]);
  }

  return (
    <div className="panel">
      <div className="card">
        <h2>New eco-system</h2>
        <form onSubmit={createEcoSystem}>
          <label>
            Name
            <input value={ecoName} onChange={(e) => setEcoName(e.target.value)} required />
          </label>
          <button className="primary" disabled={busy}>
            Create eco-system
          </button>
        </form>

        {selected && (
          <>
            <h2 style={{ marginTop: 24 }}>Assign offering to {selected.name}</h2>
            <form onSubmit={assignOffering}>
              <label>
                Vendor
                <select value={assignVendorId} onChange={(e) => setAssignVendorId(e.target.value)}>
                  <option value="">Select vendor…</option>
                  {vendors.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Offering
                <select value={assignOfferingId} onChange={(e) => setAssignOfferingId(e.target.value)}>
                  <option value="">Select offering…</option>
                  {vendorOfferings.map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.name}
                    </option>
                  ))}
                </select>
              </label>
              <button className="primary" disabled={busy || !assignOfferingId}>
                Assign
              </button>
            </form>

            <h2 style={{ marginTop: 24 }}>New bundle (mix of assigned offerings)</h2>
            {selected.assignedOfferingIds.length === 0 && (
              <p className="empty">Assign at least one offering first.</p>
            )}
            {selected.assignedOfferingIds.length > 0 && (
              <form onSubmit={createBundle}>
                <label>
                  Name
                  <input value={bundleName} onChange={(e) => setBundleName(e.target.value)} required />
                </label>
                <label>Offerings</label>
                <div className="checkbox-list">
                  {selected.assignedOfferingIds.map((id) => (
                    <label key={id}>
                      <input
                        type="checkbox"
                        checked={bundleOfferingIds.includes(id)}
                        onChange={() => toggle(bundleOfferingIds, setBundleOfferingIds, id)}
                      />
                      {shortId(id)}
                    </label>
                  ))}
                </div>
                <button className="primary" disabled={busy || bundleOfferingIds.length === 0}>
                  Create bundle (DRAFT)
                </button>
              </form>
            )}
          </>
        )}

        <ErrorBanner message={error} />
      </div>

      <div className="card">
        <h2>Eco-systems</h2>
        {ecoSystems.length === 0 && <p className="empty">No eco-systems yet.</p>}
        {ecoSystems.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Assigned offerings</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {ecoSystems.map((e) => (
                <tr
                  key={e.id}
                  onClick={() => setSelected(e)}
                  style={{ cursor: "pointer", background: selected?.id === e.id ? "var(--bg-subtle)" : undefined }}
                >
                  <td>{e.name}</td>
                  <td>{e.assignedOfferingIds.length}</td>
                  <td>
                    <StatusBadge status={e.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {selected && (
          <>
            <h2 style={{ marginTop: 20 }}>Bundles – {selected.name}</h2>
            {bundles.length === 0 && <p className="empty">No bundles yet.</p>}
            {bundles.length > 0 && (
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>v</th>
                    <th>Status</th>
                    <th>Offerings</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {bundles.map((b) => (
                    <tr key={b.id}>
                      <td>{b.name}</td>
                      <td>{b.version}</td>
                      <td>
                        <StatusBadge status={b.status} />
                      </td>
                      <td className="mono">{b.offeringIds.map(shortId).join(", ")}</td>
                      <td>
                        {b.status === "DRAFT" && (
                          <button className="secondary" onClick={() => publish(b)}>
                            Publish
                          </button>
                        )}
                        {b.status === "PUBLISHED" && (
                          <button className="secondary" onClick={() => startNewVersion(b)}>
                            New version
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {versioningBundle && (
              <>
                <h2 style={{ marginTop: 20 }}>
                  New version of "{versioningBundle.name}" (supersedes v{versioningBundle.version})
                </h2>
                <form onSubmit={submitNewVersion}>
                  <label>
                    Name
                    <input value={versionName} onChange={(e) => setVersionName(e.target.value)} required />
                  </label>
                  <label>Offerings</label>
                  <div className="checkbox-list">
                    {selected.assignedOfferingIds.map((id) => (
                      <label key={id}>
                        <input
                          type="checkbox"
                          checked={versionOfferingIds.includes(id)}
                          onChange={() => toggle(versionOfferingIds, setVersionOfferingIds, id)}
                        />
                        {shortId(id)}
                      </label>
                    ))}
                  </div>
                  <div style={{ display: "flex", gap: 8 }}>
                    <button className="primary" disabled={busy || versionOfferingIds.length === 0}>
                      Create new version
                    </button>
                    <button type="button" className="secondary" onClick={() => setVersioningBundle(null)}>
                      Cancel
                    </button>
                  </div>
                </form>
              </>
            )}
          </>
        )}
      </div>
    </div>
  );
}
