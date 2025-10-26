import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  createListing,
  uploadMultiplePhotos,
  addAmenities,
  getAmenityTypes,
  getPreferenceTypes,
  setPreferences,
} from '../services/listingService';
import './CreateListing.css';

const CreateListing = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [availableAmenities, setAvailableAmenities] = useState([]);
  const [preferenceTypes, setPreferenceTypes] = useState({
    dietaryPreferences: [],
    genderPreferences: [],
    smokingPreferences: [],
  });

  // Form state
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    address: '',
    city: '',
    state: '',
    zipCode: '',
    rent: '',
    securityDeposit: '',
    bedrooms: 1,
    bathrooms: 1,
    squareFeet: '',
    propertyType: 'APARTMENT',
    leaseType: 'SEMESTER',
    leaseDurationMonths: 6,
    availableFrom: '',
    availableUntil: '',
    furnished: false,
    utilitiesIncluded: false,
    petsAllowed: false,
    smokingAllowed: false,
    contactEmail: '',
    contactPhone: '',
    nearestUniversity: '',
    distanceToCampusKm: '',
  });

  const [selectedAmenities, setSelectedAmenities] = useState([]);
  const [selectedPreferences, setSelectedPreferences] = useState({
    dietaryPreference: '',
    genderPreference: '',
    smokingPreference: '',
    additionalNotes: '',
  });
  const [photos, setPhotos] = useState([]);
  const [photoPreviews, setPhotoPreviews] = useState([]);
  const [dragActive, setDragActive] = useState(false);

  useEffect(() => {
    fetchAmenities();
    fetchPreferenceTypes();
    // Set default contact email from localStorage
    const userEmail = localStorage.getItem('userEmail');
    if (userEmail) {
      setFormData((prev) => ({ ...prev, contactEmail: userEmail }));
    }
  }, []);

  const fetchAmenities = async () => {
    try {
      const amenities = await getAmenityTypes();
      setAvailableAmenities(amenities);
    } catch (err) {
      console.error('Error fetching amenities:', err);
    }
  };

  const fetchPreferenceTypes = async () => {
    try {
      const types = await getPreferenceTypes();
      setPreferenceTypes(types);
    } catch (err) {
      console.error('Error fetching preference types:', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleAmenityToggle = (amenity) => {
    if (selectedAmenities.includes(amenity)) {
      setSelectedAmenities(selectedAmenities.filter((a) => a !== amenity));
    } else {
      setSelectedAmenities([...selectedAmenities, amenity]);
    }
  };

  const handlePreferenceChange = (e) => {
    const { name, value } = e.target;
    setSelectedPreferences({
      ...selectedPreferences,
      [name]: value,
    });
  };

  // Photo upload handlers
  const handlePhotoChange = (e) => {
    const files = Array.from(e.target.files);
    handleFiles(files);
  };

  const handleFiles = (files) => {
    const validFiles = files.filter((file) => {
      if (!file.type.startsWith('image/')) {
        setError('Please upload only image files');
        return false;
      }
      if (file.size > 10 * 1024 * 1024) {
        setError('Image size should be less than 10MB');
        return false;
      }
      return true;
    });

    setPhotos((prev) => [...prev, ...validFiles]);

    // Create previews
    validFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPhotoPreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    });
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const files = Array.from(e.dataTransfer.files);
    handleFiles(files);
  };

  const removePhoto = (index) => {
    setPhotos((prev) => prev.filter((_, i) => i !== index));
    setPhotoPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      // Validate required fields
      if (!formData.title || !formData.description || !formData.address ||
          !formData.city || !formData.rent || !formData.contactEmail) {
        setError('Please fill in all required fields');
        setLoading(false);
        return;
      }

      // Step 1: Create listing
      const listingResponse = await createListing({
        ...formData,
        rent: parseFloat(formData.rent),
        securityDeposit: parseFloat(formData.securityDeposit) || 0,
        bedrooms: parseInt(formData.bedrooms),
        bathrooms: parseInt(formData.bathrooms),
        squareFeet: parseFloat(formData.squareFeet) || null,
        leaseDurationMonths: parseInt(formData.leaseDurationMonths),
        distanceToCampusKm: parseFloat(formData.distanceToCampusKm) || null,
        latitude: 0, // You can add geocoding later
        longitude: 0,
        currency: 'USD',
      });

      const listingId = listingResponse.data.id;

      // Step 2: Upload photos
      if (photos.length > 0) {
        await uploadMultiplePhotos(listingId, photos);
      }

      // Step 3: Add amenities
      if (selectedAmenities.length > 0) {
        await addAmenities(listingId, selectedAmenities);
      }

      // Step 4: Add preferences
      if (selectedPreferences.dietaryPreference || selectedPreferences.genderPreference ||
          selectedPreferences.smokingPreference || selectedPreferences.additionalNotes) {
        const prefsToSave = {};
        if (selectedPreferences.dietaryPreference) prefsToSave.dietaryPreference = selectedPreferences.dietaryPreference;
        if (selectedPreferences.genderPreference) prefsToSave.genderPreference = selectedPreferences.genderPreference;
        if (selectedPreferences.smokingPreference) prefsToSave.smokingPreference = selectedPreferences.smokingPreference;
        if (selectedPreferences.additionalNotes) prefsToSave.additionalNotes = selectedPreferences.additionalNotes;

        await setPreferences(listingId, prefsToSave);
      }

      setSuccess('Listing created successfully!');
      setTimeout(() => {
        navigate('/my-listings');
      }, 2000);
    } catch (err) {
      console.error('Error creating listing:', err);
      const errorMessage = err.response?.data?.error || err.response?.data?.message || err.message || 'Failed to create listing. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const propertyTypes = ['APARTMENT', 'HOUSE', 'CONDO', 'TOWNHOUSE', 'STUDIO', 'ROOM', 'SHARED_ROOM'];
  const leaseTypes = ['MONTHLY', 'SEMESTER', 'ACADEMIC_YEAR', 'YEARLY', 'SUMMER_ONLY', 'FLEXIBLE'];

  return (
    <div className="create-listing-container">
      <div className="create-listing-header">
        <h1>Create New Listing</h1>
        <button onClick={() => navigate('/dashboard')} className="btn-back">
          Back to Dashboard
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <form onSubmit={handleSubmit} className="create-listing-form">
        {/* Basic Information */}
        <section className="form-section">
          <h2>Basic Information</h2>

          <div className="form-group">
            <label htmlFor="title">Listing Title * (min 10 characters)</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              placeholder="e.g., Cozy 2BR Apartment near Campus"
              required
              minLength="10"
              maxLength="100"
            />
            <small style={{ color: '#6b7280', fontSize: '12px' }}>
              {formData.title.length}/100 characters
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description * (min 50 characters)</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="Describe your property in detail - location, amenities, nearby attractions, etc..."
              rows="5"
              required
              minLength="50"
              maxLength="2000"
            />
            <small style={{ color: formData.description.length < 50 ? '#ef4444' : '#6b7280', fontSize: '12px' }}>
              {formData.description.length}/2000 characters {formData.description.length < 50 && `(Need ${50 - formData.description.length} more)`}
            </small>
          </div>
        </section>

        {/* Location */}
        <section className="form-section">
          <h2>Location</h2>

          <div className="form-group">
            <label htmlFor="address">Street Address *</label>
            <input
              type="text"
              id="address"
              name="address"
              value={formData.address}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="city">City *</label>
              <input
                type="text"
                id="city"
                name="city"
                value={formData.city}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="state">State</label>
              <input
                type="text"
                id="state"
                name="state"
                value={formData.state}
                onChange={handleInputChange}
                placeholder="e.g., NY"
                maxLength="2"
              />
            </div>

            <div className="form-group">
              <label htmlFor="zipCode">Zip Code</label>
              <input
                type="text"
                id="zipCode"
                name="zipCode"
                value={formData.zipCode}
                onChange={handleInputChange}
                placeholder="e.g., 14260"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="nearestUniversity">Nearest University</label>
              <input
                type="text"
                id="nearestUniversity"
                name="nearestUniversity"
                value={formData.nearestUniversity}
                onChange={handleInputChange}
                placeholder="e.g., University at Buffalo"
              />
            </div>

            <div className="form-group">
              <label htmlFor="distanceToCampusKm">Distance to Campus (km)</label>
              <input
                type="number"
                id="distanceToCampusKm"
                name="distanceToCampusKm"
                value={formData.distanceToCampusKm}
                onChange={handleInputChange}
                step="0.1"
                min="0"
              />
            </div>
          </div>
        </section>

        {/* Property Details */}
        <section className="form-section">
          <h2>Property Details</h2>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="propertyType">Property Type *</label>
              <select
                id="propertyType"
                name="propertyType"
                value={formData.propertyType}
                onChange={handleInputChange}
                required
              >
                {propertyTypes.map((type) => (
                  <option key={type} value={type}>
                    {type.replace(/_/g, ' ')}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="bedrooms">Bedrooms *</label>
              <input
                type="number"
                id="bedrooms"
                name="bedrooms"
                value={formData.bedrooms}
                onChange={handleInputChange}
                min="0"
                max="10"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="bathrooms">Bathrooms *</label>
              <input
                type="number"
                id="bathrooms"
                name="bathrooms"
                value={formData.bathrooms}
                onChange={handleInputChange}
                min="1"
                max="10"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="squareFeet">Square Feet</label>
              <input
                type="number"
                id="squareFeet"
                name="squareFeet"
                value={formData.squareFeet}
                onChange={handleInputChange}
                min="0"
              />
            </div>
          </div>
        </section>

        {/* Pricing */}
        <section className="form-section">
          <h2>Pricing</h2>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="rent">Monthly Rent (USD) *</label>
              <input
                type="number"
                id="rent"
                name="rent"
                value={formData.rent}
                onChange={handleInputChange}
                min="0"
                step="0.01"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="securityDeposit">Security Deposit (USD)</label>
              <input
                type="number"
                id="securityDeposit"
                name="securityDeposit"
                value={formData.securityDeposit}
                onChange={handleInputChange}
                min="0"
                step="0.01"
              />
            </div>
          </div>
        </section>

        {/* Lease Information */}
        <section className="form-section">
          <h2>Lease Information</h2>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="leaseType">Lease Type *</label>
              <select
                id="leaseType"
                name="leaseType"
                value={formData.leaseType}
                onChange={handleInputChange}
                required
              >
                {leaseTypes.map((type) => (
                  <option key={type} value={type}>
                    {type.replace(/_/g, ' ')}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="leaseDurationMonths">Lease Duration (months)</label>
              <input
                type="number"
                id="leaseDurationMonths"
                name="leaseDurationMonths"
                value={formData.leaseDurationMonths}
                onChange={handleInputChange}
                min="1"
                max="24"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="availableFrom">Available From</label>
              <input
                type="date"
                id="availableFrom"
                name="availableFrom"
                value={formData.availableFrom}
                onChange={handleInputChange}
              />
            </div>

            <div className="form-group">
              <label htmlFor="availableUntil">Available Until</label>
              <input
                type="date"
                id="availableUntil"
                name="availableUntil"
                value={formData.availableUntil}
                onChange={handleInputChange}
              />
            </div>
          </div>
        </section>

        {/* Features & Preferences */}
        <section className="form-section">
          <h2>Features & Preferences</h2>

          <div className="checkbox-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="furnished"
                checked={formData.furnished}
                onChange={handleInputChange}
              />
              <span>Furnished</span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="utilitiesIncluded"
                checked={formData.utilitiesIncluded}
                onChange={handleInputChange}
              />
              <span>Utilities Included</span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="petsAllowed"
                checked={formData.petsAllowed}
                onChange={handleInputChange}
              />
              <span>Pets Allowed</span>
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                name="smokingAllowed"
                checked={formData.smokingAllowed}
                onChange={handleInputChange}
              />
              <span>Smoking Allowed</span>
            </label>
          </div>
        </section>

        {/* Amenities */}
        <section className="form-section">
          <h2>Amenities</h2>
          <p className="section-hint">Select all amenities that apply</p>

          <div className="amenities-grid">
            {availableAmenities.map((amenity) => (
              <label key={amenity} className="amenity-checkbox">
                <input
                  type="checkbox"
                  checked={selectedAmenities.includes(amenity)}
                  onChange={() => handleAmenityToggle(amenity)}
                />
                <span>{amenity.replace(/_/g, ' ')}</span>
              </label>
            ))}
          </div>
        </section>

        {/* Roommate/Tenant Preferences */}
        <section className="form-section">
          <h2>Roommate/Tenant Preferences</h2>
          <p className="section-hint">Specify your preferences for potential tenants (optional)</p>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="dietaryPreference">Dietary Preference</label>
              <select
                id="dietaryPreference"
                name="dietaryPreference"
                value={selectedPreferences.dietaryPreference}
                onChange={handlePreferenceChange}
              >
                <option value="">No Preference</option>
                {preferenceTypes.dietaryPreferences.map((pref) => (
                  <option key={pref} value={pref}>
                    {pref.replace(/_/g, ' ')}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="genderPreference">Gender Preference</label>
              <select
                id="genderPreference"
                name="genderPreference"
                value={selectedPreferences.genderPreference}
                onChange={handlePreferenceChange}
              >
                <option value="">No Preference</option>
                {preferenceTypes.genderPreferences.map((pref) => (
                  <option key={pref} value={pref}>
                    {pref.replace(/_/g, ' ')}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="smokingPreference">Smoking Preference</label>
              <select
                id="smokingPreference"
                name="smokingPreference"
                value={selectedPreferences.smokingPreference}
                onChange={handlePreferenceChange}
              >
                <option value="">No Preference</option>
                {preferenceTypes.smokingPreferences.map((pref) => (
                  <option key={pref} value={pref}>
                    {pref.replace(/_/g, ' ')}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="additionalNotes">Additional Notes</label>
            <textarea
              id="additionalNotes"
              name="additionalNotes"
              value={selectedPreferences.additionalNotes}
              onChange={handlePreferenceChange}
              placeholder="Any additional preferences or requirements..."
              rows="3"
              maxLength="500"
            />
            <small style={{ color: '#6b7280', fontSize: '12px' }}>
              {selectedPreferences.additionalNotes.length}/500 characters
            </small>
          </div>
        </section>

        {/* Photos */}
        <section className="form-section">
          <h2>Photos</h2>
          <p className="section-hint">Upload photos of your property (max 10MB per image)</p>

          <div
            className={`photo-upload-area ${dragActive ? 'drag-active' : ''}`}
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
          >
            <input
              type="file"
              id="photoInput"
              multiple
              accept="image/*"
              onChange={handlePhotoChange}
              style={{ display: 'none' }}
            />
            <label htmlFor="photoInput" className="photo-upload-label">
              <div className="upload-icon">📷</div>
              <p>Drag and drop photos here or click to browse</p>
            </label>
          </div>

          {photoPreviews.length > 0 && (
            <div className="photo-previews">
              {photoPreviews.map((preview, index) => (
                <div key={index} className="photo-preview">
                  <img src={preview} alt={`Preview ${index + 1}`} />
                  <button
                    type="button"
                    className="remove-photo-btn"
                    onClick={() => removePhoto(index)}
                  >
                    ×
                  </button>
                  {index === 0 && <span className="primary-badge">Primary</span>}
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Contact Information */}
        <section className="form-section">
          <h2>Contact Information</h2>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="contactEmail">Contact Email *</label>
              <input
                type="email"
                id="contactEmail"
                name="contactEmail"
                value={formData.contactEmail}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="contactPhone">Contact Phone</label>
              <input
                type="tel"
                id="contactPhone"
                name="contactPhone"
                value={formData.contactPhone}
                onChange={handleInputChange}
                placeholder="e.g., 7165550123"
              />
            </div>
          </div>
        </section>

        {/* Submit Button */}
        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="btn btn-secondary"
            disabled={loading}
          >
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating Listing...' : 'Create Listing'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateListing;
