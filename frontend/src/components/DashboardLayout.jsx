import { useMemo, useState } from 'react';

function iconFor(key) {
  switch (key) {
    case 'dashboard':
      return '📊';
    case 'services':
      return '🛠️';
    case 'bookings':
      return '📅';
    case 'earnings':
      return '💰';
    case 'reviews':
      return '⭐';
    case 'profile':
      return '👤';
    default:
      return '•';
  }
}

export default function DashboardLayout({ title, welcomeName, sections, onLogout, children }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  const menuItems = useMemo(() => [
    ...sections,
    { key: 'logout', label: 'Logout', action: onLogout },
  ], [sections, onLogout]);

  const scrollToSection = (sectionId) => {
    const target = document.getElementById(sectionId);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    setSidebarOpen(false);
  };

  return (
    <div className="dashboard-shell">
      <aside className={`dashboard-sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-brand">
          <div className="brand-dot" />
          <span>Local Service Pro</span>
        </div>
        <nav className="sidebar-nav">
          {menuItems.map((item) => (
            <button
              key={item.key}
              type="button"
              className={`sidebar-link ${item.key === 'logout' ? 'danger' : ''}`}
              onClick={() => {
                if (item.action) {
                  item.action();
                  return;
                }
                scrollToSection(item.key);
              }}
            >
              <span>{iconFor(item.key)}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
      </aside>

      {sidebarOpen && <button type="button" className="sidebar-backdrop" onClick={() => setSidebarOpen(false)} aria-label="Close menu" />}

      <div className="dashboard-main-area">
        <header className="dashboard-topbar">
          <div className="topbar-left">
            <button type="button" className="menu-btn" onClick={() => setSidebarOpen((prev) => !prev)}>
              ☰
            </button>
            <div>
              <h1>{title}</h1>
              <p className="muted-text">Welcome, {welcomeName || 'User'}</p>
            </div>
          </div>

          <div className="profile-menu-wrap">
            <button type="button" className="profile-btn" onClick={() => setProfileOpen((prev) => !prev)}>
              <span className="profile-avatar">👤</span>
              <span className="profile-name">Profile</span>
              <span>▾</span>
            </button>
            {profileOpen && (
              <div className="profile-dropdown">
                <button type="button" onClick={() => scrollToSection('profile')}>View Profile</button>
                <button type="button" className="danger" onClick={onLogout}>Logout</button>
              </div>
            )}
          </div>
        </header>

        <main className="dashboard-content fade-in">{children}</main>
      </div>
    </div>
  );
}
