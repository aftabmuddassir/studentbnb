import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './Profile.css';

const Profile = () => {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await authService.getProfileDetails();
      setProfile(data);
      setError('');
    } catch (err) {
      setError('Failed to load profile. Please try again.');
      console.error('Error fetching profile:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEditProfile = () => {
    navigate('/edit-profile');
  };

  const handleBackToDashboard = () => {
    navigate('/dashboard');
  };

  if (loading) {
    return (
      <div className="profile-loading">
        <div className="profile-loading-content">
          <div className="profile-spinner"></div>
          <p className="profile-loading-text">Loading profile...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="profile-error-container">
        <div className="profile-error-card">
          <div className="profile-error-message">{error}</div>
          <button onClick={fetchProfile} className="profile-retry-btn">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-container">
      <div className="profile-wrapper">
        {/* Back to Dashboard Button */}
        <div style={{ marginBottom: '1.5rem' }}>
          <button onClick={handleBackToDashboard} className="profile-back-btn">
            ← Back to Dashboard
          </button>
        </div>

        {/* Header */}
        <div className="profile-header-card">
          <div className="profile-header-content">
            <div className="profile-avatar-section">
              {/* Profile Picture Placeholder */}
              <div className="profile-avatar">
                {profile?.firstName && profile?.lastName
                  ? `${profile.firstName[0]}${profile.lastName[0]}`
                  : profile?.email[0].toUpperCase()}
              </div>

              {/* User Info */}
              <div className="profile-user-info">
                <h1>
                  {profile?.firstName && profile?.lastName
                    ? `${profile.firstName} ${profile.lastName}`
                    : 'Complete Your Profile'}
                </h1>
                <p>{profile?.email}</p>
                <span className={`profile-role-badge ${profile?.role === 'LANDLORD' ? 'landlord' : 'student'}`}>
                  {profile?.role}
                </span>
              </div>
            </div>

            {/* Edit Button */}
            <button onClick={handleEditProfile} className="profile-edit-btn">
              Edit Profile
            </button>
          </div>
        </div>

        {/* Profile Details */}
        <div className="profile-details-card">
          <h2>Profile Information</h2>

          <div className="profile-grid">
            {/* Contact Information */}
            <div className="profile-section">
              <h3 className="profile-section-title">Contact Information</h3>

              <div className="profile-field">
                <label className="profile-field-label">Email</label>
                <p className="profile-field-value">{profile?.email || 'Not provided'}</p>
              </div>

              <div className="profile-field">
                <label className="profile-field-label">Phone Number</label>
                <p className="profile-field-value">{profile?.phoneNumber || 'Not provided'}</p>
              </div>

              <div className="profile-field">
                <label className="profile-field-label">Email Verified</label>
                <p className="profile-field-value">
                  <span className={`profile-verified-badge ${profile?.emailVerified ? 'verified' : 'not-verified'}`}>
                    {profile?.emailVerified ? 'Verified' : 'Not Verified'}
                  </span>
                </p>
              </div>
            </div>

            {/* Academic Information */}
            <div className="profile-section">
              <h3 className="profile-section-title">Academic Information</h3>

              <div className="profile-field">
                <label className="profile-field-label">University</label>
                <p className="profile-field-value">{profile?.university || 'Not provided'}</p>
              </div>

              <div className="profile-field">
                <label className="profile-field-label">Graduation Year</label>
                <p className="profile-field-value">{profile?.graduationYear || 'Not provided'}</p>
              </div>
            </div>

            {/* Location Information */}
            <div className="profile-section">
              <h3 className="profile-section-title">Location Information</h3>

              <div className="profile-field">
                <label className="profile-field-label">City</label>
                <p className="profile-field-value">{profile?.city || 'Not provided'}</p>
              </div>

              <div className="profile-field">
                <label className="profile-field-label">State</label>
                <p className="profile-field-value">{profile?.state || 'Not provided'}</p>
              </div>

              <div className="profile-field">
                <label className="profile-field-label">Country</label>
                <p className="profile-field-value">{profile?.country || 'Not provided'}</p>
              </div>

              <div className="profile-field">
                <label className="profile-field-label">Zipcode</label>
                <p className="profile-field-value">{profile?.zipcode || 'Not provided'}</p>
              </div>
            </div>

            {/* Bio */}
            <div className="profile-section">
              <h3 className="profile-section-title">About</h3>

              <div className="profile-field">
                <label className="profile-field-label">Bio</label>
                <p className="profile-field-value profile-bio">
                  {profile?.bio || 'No bio provided yet. Click "Edit Profile" to add information about yourself.'}
                </p>
              </div>
            </div>
          </div>

          {/* Account Information */}
          <div className="profile-account-section">
            <h3 className="profile-section-title">Account Information</h3>

            <div className="profile-account-grid">
              <div className="profile-field">
                <label className="profile-field-label">Member Since</label>
                <p className="profile-field-value">
                  {profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : 'N/A'}
                </p>
              </div>

              <div className="profile-field">
                <label className="profile-field-label">Last Updated</label>
                <p className="profile-field-value">
                  {profile?.updatedAt ? new Date(profile.updatedAt).toLocaleDateString() : 'N/A'}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
