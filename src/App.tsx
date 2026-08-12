import { useState } from "react";
import { VendorsPage } from "./pages/VendorsPage";
import { EcoSystemsPage } from "./pages/EcoSystemsPage";
import { PartnersPage } from "./pages/PartnersPage";
import { TransactionsPage } from "./pages/TransactionsPage";

const TABS = [
  { key: "vendors", label: "Vendors & Offerings", render: () => <VendorsPage /> },
  { key: "ecosystems", label: "Eco-Systems & Bundles", render: () => <EcoSystemsPage /> },
  { key: "partners", label: "Partners & Subscriptions", render: () => <PartnersPage /> },
  { key: "transactions", label: "Transactions & Profit-Share", render: () => <TransactionsPage /> },
] as const;

function App() {
  const [tab, setTab] = useState<(typeof TABS)[number]["key"]>("vendors");
  const active = TABS.find((t) => t.key === tab)!;

  return (
    <div className="app">
      <header className="app-header">
        <h1>Partnership Pillar – Admin Console</h1>
        <p>
          Walking skeleton hitting all four backend services directly. No auth/role-gating yet
          (Cognito isn't wired up anywhere in the platform) — this shows the Admin's full surface
          to anyone who opens it.
        </p>
      </header>

      <nav className="tabs">
        {TABS.map((t) => (
          <button key={t.key} className={t.key === tab ? "active" : ""} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
      </nav>

      {active.render()}
    </div>
  );
}

export default App;
