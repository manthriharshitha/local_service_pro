import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import DashboardLayout from '../components/DashboardLayout';
import LoadingSpinner from '../components/LoadingSpinner';

const categories = [
  { value: 'AC_REPAIR', label: 'AC Repair' },
  { value: 'PLUMBING', label: 'Plumbing' },
  { value: 'ELECTRICAL', label: 'Electrical' },
  { value: 'CLEANING', label: 'Bathroom / Home Cleaning' },
  { value: 'PAINTING', label: 'Painting' },
  { value: 'HAIR_CUT', label: 'Hair Cut' },
  { value: 'MAKEUP', label: 'Makeup' },
  { value: 'NAILS', label: 'Nails' },
  { value: 'ROOM_MAKEOVERS', label: 'Room Makeovers' },
  { value: 'OTHER_SERVICES', label: 'Other Services' },
];

const bookingStatuses = ['PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'];

function displayLabel(value) {
  return value ? value.replaceAll('_', ' ') : '-';
}

function isFinalBookingStatus(status) {
  return ['COMPLETED', 'CANCELLED', 'REJECTED'].includes(status);
}

function formatDateTime(value) {
  if (!value) return '-';
  return value.replace('T', ' ').slice(0, 16);
}

function formatBookingTime(value) {
  if (!value) return '-';
  return value.length > 5 ? value.slice(0, 5) : value;
}

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

function stars(rating) {
  if (!rating) return '☆☆☆☆☆';
  return `${'★'.repeat(rating)}${'☆'.repeat(5 - rating)}`;
}

