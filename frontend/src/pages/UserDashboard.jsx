import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import DashboardLayout from '../components/DashboardLayout';
import LoadingSpinner from '../components/LoadingSpinner';

const categories = [
  'ALL',
  'AC_REPAIR',
  'PLUMBING',
  'ELECTRICAL',
  'CLEANING',
  'PAINTING',
  'HAIR_CUT',
  'MAKEUP',
  'NAILS',
  'ROOM_MAKEOVERS',
  'OTHER_SERVICES',
];

function formatCategory(value) {
  return value ? value.replaceAll('_', ' ') : '-';
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

function stars(value) {
  if (!value) return '☆☆☆☆☆';
  const rounded = Math.round(value);
  return `${'★'.repeat(rounded)}${'☆'.repeat(5 - rounded)}`;
}

export default function UserDashboard() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [overview, setOverview] = useState({
    userName: '',
    totalBookings: 0,
    pendingBookings: 0,
    completedBookings: 0,
    cancelledBookings: 0,
  });
  const [services, setServices] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [profile, setProfile] = useState({ name: '', email: '', phone: '' });
  const [profileDirty, setProfileDirty] = useState(false);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('ALL');
  const [sort, setSort] = useState('none');
  const [loading, setLoading] = useState(true);

  const [bookingModal, setBookingModal] = useState({ open: false, service: null, date: '', time: '', address: '' });
  const [cancelModal, setCancelModal] = useState({ open: false, bookingId: null, reason: '' });
  const [reviewModal, setReviewModal] = useState({ open: false, bookingId: null, rating: 5, comment: '' });
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' });

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const loadData = async (showLoader = true) => {
    if (showLoader) {
      setLoading(true);
    }
    const categoryParam = category === 'ALL' ? '' : category;
    const sortParam = sort === 'none' ? '' : sort;

    try {
      const [overviewRes, servicesRes, bookingsRes, notificationsRes, profileRes] = await Promise.all([
        axiosClient.get('/user/overview'),
        axiosClient.get('/user/services', {
          params: {
            search,
            category: categoryParam,
            sort: sortParam,
          },
        }),
        axiosClient.get('/user/bookings'),
        axiosClient.get('/user/notifications'),
        axiosClient.get('/user/profile'),
      ]);

      setOverview(overviewRes.data);
      setServices(servicesRes.data);
      setBookings(bookingsRes.data);
      setNotifications(notificationsRes.data);
      if (!profileDirty) {
        setProfile(profileRes.data);
      }
    } finally {
      if (showLoader) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadData(true);
    const timer = setInterval(() => {
      loadData(false);
    }, 15000);
    return () => clearInterval(timer);
  }, [search, category, sort, profileDirty]);

  const openBookingModal = (service) => {
    setBookingModal({ open: true, service, date: '', time: '', address: '' });
  };

  const closeBookingModal = () => {
    setBookingModal({ open: false, service: null, date: '', time: '', address: '' });
  };

  const confirmBooking = async () => {
    if (!bookingModal.service) return;
    setError('');
    setMessage('');
    try {
      await axiosClient.post('/user/book', {
        serviceId: bookingModal.service.id,
        bookingDate: bookingModal.date,
        bookingTime: bookingModal.time,
        address: bookingModal.address,
      });
      closeBookingModal();
      setMessage('Booking confirmed successfully.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to create booking.');
    }
  };

  const confirmCancelBooking = async () => {
    setError('');
    setMessage('');
    try {
      await axiosClient.put(`/user/bookings/${cancelModal.bookingId}/cancel`, {
        cancellationReason: cancelModal.reason,
      });
      setCancelModal({ open: false, bookingId: null, reason: '' });
      setMessage('Booking cancelled.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to cancel booking.');
    }
  };

  const submitReview = async () => {
    setError('');
    setMessage('');
    try {
      await axiosClient.post('/user/reviews', {
        bookingId: reviewModal.bookingId,
        rating: Number(reviewModal.rating),
        comment: reviewModal.comment,
      });
      setReviewModal({ open: false, bookingId: null, rating: 5, comment: '' });
      setMessage('Review submitted successfully.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to submit review.');
    }
  };

  const updateProfile = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await axiosClient.put('/user/profile', {
        name: profile.name,
        phone: profile.phone,
      });
      setProfileDirty(false);
      setMessage('Profile updated successfully.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to update profile.');
    }
  };

  const changePassword = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await axiosClient.put('/user/change-password', passwordForm);
      setPasswordForm({ currentPassword: '', newPassword: '' });
      setMessage('Password changed successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to change password.');
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const renderBookingAction = (booking) => {
    if (booking.status === 'PENDING') {
      return (
        <button
          type="button"
          className="btn-danger"
          onClick={() => setCancelModal({ open: true, bookingId: booking.id, reason: '' })}
        >
          Cancel Booking
        </button>
      );
    }

    if (booking.status === 'ACCEPTED') {
      return <span className="status status-accepted">In Progress</span>;
    }

    if (booking.status === 'COMPLETED') {
      return (
        <button
          type="button"
          className="btn-success"
          onClick={() => setReviewModal({ open: true, bookingId: booking.id, rating: 5, comment: '' })}
        >
          Leave Review
        </button>
      );
    }

    if (booking.status === 'CANCELLED') {
      return <span className="muted-text">Reason: {booking.cancellationReason || 'N/A'}</span>;
    }

    return null;
  };

  const sections = [
    { key: 'dashboard', label: 'Dashboard' },
    { key: 'services', label: 'Services' },
    { key: 'bookings', label: 'Bookings' },
    { key: 'earnings', label: 'Earnings' },
    { key: 'reviews', label: 'Reviews' },
    { key: 'profile', label: 'Profile' },
  ];

  const bookingProgress = useMemo(() => {
    const total = Math.max(overview.totalBookings || 0, 1);
    return [
      { label: 'Pending', value: overview.pendingBookings || 0, width: ((overview.pendingBookings || 0) / total) * 100 },
      { label: 'Completed', value: overview.completedBookings || 0, width: ((overview.completedBookings || 0) / total) * 100 },
      { label: 'Cancelled', value: overview.cancelledBookings || 0, width: ((overview.cancelledBookings || 0) / total) * 100 },
    ];
  }, [overview]);

  const completedForReview = bookings.filter((booking) => booking.status === 'COMPLETED').length;

  return (
    <DashboardLayout title="User Dashboard" welcomeName={overview.userName} sections={sections} onLogout={handleLogout}>
      {loading ? (
        <LoadingSpinner label="Loading user dashboard..." />
      ) : (
        <>
          {message && <p className="success">{message}</p>}
          {error && <p className="error">{error}</p>}

          <section id="dashboard" className="section-card">
            <h3>Overview</h3>
            <div className="stats-grid">
              <article className="stat-card soft-primary">
                <p className="stat-icon">📦</p>
                <p className="stat-title">Total Bookings</p>
                <p className="stat-value">{overview.totalBookings}</p>
              </article>
              <article className="stat-card soft-warning">
                <p className="stat-icon">⏳</p>
                <p className="stat-title">Pending</p>
                <p className="stat-value">{overview.pendingBookings}</p>
              </article>
              <article className="stat-card soft-success">
                <p className="stat-icon">✅</p>
                <p className="stat-title">Completed</p>
                <p className="stat-value">{overview.completedBookings}</p>
              </article>
              <article className="stat-card soft-danger">
                <p className="stat-icon">❌</p>
                <p className="stat-title">Cancelled</p>
                <p className="stat-value">{overview.cancelledBookings}</p>
              </article>
            </div>
          </section>

          <section id="services" className="section-card">
            <h3>Available Services</h3>
            <div className="filter-row" style={{ marginBottom: 12 }}>
              <input placeholder="Search by service title" value={search} onChange={(event) => setSearch(event.target.value)} />
              <select value={category} onChange={(event) => setCategory(event.target.value)}>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>{formatCategory(cat)}</option>
                ))}
              </select>
              <select value={sort} onChange={(event) => setSort(event.target.value)}>
                <option value="none">Sort by price</option>
                <option value="low">Low to High</option>
                <option value="high">High to Low</option>
              </select>
            </div>

            <div className="card-grid">
              {services.map((service) => (
                <article key={service.id} className="service-card">
                  <div className="service-head">
                    <h4>{service.title}</h4>
                    <span className="badge badge-category">{formatCategory(service.category)}</span>
                  </div>
                  <p>{service.description}</p>
                  <p><strong>Provider:</strong> {service.providerName}</p>
                  <p className="price-tag">₹{service.price}</p>
                  <p><strong>Rating:</strong> <span className="rating-stars">{stars(service.averageRating)}</span> {service.averageRating ? `(${service.averageRating.toFixed(1)})` : '(N/A)'}</p>
                  <button type="button" onClick={() => openBookingModal(service)}>Book Now</button>
                </article>
              ))}
            </div>
          </section>

          <section id="bookings" className="section-card">
            <h3>My Bookings</h3>
            <div className="card-grid">
              {bookings.map((booking) => (
                <article key={booking.id} className="booking-card">
                  <div className="booking-head">
                    <h4>Booking #{booking.id}</h4>
                    <span className={statusClass(booking.status)}>{booking.status}</span>
                  </div>
                  <p><strong>Service:</strong> {booking.serviceTitle}</p>
                  <p><strong>Provider:</strong> {booking.providerName}</p>
                  <p><strong>Date:</strong> {booking.bookingDate || '-'}</p>
                  <p><strong>Time:</strong> {booking.bookingTime || '-'}</p>
                  <p><strong>Address:</strong> {booking.address || '-'}</p>
                  <div className="button-row" style={{ marginTop: 10 }}>
                    {renderBookingAction(booking)}
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section id="earnings" className="section-card">
            <h3>Booking Insights</h3>
            <p className="muted-text">Track your booking trends with a quick visual summary.</p>
            <div className="chart-wrap">
              {bookingProgress.map((item) => (
                <div className="chart-row" key={item.label}>
                  <span>{item.label}</span>
                  <div className="chart-bar"><div className="chart-fill" style={{ width: `${item.width}%` }} /></div>
                  <strong>{item.value}</strong>
                </div>
              ))}
            </div>
          </section>

          <section id="reviews" className="section-card">
            <h3>Reviews</h3>
            <p className="muted-text">Completed bookings available for rating: {completedForReview}</p>
            <div className="review-list fade-in" style={{ marginTop: 10 }}>
              {bookings.filter((booking) => booking.status === 'COMPLETED').slice(0, 5).map((booking) => (
                <article className="review-card" key={booking.id}>
                  <div className="review-head">
                    <strong>{booking.serviceTitle}</strong>
                    <button
                      type="button"
                      className="btn-success"
                      onClick={() => setReviewModal({ open: true, bookingId: booking.id, rating: 5, comment: '' })}
                    >
                      Rate Now
                    </button>
                  </div>
                  <p><strong>Provider:</strong> {booking.providerName}</p>
                  <p className="hint">Booking #{booking.id}</p>
                </article>
              ))}
            </div>
          </section>

          <section id="profile" className="section-card">
            <h3>Profile & Security</h3>
            <form className="form" onSubmit={updateProfile}>
              <div className="form-grid">
                <div className="field">
                  <label>Name</label>
                  <input
                    value={profile.name}
                    onChange={(event) => {
                      setProfileDirty(true);
                      setProfile({ ...profile, name: event.target.value });
                    }}
                  />
                </div>
                <div className="field">
                  <label>Email</label>
                  <input value={profile.email} disabled />
                </div>
                <div className="field">
                  <label>Phone</label>
                  <input
                    value={profile.phone || ''}
                    onChange={(event) => {
                      setProfileDirty(true);
                      setProfile({ ...profile, phone: event.target.value });
                    }}
                  />
                </div>
              </div>
              <button type="submit">Update Profile</button>
            </form>

            <form className="form" onSubmit={changePassword} style={{ marginTop: 12 }}>
              <div className="form-grid">
                <div className="field">
                  <label>Current Password</label>
                  <input type="password" value={passwordForm.currentPassword} onChange={(event) => setPasswordForm({ ...passwordForm, currentPassword: event.target.value })} required />
                </div>
                <div className="field">
                  <label>New Password</label>
                  <input type="password" value={passwordForm.newPassword} onChange={(event) => setPasswordForm({ ...passwordForm, newPassword: event.target.value })} required />
                </div>
              </div>
              <button type="submit">Change Password</button>
            </form>

            <h4 style={{ marginTop: 16, marginBottom: 10 }}>Notifications</h4>
            {notifications.length === 0 ? (
              <p className="muted-text">No notifications yet.</p>
            ) : (
              <div className="review-list">
                {notifications.map((note, index) => (
                  <article key={`${note}-${index}`} className="list-card">{note}</article>
                ))}
              </div>
            )}
          </section>
        </>
      )}

      {bookingModal.open && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Confirm Booking</h3>
            <p><strong>Service:</strong> {bookingModal.service?.title}</p>
            <div className="form-grid">
              <div className="field">
                <label>Date</label>
                <input type="date" value={bookingModal.date} onChange={(event) => setBookingModal({ ...bookingModal, date: event.target.value })} />
              </div>
              <div className="field">
                <label>Time</label>
                <input type="time" value={bookingModal.time} onChange={(event) => setBookingModal({ ...bookingModal, time: event.target.value })} />
              </div>
            </div>
            <div className="field">
              <label>Address</label>
              <input value={bookingModal.address} onChange={(event) => setBookingModal({ ...bookingModal, address: event.target.value })} />
            </div>
            <div className="button-row">
              <button type="button" onClick={confirmBooking}>Confirm</button>
              <button type="button" className="btn-secondary" onClick={closeBookingModal}>Close</button>
            </div>
          </div>
        </div>
      )}

      {cancelModal.open && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Cancel Booking</h3>
            <div className="field">
              <label>Reason</label>
              <input value={cancelModal.reason} onChange={(event) => setCancelModal({ ...cancelModal, reason: event.target.value })} />
            </div>
            <div className="button-row">
              <button type="button" className="btn-danger" onClick={confirmCancelBooking}>Confirm Cancel</button>
              <button type="button" className="btn-secondary" onClick={() => setCancelModal({ open: false, bookingId: null, reason: '' })}>Close</button>
            </div>
          </div>
        </div>
      )}

      {reviewModal.open && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Leave Review</h3>
            <div className="form-grid">
              <div className="field">
                <label>Rating</label>
                <select value={reviewModal.rating} onChange={(event) => setReviewModal({ ...reviewModal, rating: event.target.value })}>
                  <option value={5}>5 Stars</option>
                  <option value={4}>4 Stars</option>
                  <option value={3}>3 Stars</option>
                  <option value={2}>2 Stars</option>
                  <option value={1}>1 Star</option>
                </select>
              </div>
            </div>
            <div className="field">
              <label>Comment</label>
              <textarea rows={4} value={reviewModal.comment} onChange={(event) => setReviewModal({ ...reviewModal, comment: event.target.value })} />
            </div>
            <div className="button-row">
              <button type="button" className="btn-success" onClick={submitReview}>Submit Review</button>
              <button type="button" className="btn-secondary" onClick={() => setReviewModal({ open: false, bookingId: null, rating: 5, comment: '' })}>Close</button>
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}
