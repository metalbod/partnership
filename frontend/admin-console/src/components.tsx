export function StatusBadge({ status }: { status: string }) {
  return <span className={`badge status-${status}`}>{status}</span>;
}

export function ErrorBanner({ message }: { message: string | null }) {
  if (!message) return null;
  return <p className="error">{message}</p>;
}

export function shortId(id: string) {
  return id.slice(0, 8);
}
