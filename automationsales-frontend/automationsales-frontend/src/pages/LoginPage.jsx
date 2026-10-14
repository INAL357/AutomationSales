// =====================================================================
// Login page — POST /api/auth/login
// =====================================================================
import { useState } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { apiPost, saveLogin, getLogin } from '../api';

export default function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState(
    searchParams.get('expired') ? 'Your session has ended. Please sign in again.' : '',
  );
  const [loading, setLoading] = useState(false);

  // Already logged in? Go to the dashboard.
  if (getLogin()) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      // Response: { accessToken, tokenType, expiresIn, user }
      const response = await apiPost('/auth/login', { email: email.trim(), password: password });
      saveLogin(response);
      navigate('/');
    } catch (error) {
      // "Invalid email or password" or "Account is disabled"
      setMessage(error.message);
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <section className="login-brand">
        <div className="brand-logo"><span className="logo-mark">A</span> AutomationSales</div>
        <div>
          <h1>Never miss a renewal again.</h1>
          <p>Leads, customers, licences and renewals in one place. Reminders and follow-up tasks are created for you automatically.</p>
        </div>
        <ul className="check-list">
          <li>Website leads assigned to your team instantly</li>
          <li>Licence expiry tracked to the day</li>
          <li>Renewal reminders at 90, 60, 30, 14 and 7 days</li>
        </ul>
      </section>

      <main className="login-form-side">
        <form className="login-card" onSubmit={handleSubmit}>
          <h2>Welcome back</h2>
          <p className="muted">Sign in with the email and password your admin gave you.</p>

          {message && <div className="form-error">{message}</div>}

          <label htmlFor="email">Email</label>
          <input id="email" type="email" required autoComplete="username" placeholder="you@company.com"
            value={email} onChange={(event) => setEmail(event.target.value)} />

          <label htmlFor="password">Password</label>
          <input id="password" type="password" required autoComplete="current-password"
            value={password} onChange={(event) => setPassword(event.target.value)} />

          <button type="submit" className="button button-primary button-full" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>

          <p className="muted small center">Forgot your password? Ask your administrator.</p>
        </form>
      </main>
    </div>
  );
}
