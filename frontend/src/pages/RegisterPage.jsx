import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'USER',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await register(form);
      setSuccess('Registration successful. Please login.');
      setTimeout(() => navigate('/login'), 800);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card fade-in">
        <aside className="auth-side">
          <h1>Create Your Account</h1>
          <p>Join as a customer or provider and access a clean, role-based professional workspace.</p>
        </aside>

        <section className="auth-main">
          <h2>Register</h2>
          <p className="muted-text">Set up your account in less than a minute</p>

          <form onSubmit={handleSubmit} className="form" style={{ marginTop: 12 }}>
            <div className="field">
              <label>Name</label>
              <input name="name" value={form.name} onChange={handleChange} required />
            </div>
            <div className="field">
              <label>Email</label>
              <input name="email" type="email" value={form.email} onChange={handleChange} required />
            </div>
            <div className="field">
              <label>Password</label>
              <div className="password-field">
                <input
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  value={form.password}
                  onChange={handleChange}
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((prev) => !prev)}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>
            <div className="field">
              <label>Role</label>
              <select name="role" value={form.role} onChange={handleChange}>
                <option value="USER">USER</option>
                <option value="PROVIDER">PROVIDER</option>
              </select>
            </div>
            <button type="submit">Register</button>
          </form>

          {error && <p className="error">{error}</p>}
          {success && <p className="success">{success}</p>}
          <p className="auth-switch">Already have an account? <Link to="/login">Login</Link></p>
        </section>
      </div>
    </div>
  );
}
