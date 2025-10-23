import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './EditProfile.css';

const EditProfile = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    bio: '',
    university: '',
    graduationYear: '',
    city: '',
    state: '',
    country: '',
    zipcode: '',
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await authService.getProfileDetails();

      // Populate form with existing data
      setFormData({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        phoneNumber: data.phoneNumber || '',
        bio: data.bio || '',
        university: data.university || '',
        graduationYear: data.graduationYear || '',
        city: data.city || '',
        state: data.state || '',
        country: data.country || '',
        zipcode: data.zipcode || '',
      });

      setError('');
    } catch (err) {
      setError('Failed to load profile. Please try again.');
      console.error('Error fetching profile:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      setSaving(true);
      setError('');
      setSuccess('');

      // Convert graduationYear to number if provided
      const dataToSubmit = {
        ...formData,
        graduationYear: formData.graduationYear ? parseInt(formData.graduationYear) : null,
      };

      await authService.updateProfile(dataToSubmit);

      setSuccess('Profile updated successfully!');

      // Redirect to profile page after 1.5 seconds
      setTimeout(() => {
        navigate('/profile');
      }, 1500);

    } catch (err) {
      setError(err.response?.data?.error || 'Failed to update profile. Please try again.');
      console.error('Error updating profile:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate('/profile');
  };

  if (loading) {
    return (
      <div className="edit-profile-loading">
        <div className="edit-profile-loading-content">
          <div className="edit-profile-loading-spinner"></div>
          <p className="edit-profile-loading-text">Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="edit-profile-container">
      <div className="edit-profile-wrapper">
        {/* Header */}
        <div className="edit-profile-header">
          <h1>Edit Profile</h1>
          <p>Update your personal information</p>
        </div>

        {/* Alert Messages */}
        {error && (
          <div className="edit-profile-alert error">
            <p>{error}</p>
          </div>
        )}

        {success && (
          <div className="edit-profile-alert success">
            <p>{success}</p>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="edit-profile-form">
          {/* Personal Information */}
          <div className="edit-profile-section">
            <h2>Personal Information</h2>

            <div className="edit-profile-grid">
              <div className="edit-profile-field">
                <label htmlFor="firstName">First Name</label>
                <input
                  type="text"
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  placeholder="Enter your first name"
                />
              </div>

              <div className="edit-profile-field">
                <label htmlFor="lastName">Last Name</label>
                <input
                  type="text"
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  placeholder="Enter your last name"
                />
              </div>

              <div className="edit-profile-field">
                <label htmlFor="phoneNumber">Phone Number</label>
                <input
                  type="tel"
                  id="phoneNumber"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  placeholder="+1 (555) 123-4567"
                />
              </div>
            </div>

            <div className="edit-profile-field">
              <label htmlFor="bio">Bio</label>
              <textarea
                id="bio"
                name="bio"
                value={formData.bio}
                onChange={handleChange}
                rows="4"
                placeholder="Tell us about yourself..."
              />
            </div>
          </div>

          {/* Academic Information */}
          <div className="edit-profile-section">
            <h2>Academic Information</h2>

            <div className="edit-profile-grid">
              <div className="edit-profile-field">
                <label htmlFor="university">University</label>
                <input
                  type="text"
                  id="university"
                  name="university"
                  value={formData.university}
                  onChange={handleChange}
                  placeholder="e.g., University at Buffalo"
                />
                <p className="edit-profile-field-hint">
                  Enter your university name (supports all universities worldwide)
                </p>
              </div>

              <div className="edit-profile-field">
                <label htmlFor="graduationYear">Graduation Year</label>
                <input
                  type="number"
                  id="graduationYear"
                  name="graduationYear"
                  value={formData.graduationYear}
                  onChange={handleChange}
                  placeholder="2025"
                  min="2000"
                  max="2050"
                />
              </div>
            </div>
          </div>

          {/* Location Information */}
          <div className="edit-profile-section">
            <h2>Location (Optional)</h2>

            <div className="edit-profile-grid">
              <div className="edit-profile-field">
                <label htmlFor="city">City</label>
                <input
                  type="text"
                  id="city"
                  name="city"
                  value={formData.city}
                  onChange={handleChange}
                  placeholder="e.g., Buffalo"
                />
              </div>

              <div className="edit-profile-field">
                <label htmlFor="state">State/Province</label>
                <input
                  type="text"
                  id="state"
                  name="state"
                  value={formData.state}
                  onChange={handleChange}
                  placeholder="e.g., New York"
                />
              </div>

              <div className="edit-profile-field">
                <label htmlFor="country">Country</label>
                <input
                  type="text"
                  id="country"
                  name="country"
                  value={formData.country}
                  onChange={handleChange}
                  placeholder="e.g., United States"
                />
              </div>

              <div className="edit-profile-field">
                <label htmlFor="zipcode">Zipcode</label>
                <input
                  type="text"
                  id="zipcode"
                  name="zipcode"
                  value={formData.zipcode}
                  onChange={handleChange}
                  placeholder="e.g., 14260"
                />
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="edit-profile-actions">
            <button
              type="button"
              onClick={handleCancel}
              className="edit-profile-btn edit-profile-btn-cancel"
              disabled={saving}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="edit-profile-btn edit-profile-btn-submit"
              disabled={saving}
            >
              {saving ? (
                <span className="edit-profile-btn-loading">
                  <div className="edit-profile-spinner"></div>
                  Saving...
                </span>
              ) : (
                'Save Changes'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditProfile;
