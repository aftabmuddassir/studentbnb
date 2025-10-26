import axios from 'axios';

const API_BASE_URL = 'http://localhost:8082/api/listings';

// Get auth token from localStorage
const getAuthToken = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  return user?.accessToken || null;
};

// Create axios instance with auth
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use(
  (config) => {
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Listing Services

export const createListing = async (listingData) => {
  try {
    const response = await api.post('', listingData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const updateListing = async (listingId, listingData) => {
  try {
    const response = await api.put(`/${listingId}`, listingData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const deleteListing = async (listingId) => {
  try {
    const response = await api.delete(`/${listingId}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getMyListings = async () => {
  try {
    const response = await api.get('/my-listings');
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getAllListings = async (page = 0, size = 20) => {
  try {
    const response = await axios.get(`${API_BASE_URL}?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getListingById = async (listingId) => {
  try {
    const response = await api.get(`/${listingId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching listing by ID:', error);
    throw error.response?.data || error.message;
  }
};

export const searchListings = async (searchParams) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/search`, searchParams);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Photo Services

export const uploadPhoto = async (listingId, file, description = '', isPrimary = false) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('description', description);
    formData.append('isPrimary', isPrimary);

    const token = getAuthToken();
    const response = await axios.post(
      `${API_BASE_URL}/${listingId}/photos/upload`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const uploadMultiplePhotos = async (listingId, files) => {
  try {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const token = getAuthToken();
    const response = await axios.post(
      `${API_BASE_URL}/${listingId}/photos/upload-multiple`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getListingPhotos = async (listingId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/${listingId}/photos`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const deletePhoto = async (photoId) => {
  try {
    const token = getAuthToken();
    const response = await axios.delete(`${API_BASE_URL}/photos/${photoId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Amenity Services

export const getAmenityTypes = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/amenities/types`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const addAmenities = async (listingId, amenityTypes) => {
  try {
    const response = await api.post(`/${listingId}/amenities/bulk`, {
      amenityTypes,
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getListingAmenities = async (listingId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/${listingId}/amenities`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const deleteAmenity = async (listingId, amenityType) => {
  try {
    const response = await api.delete(`/${listingId}/amenities/${amenityType}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Preference Services

export const getPreferenceTypes = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/1/preferences/types`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const setPreferences = async (listingId, preferences) => {
  try {
    const response = await api.post(`/${listingId}/preferences`, preferences);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getListingPreferences = async (listingId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/${listingId}/preferences`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const deletePreferences = async (listingId) => {
  try {
    const response = await api.delete(`/${listingId}/preferences`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export default {
  createListing,
  updateListing,
  deleteListing,
  getMyListings,
  getAllListings,
  getListingById,
  searchListings,
  uploadPhoto,
  uploadMultiplePhotos,
  getListingPhotos,
  deletePhoto,
  getAmenityTypes,
  addAmenities,
  getListingAmenities,
  deleteAmenity,
  getPreferenceTypes,
  setPreferences,
  getListingPreferences,
  deletePreferences,
};
