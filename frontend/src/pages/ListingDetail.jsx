import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getListingById, getListingPreferences } from '../services/listingService';
import './ListingDetail.css';

const ListingDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [listing, setListing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedPhoto, setSelectedPhoto] = useState(0);
  const [showLightbox, setShowLightbox] = useState(false);
  const [preferences, setPreferences] = useState([]);

  const fetchListing = async () => {
    try {
      setLoading(true);
      const data = await getListingById(id);
      setListing(data);

      // Fetch preferences
      try {
        const prefs = await getListingPreferences(id);
        setPreferences(prefs);
      } catch (prefErr) {
        console.log('No preferences found for this listing');
        setPreferences([]);
      }

      setError('');
    } catch (err) {
      console.error('Error fetching listing:', err);
      setError('Failed to load listing details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchListing();
  }, [id]);

  const photos = listing?.photos || [];
  const amenities = listing?.amenities || [];

  const handlePrevPhoto = () => {
    setSelectedPhoto((prev) => (prev === 0 ? photos.length - 1 : prev - 1));
  };

  const handleNextPhoto = () => {
    setSelectedPhoto((prev) => (prev === photos.length - 1 ? 0 : prev + 1));
  };

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (showLightbox) {
        if (e.key === 'Escape') setShowLightbox(false);
        if (e.key === 'ArrowLeft') handlePrevPhoto();
        if (e.key === 'ArrowRight') handleNextPhoto();
      }
    };

    if (showLightbox) {
      window.addEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'auto';
    };
  }, [showLightbox, selectedPhoto, photos.length]);

  if (loading) {
    return (
      <div className="listing-detail-container">
        <div className="loading">Loading listing details...</div>
      </div>
    );
  }

  if (error || !listing) {
    return (
      <div className="listing-detail-container">
        <div className="error-state">
          <h2>Listing Not Found</h2>
          <p>{error || 'The listing you are looking for does not exist.'}</p>
          <button onClick={() => navigate('/dashboard')} className="btn btn-primary">
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="listing-detail-container">
      <div className="listing-detail-header">
        <button onClick={() => navigate(-1)} className="btn-back">
          ← Back
        </button>
        <span className={`status-badge status-${listing.status.toLowerCase()}`}>
          {listing.status}
        </span>
      </div>

      {/* Photo Gallery */}
      {photos.length > 0 && (
        <div className="photo-gallery">
          <div className="main-photo" onClick={() => setShowLightbox(true)}>
            <img
              src={photos[selectedPhoto]?.photoUrl || photos[0]?.photoUrl}
              alt={listing.title}
            />
            <div className="zoom-hint">🔍 Click to view full size</div>
          </div>
          {photos.length > 1 && (
            <div className="photo-thumbnails">
              {photos.map((photo, index) => (
                <div
                  key={photo.id}
                  className={`thumbnail ${index === selectedPhoto ? 'active' : ''}`}
                  onClick={() => setSelectedPhoto(index)}
                >
                  <img src={photo.photoUrl} alt={`Thumbnail ${index + 1}`} />
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Lightbox for full-size images */}
      {showLightbox && photos.length > 0 && (
        <div className="lightbox" onClick={() => setShowLightbox(false)}>
          <button className="lightbox-close" onClick={() => setShowLightbox(false)}>
            ×
          </button>
          <button className="lightbox-prev" onClick={(e) => { e.stopPropagation(); handlePrevPhoto(); }}>
            ‹
          </button>
          <button className="lightbox-next" onClick={(e) => { e.stopPropagation(); handleNextPhoto(); }}>
            ›
          </button>
          <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
            <img
              src={photos[selectedPhoto]?.photoUrl}
              alt={listing.title}
            />
            <div className="lightbox-caption">
              {selectedPhoto + 1} / {photos.length}
            </div>
          </div>
        </div>
      )}

      {/* Listing Info */}
      <div className="listing-info">
        <div className="listing-header-info">
          <h1>{listing.title}</h1>
          <div className="price">${listing.rent}/month</div>
        </div>

        <div className="listing-location">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 0C5.243 0 3 2.243 3 5c0 4.5 5 11 5 11s5-6.5 5-11c0-2.757-2.243-5-5-5zm0 7.5a2.5 2.5 0 110-5 2.5 2.5 0 010 5z"/>
          </svg>
          <span>{listing.address}, {listing.city}, {listing.state} {listing.zipCode}</span>
        </div>

        {/* Quick Stats */}
        <div className="quick-stats">
          <div className="stat-item">
            <div className="stat-label">Bedrooms</div>
            <div className="stat-value">{listing.bedrooms}</div>
          </div>
          <div className="stat-item">
            <div className="stat-label">Bathrooms</div>
            <div className="stat-value">{listing.bathrooms}</div>
          </div>
          <div className="stat-item">
            <div className="stat-label">Property Type</div>
            <div className="stat-value">{listing.propertyType.replace(/_/g, ' ')}</div>
          </div>
          {listing.squareFeet && (
            <div className="stat-item">
              <div className="stat-label">Square Feet</div>
              <div className="stat-value">{listing.squareFeet}</div>
            </div>
          )}
        </div>

        {/* Description */}
        <div className="section">
          <h2>Description</h2>
          <p>{listing.description}</p>
        </div>

        {/* Property Details */}
        <div className="section">
          <h2>Property Details</h2>
          <div className="details-grid">
            <div className="detail-item">
              <span className="detail-label">Lease Type:</span>
              <span className="detail-value">{listing.leaseType.replace(/_/g, ' ')}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Lease Duration:</span>
              <span className="detail-value">{listing.leaseDurationMonths} months</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Security Deposit:</span>
              <span className="detail-value">${listing.securityDeposit}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Utilities Included:</span>
              <span className="detail-value">{listing.utilitiesIncluded ? 'Yes' : 'No'}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Furnished:</span>
              <span className="detail-value">{listing.furnished ? 'Yes' : 'No'}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Pets Allowed:</span>
              <span className="detail-value">{listing.petsAllowed ? 'Yes' : 'No'}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Smoking Allowed:</span>
              <span className="detail-value">{listing.smokingAllowed ? 'Yes' : 'No'}</span>
            </div>
            {listing.nearestUniversity && (
              <div className="detail-item">
                <span className="detail-label">Nearest University:</span>
                <span className="detail-value">{listing.nearestUniversity}</span>
              </div>
            )}
            {listing.distanceToCampusKm && (
              <div className="detail-item">
                <span className="detail-label">Distance to Campus:</span>
                <span className="detail-value">{listing.distanceToCampusKm} km</span>
              </div>
            )}
          </div>
        </div>

        {/* Availability */}
        {(listing.availableFrom || listing.availableUntil) && (
          <div className="section">
            <h2>Availability</h2>
            <div className="details-grid">
              {listing.availableFrom && (
                <div className="detail-item">
                  <span className="detail-label">Available From:</span>
                  <span className="detail-value">{listing.availableFrom}</span>
                </div>
              )}
              {listing.availableUntil && (
                <div className="detail-item">
                  <span className="detail-label">Available Until:</span>
                  <span className="detail-value">{listing.availableUntil}</span>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Amenities */}
        {amenities.length > 0 && (
          <div className="section">
            <h2>Amenities</h2>
            <div className="amenities-list">
              {amenities.map((amenity, index) => (
                <div key={index} className="amenity-tag">
                  ✓ {amenity.amenityType.replace(/_/g, ' ')}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Roommate/Tenant Preferences */}
        {preferences.length > 0 && (
          <div className="section">
            <h2>Roommate/Tenant Preferences</h2>
            <div className="details-grid">
              {preferences.map((pref, index) => (
                <div key={index}>
                  {pref.dietaryPreference && (
                    <div className="detail-item">
                      <span className="detail-label">Dietary Preference:</span>
                      <span className="detail-value">{pref.dietaryPreference.replace(/_/g, ' ')}</span>
                    </div>
                  )}
                  {pref.genderPreference && (
                    <div className="detail-item">
                      <span className="detail-label">Gender Preference:</span>
                      <span className="detail-value">{pref.genderPreference.replace(/_/g, ' ')}</span>
                    </div>
                  )}
                  {pref.smokingPreference && (
                    <div className="detail-item">
                      <span className="detail-label">Smoking Preference:</span>
                      <span className="detail-value">{pref.smokingPreference.replace(/_/g, ' ')}</span>
                    </div>
                  )}
                  {pref.additionalNotes && (
                    <div className="detail-item" style={{ gridColumn: '1 / -1' }}>
                      <span className="detail-label">Additional Notes:</span>
                      <span className="detail-value">{pref.additionalNotes}</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Contact Information */}
        <div className="section contact-section">
          <h2>Contact Information</h2>
          <div className="contact-info">
            <div className="contact-item">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
                <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
              </svg>
              <a href={`mailto:${listing.contactEmail}`}>{listing.contactEmail}</a>
            </div>
            {listing.contactPhone && (
              <div className="contact-item">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/>
                </svg>
                <a href={`tel:${listing.contactPhone}`}>{listing.contactPhone}</a>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ListingDetail;
