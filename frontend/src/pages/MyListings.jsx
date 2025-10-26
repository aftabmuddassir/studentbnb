import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyListings, deleteListing } from '../services/listingService';
import './MyListings.css';

const MyListings = () => {
  const navigate = useNavigate();
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMyListings();
  }, []);

  const fetchMyListings = async () => {
    try {
      setLoading(true);
      const response = await getMyListings();
      console.log('My listings response:', response);

      // Handle different response formats
      let listingsData = [];
      if (Array.isArray(response)) {
        listingsData = response;
      } else if (response && response.data && Array.isArray(response.data)) {
        listingsData = response.data;
      } else if (response && Array.isArray(response.listings)) {
        listingsData = response.listings;
      }

      setListings(listingsData);
      setError('');
    } catch (err) {
      console.error('Error fetching listings:', err);
      setError('Failed to load your listings');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (listingId) => {
    if (!window.confirm('Are you sure you want to delete this listing?')) {
      return;
    }

    try {
      await deleteListing(listingId);
      setListings(listings.filter(listing => listing.id !== listingId));
    } catch (err) {
      console.error('Error deleting listing:', err);
      alert('Failed to delete listing');
    }
  };

  const handleEdit = (listingId) => {
    navigate(`/edit-listing/${listingId}`);
  };

  const handleView = (listingId) => {
    navigate(`/listings/${listingId}`);
  };

  if (loading) {
    return (
      <div className="my-listings-container">
        <div className="loading">Loading your listings...</div>
      </div>
    );
  }

  return (
    <div className="my-listings-container">
      <div className="my-listings-header">
        <h1>My Listings</h1>
        <div className="header-actions">
          <button onClick={() => navigate('/create-listing')} className="btn btn-primary">
            Create New Listing
          </button>
          <button onClick={() => navigate('/dashboard')} className="btn btn-secondary">
            Back to Dashboard
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {listings.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📋</div>
          <h2>No Listings Yet</h2>
          <p>Create your first listing to get started!</p>
          <button onClick={() => navigate('/create-listing')} className="btn btn-primary">
            Create Listing
          </button>
        </div>
      ) : (
        <div className="listings-grid">
          {listings.map((listing) => (
            <div key={listing.id} className="listing-card">
              <div className="listing-image">
                {listing.photos && listing.photos.length > 0 ? (
                  <img src={listing.photos[0].photoUrl} alt={listing.title} />
                ) : (
                  <div className="no-image">No Image</div>
                )}
                <span className={`status-badge status-${listing.status.toLowerCase()}`}>
                  {listing.status}
                </span>
              </div>

              <div className="listing-content">
                <h3>{listing.title}</h3>
                <p className="listing-location">
                  {listing.city}, {listing.state}
                </p>
                <p className="listing-price">${listing.rent}/month</p>

                <div className="listing-details">
                  <span>{listing.bedrooms} bed</span>
                  <span>{listing.bathrooms} bath</span>
                  <span>{listing.propertyType.replace(/_/g, ' ')}</span>
                </div>

                <div className="listing-stats">
                  <span>Views: {listing.viewCount || 0}</span>
                  <span>Inquiries: {listing.inquiryCount || 0}</span>
                </div>

                <div className="listing-actions">
                  <button onClick={() => handleView(listing.id)} className="btn btn-view">
                    View
                  </button>
                  <button onClick={() => handleEdit(listing.id)} className="btn btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(listing.id)} className="btn btn-delete">
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyListings;
