import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate(location.state?.from?.pathname || '/', { replace: true });
    } catch (err) {
      setError(err.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-screen">
      <form className="card login-card" onSubmit={handleSubmit}>
        <div className="brand brand-center">
          <span className="brand-mark">OT</span>
          <div>
            <strong>OTMS</strong>
            <small>Operation Theatre Management System</small>
          </div>
        </div>

        <label>
          Email
          <input
            type="email"
            required
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@hospital.org"
          />
        </label>

        <label>
          Password
          <input
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="••••••••"
          />
        </label>

        {error ? (
          <p className="form-error" role="alert">
            {error}
          </p>
        ) : null}

        <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
          {loading ? 'Signing in…' : 'Sign in'}
        </button>

        <p className="login-hint">Hospital staff only. Ask your administrator for an account.</p>
      </form>
    </div>
  );
}
