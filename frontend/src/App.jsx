import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [results, setResults] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleDelete() {
    setError(null);
    setResults(null);
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    if (paths.length === 0) {
      setError('Enter at least one path');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paths })
      });
      const data = await res.json();
      if (!res.ok) {
        setError(data.error || 'Request failed');
      } else {
        setResults(data);
      }
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="app">
      <h1>Safe File Deleter</h1>
      <p className="hint">One relative path per line. Symlinks, non-regular files, and paths outside allowed_root are refused.</p>
      <textarea
        value={input}
        onChange={e => setInput(e.target.value)}
        placeholder="docs/old.txt\ntmp/cache.bin"
        rows={8}
      />
      <button onClick={handleDelete} disabled={loading}>
        {loading ? 'Deleting...' : 'Delete'}
      </button>
      {error && <div className="error">{error}</div>}
      {results && (
        <div className="results">
          <div className="root">allowed_root: <code>{results.allowedRoot}</code></div>
          <ul>
            {results.results.map((r, i) => (
              <li key={i} className={r.status}>
                <code>{r.path}</code> — <strong>{r.status}</strong>
                {r.reason && <span> ({r.reason})</span>}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
