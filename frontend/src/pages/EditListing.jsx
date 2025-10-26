import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getListingById,
  updateListing,
  uploadMultiplePhotos,
  addAmenities,
  getAmenityTypes,
  deletePhoto,
} from '../services/listingService';
import './CreateListing.css';

const EditListing = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [availableAmenities, setAvailableAmenities] = useState([]);

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

  const [existingPhotos, setExistingPhotos] = useState([]);
  const [selectedAmenities, setSelectedAmenities] = useState([]);
  const [newPhotos, setNewPhotos] = useState([]);
  const [newPhotoPreviews, setNewPhotoPreviews] = useState([]);
  const [dragActive, setDragActive] = useState(false);

  useEffect(() => {
    fetchListingData();
    fetchAmenities();
  }, [id]);

  const fetchListingData = async () => {
    try {
      setLoading(true);
      const listing = await getListingById(id);
      console.log('Fetched listing:', listing);

      // Populate form with listing data
      setFormData({
        title: listing.title || '',
        description: listing.description || '',
        address: listing.address || '',
        city: listing.city || '',
        state: listing.state || '',
        zipCode: listing.zipCode || '',
        rent: listing.rent || '',
        securityDeposit: listing.securityDeposit || '',
        bedrooms: listing.bedrooms || 1,
        bathrooms: listing.bathrooms || 1,
        squareFeet: listing.squareFeet || '',
        propertyType: listing.propertyType || 'APARTMENT',
        leaseType: listing.leaseType || 'SEMESTER',
        leaseDurationMonths: listing.leaseDurationMonths || 6,
        availableFrom: listing.availableFrom || '',
        availableUntil: listing.availableUntil || '',
        furnished: listing.furnished || false,
        utilitiesIncluded: listing.utilitiesIncluded || false,
        petsAllowed: listing.petsAllowed || false,
        smokingAllowed: listing.smokingAllowed || false,
        contactEmail: listing.contactEmail || '',
        contactPhone: listing.contactPhone || '',
        nearestUniversity: listing.nearestUniversity || '',
        distanceToCampusKm: listing.distanceToCampusKm || '',
      });

      setExistingPhotos(listing.photos || []);
      setSelectedAmenities(
        listing.amenities?.map((a) => a.amenityType) || []
      );
      setError('');
    } catch (err) {
      console.error('Error fetching listing:', err);
      setError('Failed to load listing data');
    } finally {
      setLoading(false);
    }
  };

  const fetchAmenities = async () => {
    try {
      const amenities = await getAmenityTypes();
      setAvailableAmenities(amenities);
    } catch (err) {
      console.error('Error fetching amenities:', err);
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

  const handleRemoveExistingPhoto = async (photoId) => {
    if (!window.confirm('Are you sure you want to delete this photo?')) {
      return;
    }

    try {
      await deletePhoto(photoId);
      setExistingPhotos(existingPhotos.filter((p) => p.id !== photoId));
    } catch (err) {
      console.error('Error deleting photo:', err);
      alert('Failed to delete photo');
    }
  };

  // New photo upload handlers
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

    setNewPhotos((prev) => [...prev, ...validFiles]);

    // Create previews
    validFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setNewPhotoPreviews((prev) => [...prev, reader.result]);
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

  const removeNewPhoto = (index) => {
    setNewPhotos((prev) => prev.filter((_, i) => i !== index));
    setNewPhotoPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      // Update listing
      await updateListing(id, {
        ...formData,
        rent: parseFloat(formData.rent),
        securityDeposit: parseFloat(formData.securityDeposit) || 0,
        bedrooms: parseInt(formData.bedrooms),
        bathrooms: parseInt(formData.bathrooms),
        squareFeet: parseFloat(formData.squareFeet) || null,
        leaseDurationMonths: parseInt(formData.leaseDurationMonths),
        distanceToCampusKm: parseFloat(formData.distanceToCampusKm) || null,
        currency: 'USD',
      });

      // Upload new photos if any
      if (newPhotos.length > 0) {
        await uploadMultiplePhotos(id, newPhotos);
      }

      // Update amenities
      if (selectedAmenities.length > 0) {
        await addAmenities(id, selectedAmenities);
      }

      setSuccess('Listing updated successfully!');
      setTimeout(() => {
        navigate('/my-listings');
      }, 2000);
    } catch (err) {
      console.error('Error updating listing:', err);
      const errorMessage =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        'Failed to update listing. Please try again.';
      setError(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  const propertyTypes = ['APARTMENT', 'HOUSE', 'CONDO', 'TOWNHOUSE', 'STUDIO', 'ROOM', 'SHARED_ROOM'];
  const leaseTypes = ['MONTHLY', 'SEMESTER', 'ACADEMIC_YEAR', 'YEARLY', 'SUMMER_ONLY', 'FLEXIBLE'];

  if (loading) {
    return (
      <div className="create-listing-container">
        <div className="loading">Loading listing data...</div>
      </div>
    );
  }

  return (
    <div className="create-listing-container">
      <div className="create-listing-header">
        <h1>Edit Listing</h1>
        <button onClick={() => navigate('/my-listings')} className="btn-back">
          Back to My Listings
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
              rows="5"
              required
              minLength="50"
              maxLength="2000"
            />
            <small
              style={{
                color: formData.description.length < 50 ? '#ef4444' : '#6b7280',
                fontSize: '12px',
              }}
            >
              {formData.description.length}/2000 characters{' '}
              {formData.description.length < 50 &&
                `(Need ${50 - formData.description.length} more)`}
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

        {/* Existing Photos */}
        {existingPhotos.length > 0 && (
          <section className="form-section">
            <h2>Current Photos</h2>
            <div className="photo-previews">
              {existingPhotos.map((photo, index) => (
                <div key={photo.id} className="photo-preview">
                  <img src={photo.photoUrl} alt={`Photo ${index + 1}`} />
                  <button
                    type="button"
                    className="remove-photo-btn"
                    onClick={() => handleRemoveExistingPhoto(photo.id)}
                  >
                    ×
                  </button>
                  {photo.isPrimary && <span className="primary-badge">Primary</span>}
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Add New Photos */}
        <section className="form-section">
          <h2>Add New Photos</h2>
          <p className="section-hint">Upload additional photos (max 10MB per image)</p>

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

          {newPhotoPreviews.length > 0 && (
            <div className="photo-previews">
              {newPhotoPreviews.map((preview, index) => (
                <div key={index} className="photo-preview">
                  <img src={preview} alt={`New Photo ${index + 1}`} />
                  <button
                    type="button"
                    className="remove-photo-btn"
                    onClick={() => removeNewPhoto(index)}
                  >
                    ×
                  </button>
                  <span className="new-badge">New</span>
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
              />
            </div>
          </div>
        </section>

        {/* Submit Button */}
        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/my-listings')}
            className="btn btn-secondary"
            disabled={submitting}
          >
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Updating Listing...' : 'Update Listing'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default EditListing;