export default function ProviderDashboard() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [overview, setOverview] = useState({
    providerName: '',
    totalServices: 0,
    totalBookings: 0,
    totalCompletedBookings: 0,
    totalEarnings: 0,
    thisMonthEarnings: 0,
  });

  const [services, setServices] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [averageRating, setAverageRating] = useState(0);
  const [totalReviews, setTotalReviews] = useState(0);
  const [profile, setProfile] = useState({ name: '', email: '' });
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' });
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({
    title: '',
    description: '',
    category: 'AC_REPAIR',
    price: '',
    status: 'ACTIVE',
  });
  const [editingServiceId, setEditingServiceId] = useState(null);

  const [availability, setAvailability] = useState({
    workingDays: '',
    workingHours: '',
    emergencyServiceEnabled: false,
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadDashboard = async () => {
    setLoading(true);
    try {
      const [overviewRes, servicesRes, bookingsRes, availabilityRes, reviewsRes, profileRes] = await Promise.all([
        axiosClient.get('/provider/overview'),
        axiosClient.get('/provider/services'),
        axiosClient.get('/provider/bookings'),
        axiosClient.get('/provider/availability'),
        axiosClient.get('/provider/reviews'),
        axiosClient.get('/provider/profile'),
      ]);

      setOverview(overviewRes.data);
      setServices(servicesRes.data);
      setBookings(bookingsRes.data);
      setAvailability({
        workingDays: availabilityRes.data.workingDays || '',
        workingHours: availabilityRes.data.workingHours || '',
        emergencyServiceEnabled: availabilityRes.data.emergencyServiceEnabled || false,
      });
      setReviews(reviewsRes.data.reviews || []);
      setAverageRating(reviewsRes.data.averageRating || 0);
      setTotalReviews(reviewsRes.data.totalReviews || 0);
      setProfile({
        name: profileRes.data.name || '',
        email: profileRes.data.email || '',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const resetServiceForm = () => {
    setForm({
      title: '',
      description: '',
      category: 'AC_REPAIR',
      price: '',
      status: 'ACTIVE',
    });
    setEditingServiceId(null);
  };

  const handleSaveService = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    const payload = {
      title: form.title,
      description: form.description,
      category: form.category,
      price: Number(form.price),
      status: form.status,
    };

    try {
      if (editingServiceId) {
        await axiosClient.put(`/provider/services/${editingServiceId}`, payload);
        setSuccess('Service updated successfully.');
      } else {
        await axiosClient.post('/provider/services', payload);
        setSuccess('Service added successfully.');
      }
      resetServiceForm();
      await loadDashboard();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save service.');
    }
  };

  const handleEditService = (service) => {
    setEditingServiceId(service.id);
    setForm({
      title: service.title,
      description: service.description,
      category: service.category,
      price: service.price,
      status: service.status,
    });
    document.getElementById('services')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const handleDeleteService = async (id) => {
    setError('');
    setSuccess('');
    try {
      await axiosClient.delete(`/provider/services/${id}`);
      setSuccess('Service deleted successfully.');
      await loadDashboard();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete service.');
    }
  };

  const handleToggleServiceStatus = async (service) => {
    setError('');
    setSuccess('');
    const nextStatus = service.status === 'ACTIVE' ? 'PAUSED' : 'ACTIVE';
    try {
      await axiosClient.put(`/provider/services/${service.id}/status`, { status: nextStatus });
      setSuccess(`Service ${nextStatus === 'ACTIVE' ? 'activated' : 'paused'} successfully.`);
      await loadDashboard();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update service status.');
    }
  };

  const handleBookingStatusChange = async (bookingId, status) => {
    setError('');
    setSuccess('');
    try {
      await axiosClient.put(`/provider/bookings/${bookingId}/status`, { status });
      setSuccess('Booking status updated successfully.');
      await loadDashboard();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update booking status.');
    }
  };

  const handleSaveAvailability = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');
    try {
      await axiosClient.put('/provider/availability', availability);
      setSuccess('Availability updated successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update availability.');
    }
  };

  const handleUpdateProfile = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');
    try {
      await axiosClient.put('/provider/profile', {
        name: profile.name,
      });
      setSuccess('Profile updated successfully.');
      await loadDashboard();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile.');
    }
  };

  const handleChangePassword = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');
    try {
      await axiosClient.put('/provider/change-password', passwordForm);
      setPasswordForm({ currentPassword: '', newPassword: '' });
      setSuccess('Password changed successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to change password.');
    }
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

  const earningsMax = useMemo(
    () => Math.max(Number(overview.totalEarnings || 0), Number(overview.thisMonthEarnings || 0), 1),
    [overview],
  );

  return (
    <DashboardLayout title="Provider Dashboard" welcomeName={overview.providerName} sections={sections} onLogout={handleLogout}>
      {loading ? (
        <LoadingSpinner label="Loading provider dashboard..." />
      ) : (
        <>
          {error && <p className="error">{error}</p>}
          {success && <p className="success">{success}</p>}

          <section id="dashboard" className="section-card">
            <h3>Overview</h3>
            <div className="stats-grid">
              <article className="stat-card soft-primary">
                <p className="stat-icon">🛠️</p>
                <p className="stat-title">Total Services</p>
                <p className="stat-value">{overview.totalServices}</p>
              </article>
              <article className="stat-card soft-warning">
                <p className="stat-icon">📦</p>
                <p className="stat-title">Total Bookings</p>
                <p className="stat-value">{overview.totalBookings}</p>
              </article>
              <article className="stat-card soft-success">
                <p className="stat-icon">✅</p>
                <p className="stat-title">Completed</p>
                <p className="stat-value">{overview.totalCompletedBookings}</p>
              </article>
              <article className="stat-card soft-primary">
                <p className="stat-icon">💰</p>
                <p className="stat-title">Total Earnings</p>
                <p className="stat-value">₹{overview.totalEarnings}</p>
              </article>
            </div>
          </section>

          <section id="services" className="section-card">
            <h3>{editingServiceId ? 'Edit Service' : 'Add New Service'}</h3>
            <form className="form" onSubmit={handleSaveService}>
              <div className="form-grid">
                <div className="field">
                  <label>Title</label>
                  <input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} required />
                </div>
                <div className="field">
                  <label>Category</label>
                  <select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}>
                    {categories.map((category) => (
                      <option key={category.value} value={category.value}>{category.label}</option>
                    ))}
                  </select>
                </div>
                <div className="field">
                  <label>Price</label>
                  <input type="number" step="0.01" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} required />
                </div>
                <div className="field">
                  <label>Status</label>
                  <select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="PAUSED">PAUSED</option>
                  </select>
                </div>
              </div>
              <div className="field">
                <label>Description</label>
                <textarea rows={3} value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} required />
              </div>
              <div className="button-row">
                <button type="submit">{editingServiceId ? 'Update Service' : 'Add Service'}</button>
                {editingServiceId && (
                  <button type="button" className="btn-secondary" onClick={resetServiceForm}>Cancel Edit</button>
                )}
              </div>
            </form>

            <h3 style={{ marginTop: 18 }}>My Services</h3>
            <div className="card-grid">
              {services.map((service) => (
                <article key={service.id} className="service-card">
                  <div className="service-head">
                    <h4>{service.title}</h4>
                    <span className={`badge ${service.status === 'ACTIVE' ? 'badge-active' : 'badge-paused'}`}>{service.status}</span>
                  </div>
                  <p className="muted-text">#{service.id}</p>
                  <p>{service.description}</p>
                  <div className="button-row" style={{ marginTop: 8 }}>
                    <span className="badge badge-category">{displayLabel(service.category)}</span>
                    <span className="price-tag">₹{service.price}</span>
                  </div>
                  <div className="button-row" style={{ marginTop: 12 }}>
                    <button type="button" className="btn-info" onClick={() => handleEditService(service)}>Edit</button>
                    <button type="button" className="btn-danger" onClick={() => handleDeleteService(service.id)}>Delete</button>
                    <button type="button" className="btn-warning" onClick={() => handleToggleServiceStatus(service)}>
                      {service.status === 'ACTIVE' ? 'Pause' : 'Activate'}
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section id="bookings" className="section-card">
            <h3>Booking Management</h3>
            <div className="card-grid">
              {bookings.map((booking) => (
                <article key={booking.id} className="booking-card">
                  <div className="booking-head">
                    <h4>Booking #{booking.id}</h4>
                    <span className={statusClass(booking.status)}>{booking.status}</span>
                  </div>
                  <p><strong>Customer:</strong> {booking.customer?.name || '-'}</p>
                  <p><strong>Service:</strong> {booking.service?.title || '-'}</p>
                  <p><strong>Date:</strong> {booking.bookingDate || '-'}</p>
                  <p><strong>Time:</strong> {formatBookingTime(booking.bookingTime)}</p>
                  <p><strong>Address:</strong> {booking.address || '-'}</p>
                  <div className="field" style={{ marginTop: 10 }}>
                    <label>Update Status</label>
                    <select
                      className="action-select"
                      value={booking.status}
                      disabled={isFinalBookingStatus(booking.status)}
                      onChange={(event) => handleBookingStatusChange(booking.id, event.target.value)}
                    >
                      {bookingStatuses.map((status) => (
                        <option key={status} value={status}>{status}</option>
                      ))}
                    </select>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section id="earnings" className="section-card">
            <h3>Earnings</h3>
            <div className="stats-grid">
              <article className="stat-card soft-success">
                <p className="stat-title">Completed Bookings</p>
                <p className="stat-value">{overview.totalCompletedBookings}</p>
              </article>
              <article className="stat-card soft-primary">
                <p className="stat-title">Total Earnings</p>
                <p className="stat-value">₹{overview.totalEarnings}</p>
              </article>
              <article className="stat-card soft-warning">
                <p className="stat-title">This Month</p>
                <p className="stat-value">₹{overview.thisMonthEarnings}</p>
              </article>
            </div>

            <div className="chart-wrap">
              <div className="chart-row">
                <span>Total Earnings</span>
                <div className="chart-bar"><div className="chart-fill" style={{ width: `${(Number(overview.totalEarnings || 0) / earningsMax) * 100}%` }} /></div>
                <strong>₹{overview.totalEarnings}</strong>
              </div>
              <div className="chart-row">
                <span>This Month</span>
                <div className="chart-bar"><div className="chart-fill" style={{ width: `${(Number(overview.thisMonthEarnings || 0) / earningsMax) * 100}%` }} /></div>
                <strong>₹{overview.thisMonthEarnings}</strong>
              </div>
            </div>
          </section>

          <section id="reviews" className="section-card">
            <h3>Ratings & Reviews</h3>
            <p className="rating-stars">{stars(Math.round(averageRating || 0))}</p>
            <p className="muted-text">Average: {totalReviews > 0 ? averageRating.toFixed(1) : 'N/A'} ({totalReviews} review{totalReviews === 1 ? '' : 's'})</p>
            <div className="review-list fade-in" style={{ marginTop: 10 }}>
              {reviews.length === 0 ? (
                <p className="muted-text">No reviews available yet.</p>
              ) : (
                reviews.map((review) => (
                  <article key={review.reviewId} className="review-card">
                    <div className="review-head">
                      <strong>{review.customerName || 'Customer'}</strong>
                      <span className="rating-stars">{stars(review.rating || 0)}</span>
                    </div>
                    <p><strong>Service:</strong> {review.serviceTitle || '-'}</p>
                    <p>{review.comment || '-'}</p>
                    <p className="hint">{formatDateTime(review.createdAt)}</p>
                  </article>
                ))
              )}
            </div>
          </section>

          <section id="profile" className="section-card">
            <h3>Profile & Availability</h3>
            <form className="form" onSubmit={handleUpdateProfile}>
              <div className="form-grid">
                <div className="field">
                  <label>Username</label>
                  <input value={profile.name} onChange={(event) => setProfile({ ...profile, name: event.target.value })} required />
                </div>
                <div className="field">
                  <label>Email</label>
                  <input value={profile.email} disabled />
                </div>
              </div>
              <button type="submit">Update Username</button>
            </form>

            <form className="form" onSubmit={handleChangePassword} style={{ marginTop: 12 }}>
              <div className="form-grid">
                <div className="field">
                  <label>Current Password</label>
                  <input
                    type="password"
                    value={passwordForm.currentPassword}
                    onChange={(event) => setPasswordForm({ ...passwordForm, currentPassword: event.target.value })}
                    required
                  />
                </div>
                <div className="field">
                  <label>New Password</label>
                  <input
                    type="password"
                    value={passwordForm.newPassword}
                    onChange={(event) => setPasswordForm({ ...passwordForm, newPassword: event.target.value })}
                    required
                  />
                </div>
              </div>
              <button type="submit">Change Password</button>
            </form>

            <form className="form" onSubmit={handleSaveAvailability}>
              <div className="form-grid">
                <div className="field">
                  <label>Working Days</label>
                  <input
                    placeholder="Mon-Fri"
                    value={availability.workingDays}
                    onChange={(event) => setAvailability({ ...availability, workingDays: event.target.value })}
                  />
                </div>
                <div className="field">
                  <label>Working Hours</label>
                  <input
                    placeholder="09:00-18:00"
                    value={availability.workingHours}
                    onChange={(event) => setAvailability({ ...availability, workingHours: event.target.value })}
                  />
                </div>
              </div>

              <label className="checkbox-row">
                <input
                  type="checkbox"
                  checked={availability.emergencyServiceEnabled}
                  onChange={(event) => setAvailability({ ...availability, emergencyServiceEnabled: event.target.checked })}
                />
                Enable emergency service
              </label>
              <button type="submit">Save Availability</button>
            </form>
          </section>
        </>
      )}
    </DashboardLayout>
  );
}
