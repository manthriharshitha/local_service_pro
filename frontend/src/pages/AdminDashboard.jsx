import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import DashboardLayout from '../components/DashboardLayout';
import LoadingSpinner from '../components/LoadingSpinner';

const bookingStatusOptions = ['PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'];
const userRoles = ['ALL', 'USER', 'PROVIDER', 'ADMIN'];

function statusClass(status) {
  switch (status) {
    case 'PENDING':
      return 'status status-pending';
    case 'ACCEPTED':
      return 'status status-accepted';
    case 'COMPLETED':
      return 'status status-completed';
    case 'CANCELLED':
      return 'status status-cancelled';
    case 'REJECTED':
      return 'status status-rejected';
    default:
      return 'status';
  }
}

function displayLabel(value) {
  return value ? value.replaceAll('_', ' ') : '-';
}

export default function AdminDashboard() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [overview, setOverview] = useState({
    totalUsers: 0,
    totalProviders: 0,
    totalServices: 0,
    totalBookings: 0,
    pendingBookings: 0,
    completedBookings: 0,
    totalRevenue: 0,
  });

  const [users, setUsers] = useState([]);
  const [services, setServices] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [analytics, setAnalytics] = useState({
    totalRevenue: 0,
    monthlyRevenue: 0,
    revenuePerProvider: {},
    mostBookedService: 'N/A',
    leastBookedService: 'N/A',
  });
  const [monitoring, setMonitoring] = useState({
    recentUsers: [],
    recentServices: [],
    recentBookings: [],
  });

  const [userFilters, setUserFilters] = useState({ search: '', role: 'ALL', sort: 'desc' });
  const [serviceFilters, setServiceFilters] = useState({ category: '', provider: '', sort: 'none' });
  const [bookingFilters, setBookingFilters] = useState({ status: '', provider: '', fromDate: '', toDate: '' });

  const [selectedBooking, setSelectedBooking] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const [overviewRes, usersRes, servicesRes, bookingsRes, analyticsRes, monitoringRes] = await Promise.all([
        axiosClient.get('/admin/overview'),
        axiosClient.get('/admin/users', {
          params: {
            search: userFilters.search,
            role: userFilters.role === 'ALL' ? '' : userFilters.role,
            sort: userFilters.sort,
          },
        }),
        axiosClient.get('/admin/services', { params: serviceFilters }),
        axiosClient.get('/admin/bookings', { params: bookingFilters }),
        axiosClient.get('/admin/analytics'),
        axiosClient.get('/admin/monitoring'),
      ]);

      setOverview(overviewRes.data);
      setUsers(usersRes.data);
      setServices(servicesRes.data);
      setBookings(bookingsRes.data);
      setAnalytics(analyticsRes.data);
      setMonitoring(monitoringRes.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [userFilters, serviceFilters, bookingFilters]);

  const deleteUser = async (id) => {
    setMessage('');
    setError('');
    try {
      await axiosClient.delete(`/admin/users/${id}`);
      setMessage('User deleted successfully.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to delete user.');
    }
  };

  const updateUserStatus = async (id, status) => {
    setMessage('');
    setError('');
    try {
      await axiosClient.put(`/admin/users/${id}/status`, { status });
      setMessage('User status updated.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to update user status.');
    }
  };

  const updateUserRole = async (id, role) => {
    setMessage('');
    setError('');
    try {
      await axiosClient.put(`/admin/users/${id}/role`, { role });
      setMessage('User role updated.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to update role.');
    }
  };

  const resetPassword = async (id) => {
    setMessage('');
    setError('');
    try {
      await axiosClient.put(`/admin/users/${id}/reset-password`, { newPassword: 'Temp@123' });
      setMessage('Password reset to Temp@123');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to reset password.');
    }
  };

  const deleteService = async (id) => {
    setMessage('');
    setError('');
    try {
      await axiosClient.delete(`/admin/services/${id}`);
      setMessage('Service deleted successfully.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to delete service.');
    }
  };

  const updateServiceStatus = async (id, status) => {
    setMessage('');
    setError('');
    try {
      await axiosClient.put(`/admin/services/${id}/status`, { status });
      setMessage('Service status updated.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to update service status.');
    }
  };

  const updateBookingStatus = async (id, status) => {
    setMessage('');
    setError('');
    try {
      await axiosClient.put(`/admin/bookings/${id}/status`, { status });
      setMessage('Booking status updated.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to update booking status.');
    }
  };

  const viewBookingDetails = async (id) => {
    const response = await axiosClient.get(`/admin/bookings/${id}`);
    setSelectedBooking(response.data);
  };

  const downloadReport = async (type, fileName) => {
    const response = await axiosClient.get(`/admin/reports/${type}`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const sections = [
    { key: 'dashboard', label: 'Dashboard' },
    { key: 'services', label: 'Services' },
    { key: 'bookings', label: 'Bookings' },
    { key: 'earnings', label: 'Earnings' },
    { key: 'reviews', label: 'Reviews' },
    { key: 'profile', label: 'Profile' },
  ];

  const revenueProviders = useMemo(() => Object.entries(analytics.revenuePerProvider || {}), [analytics]);
  const revenueMax = useMemo(() => {
    const values = revenueProviders.map(([, revenue]) => Number(revenue || 0));
    return Math.max(...values, 1);
  }, [revenueProviders]);

  return (
    <DashboardLayout title="Admin Dashboard" welcomeName="Admin" sections={sections} onLogout={handleLogout}>
      {loading ? (
        <LoadingSpinner label="Loading admin dashboard..." />
      ) : (
        <>
          {message && <p className="success">{message}</p>}
          {error && <p className="error">{error}</p>}

          <section id="dashboard" className="section-card">
            <h3>Overview</h3>
            <div className="stats-grid">
              <article className="stat-card soft-primary">
                <p className="stat-icon">👥</p>
                <p className="stat-title">Total Users</p>
                <p className="stat-value">{overview.totalUsers}</p>
              </article>
              <article className="stat-card soft-warning">
                <p className="stat-icon">🧑‍🔧</p>
                <p className="stat-title">Providers</p>
                <p className="stat-value">{overview.totalProviders}</p>
              </article>
              <article className="stat-card soft-success">
                <p className="stat-icon">📦</p>
                <p className="stat-title">Bookings</p>
                <p className="stat-value">{overview.totalBookings}</p>
              </article>
              <article className="stat-card soft-primary">
                <p className="stat-icon">💸</p>
                <p className="stat-title">Revenue</p>
                <p className="stat-value">₹{overview.totalRevenue}</p>
              </article>
            </div>
          </section>

          <section id="reviews" className="section-card">
            <h3>User Management</h3>
            <div className="filter-row" style={{ marginBottom: 12 }}>
              <input placeholder="Search name or email" value={userFilters.search} onChange={(event) => setUserFilters({ ...userFilters, search: event.target.value })} />
              <select value={userFilters.role} onChange={(event) => setUserFilters({ ...userFilters, role: event.target.value })}>
                {userRoles.map((role) => <option key={role} value={role}>{role}</option>)}
              </select>
              <select value={userFilters.sort} onChange={(event) => setUserFilters({ ...userFilters, sort: event.target.value })}>
                <option value="desc">Newest First</option>
                <option value="asc">Oldest First</option>
              </select>
            </div>

            <div className="card-grid">
              {users.map((user) => (
                <article className="list-card" key={user.id}>
                  <div className="service-head">
                    <h4>{user.name}</h4>
                    <span className={user.status === 'ACTIVE' ? 'badge badge-active' : 'badge badge-paused'}>{user.status}</span>
                  </div>
                  <p><strong>ID:</strong> {user.id}</p>
                  <p><strong>Email:</strong> {user.email}</p>
                  <p><strong>Role:</strong> {user.role}</p>
                  <p className="hint">Joined: {user.registrationDate || '-'}</p>
                  <div className="button-row" style={{ marginTop: 10 }}>
                    {user.role !== 'ADMIN' && (
                      <button type="button" className="btn-danger" onClick={() => deleteUser(user.id)}>Delete</button>
                    )}
                    <button type="button" className="btn-warning" onClick={() => updateUserStatus(user.id, user.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE')}>
                      {user.status === 'ACTIVE' ? 'Block' : 'Unblock'}
                    </button>
                    {user.role === 'USER' && <button type="button" className="btn-info" onClick={() => updateUserRole(user.id, 'PROVIDER')}>Promote</button>}
                    {user.role === 'PROVIDER' && <button type="button" className="btn-secondary" onClick={() => updateUserRole(user.id, 'USER')}>Demote</button>}
                    <button type="button" onClick={() => resetPassword(user.id)}>Reset Password</button>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section id="services" className="section-card">
            <h3>Service Management</h3>
            <div className="filter-row" style={{ marginBottom: 12 }}>
              <select value={serviceFilters.category} onChange={(event) => setServiceFilters({ ...serviceFilters, category: event.target.value })}>
                <option value="">All Categories</option>
                <option value="AC_REPAIR">AC Repair</option>
                <option value="PLUMBING">Plumbing</option>
                <option value="ELECTRICAL">Electrical</option>
                <option value="CLEANING">Cleaning</option>
                <option value="PAINTING">Painting</option>
              </select>
              <input placeholder="Filter by provider" value={serviceFilters.provider} onChange={(event) => setServiceFilters({ ...serviceFilters, provider: event.target.value })} />
              <select value={serviceFilters.sort} onChange={(event) => setServiceFilters({ ...serviceFilters, sort: event.target.value })}>
                <option value="none">Sort by Price</option>
                <option value="low">Low to High</option>
                <option value="high">High to Low</option>
              </select>
            </div>
            <div className="card-grid">
              {services.map((service) => (
                <article className="service-card" key={service.id}>
                  <div className="service-head">
                    <h4>{service.title}</h4>
                    <span className={service.status === 'ACTIVE' ? 'badge badge-active' : 'badge badge-paused'}>{service.status}</span>
                  </div>
                  <p><strong>ID:</strong> {service.id}</p>
                  <p><strong>Category:</strong> {displayLabel(service.category)}</p>
                  <p><strong>Provider:</strong> {service.providerName}</p>
                  <p><strong>Bookings:</strong> {service.totalBookings}</p>
                  <p className="price-tag">₹{service.price}</p>
                  <div className="button-row" style={{ marginTop: 10 }}>
                    <button type="button" className="btn-danger" onClick={() => deleteService(service.id)}>Delete</button>
                    {service.status === 'ACTIVE' ? (
                      <button type="button" className="btn-warning" onClick={() => updateServiceStatus(service.id, 'PAUSED')}>Disable</button>
                    ) : (
                      <button type="button" className="btn-success" onClick={() => updateServiceStatus(service.id, 'ACTIVE')}>Enable</button>
                    )}
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section id="bookings" className="section-card">
            <h3>Booking Management</h3>
            <div className="filter-row" style={{ marginBottom: 12 }}>
              <select value={bookingFilters.status} onChange={(event) => setBookingFilters({ ...bookingFilters, status: event.target.value })}>
                <option value="">All Status</option>
                {bookingStatusOptions.map((status) => <option key={status} value={status}>{status}</option>)}
              </select>
              <input placeholder="Filter by provider" value={bookingFilters.provider} onChange={(event) => setBookingFilters({ ...bookingFilters, provider: event.target.value })} />
              <input type="date" value={bookingFilters.fromDate} onChange={(event) => setBookingFilters({ ...bookingFilters, fromDate: event.target.value })} />
              <input type="date" value={bookingFilters.toDate} onChange={(event) => setBookingFilters({ ...bookingFilters, toDate: event.target.value })} />
            </div>

            <div className="card-grid">
              {bookings.map((booking) => (
                <article key={booking.id} className="booking-card">
                  <div className="booking-head">
                    <h4>Booking #{booking.id}</h4>
                    <span className={statusClass(booking.status)}>{booking.status}</span>
                  </div>
                  <p><strong>Service:</strong> {booking.serviceTitle}</p>
                  <p><strong>Customer:</strong> {booking.customerName}</p>
                  <p><strong>Provider:</strong> {booking.providerName}</p>
                  <p><strong>Date:</strong> {booking.bookingDate || '-'}</p>
                  <p><strong>Payment:</strong> {booking.paymentStatus || '-'}</p>
                  <div className="field" style={{ marginTop: 10 }}>
                    <label>Update Status</label>
                    <select value={booking.status} onChange={(event) => updateBookingStatus(booking.id, event.target.value)}>
                      {bookingStatusOptions.map((status) => <option key={status} value={status}>{status}</option>)}
                    </select>
                  </div>
                  <div className="button-row" style={{ marginTop: 10 }}>
                    <button type="button" className="btn-danger" onClick={() => updateBookingStatus(booking.id, 'CANCELLED')}>Cancel</button>
                    <button type="button" className="btn-info" onClick={() => viewBookingDetails(booking.id)}>Details</button>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section id="earnings" className="section-card">
            <h3>Revenue Analytics</h3>
            <div className="stats-grid">
              <article className="stat-card soft-success">
                <p className="stat-title">Total Revenue</p>
                <p className="stat-value">₹{analytics.totalRevenue}</p>
              </article>
              <article className="stat-card soft-primary">
                <p className="stat-title">Monthly Revenue</p>
                <p className="stat-value">₹{analytics.monthlyRevenue}</p>
              </article>
              <article className="stat-card soft-warning">
                <p className="stat-title">Most Booked Service</p>
                <p className="stat-value" style={{ fontSize: '1rem' }}>{analytics.mostBookedService}</p>
              </article>
              <article className="stat-card soft-danger">
                <p className="stat-title">Least Booked Service</p>
                <p className="stat-value" style={{ fontSize: '1rem' }}>{analytics.leastBookedService}</p>
              </article>
            </div>

            <div className="chart-wrap" style={{ marginTop: 12 }}>
              {revenueProviders.length === 0 ? (
                <p className="muted-text">No provider revenue data available.</p>
              ) : (
                revenueProviders.map(([provider, revenue]) => (
                  <div className="chart-row" key={provider}>
                    <span>{provider}</span>
                    <div className="chart-bar"><div className="chart-fill" style={{ width: `${(Number(revenue || 0) / revenueMax) * 100}%` }} /></div>
                    <strong>₹{revenue}</strong>
                  </div>
                ))
              )}
            </div>
          </section>

          <section id="profile" className="section-card">
            <h3>Monitoring & Reports</h3>
            <div className="stats-grid" style={{ marginBottom: 14 }}>
              <article className="list-card">
                <strong>Recently Registered Users</strong>
                <div className="review-list" style={{ marginTop: 8 }}>
                  {monitoring.recentUsers?.map((user) => (
                    <div key={user.id} className="hint">{user.name} ({user.role})</div>
                  ))}
                </div>
              </article>
              <article className="list-card">
                <strong>Recently Added Services</strong>
                <div className="review-list" style={{ marginTop: 8 }}>
                  {monitoring.recentServices?.map((service) => (
                    <div key={service.id} className="hint">{service.title}</div>
                  ))}
                </div>
              </article>
              <article className="list-card">
                <strong>Recent Bookings</strong>
                <div className="review-list" style={{ marginTop: 8 }}>
                  {monitoring.recentBookings?.map((booking) => (
                    <div key={booking.id} className="hint">#{booking.id} {booking.status}</div>
                  ))}
                </div>
              </article>
            </div>

            <div className="button-row">
              <button type="button" onClick={() => downloadReport('users', 'users-report.csv')}>Download User Report</button>
              <button type="button" onClick={() => downloadReport('bookings', 'bookings-report.csv')}>Download Booking Report</button>
              <button type="button" onClick={() => downloadReport('revenue', 'revenue-report.csv')}>Download Revenue Report</button>
            </div>

            <p className="hint" style={{ marginTop: 12 }}>
              Security controls are available through user blocking, role change, account deletion, and service disabling actions.
            </p>
          </section>
        </>
      )}

      {selectedBooking && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Booking Details</h3>
            <p><strong>ID:</strong> {selectedBooking.id}</p>
            <p><strong>Service:</strong> {selectedBooking.serviceTitle}</p>
            <p><strong>Customer:</strong> {selectedBooking.customerName}</p>
            <p><strong>Provider:</strong> {selectedBooking.providerName}</p>
            <p><strong>Date:</strong> {selectedBooking.bookingDate || '-'}</p>
            <p><strong>Time:</strong> {selectedBooking.bookingTime || '-'}</p>
            <p><strong>Address:</strong> {selectedBooking.address || '-'}</p>
            <p><strong>Status:</strong> {selectedBooking.status}</p>
            <p><strong>Payment:</strong> {selectedBooking.paymentStatus || '-'}</p>
            <p><strong>Cancellation Reason:</strong> {selectedBooking.cancellationReason || '-'}</p>
            <div className="button-row">
              <button type="button" className="btn-secondary" onClick={() => setSelectedBooking(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}
