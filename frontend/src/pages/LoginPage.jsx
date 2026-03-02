import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const role = await login(email, password);
      if (role === 'USER') navigate('/dashboard');
      else if (role === 'PROVIDER') navigate('/provider-dashboard');
      else navigate('/admin-dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials');
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card fade-in">
        <aside className="auth-side">
          <h1>Local Service Pro</h1>
          <p>Manage bookings, services, and growth with a modern all-in-one local service platform.</p>
        </aside>

        <section className="auth-main">
          <h2>Login</h2>
          <p className="muted-text">Sign in to continue to your dashboard</p>

          <form onSubmit={handleSubmit} className="form" style={{ marginTop: 12 }}>
            <div className="field">
              <label>Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="field">
              <label>Password</label>
              <div className="password-field">
                <input type={showPassword ? 'text' : 'password'} value={password} onChange={(e) => setPassword(e.target.value)} required />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((prev) => !prev)}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>
            <button type="submit">Login</button>
          </form>

          {error && <p className="error">{error}</p>}
          <p className="auth-switch">Don&apos;t have an account? <Link to="/register">Register</Link></p>
        </section>
      </div>
    </div>
  );
}
