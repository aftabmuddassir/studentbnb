import axios from 'axios';

const API_URL = `${import.meta.env.VITE_API_URL}/api/auth`;

const authService = {
  register: async (userData) => {
    const response = await axios.post(`${API_URL}/register`, userData);
    if (response.data.accessToken) {
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  login: async (email, password) => {
    const response = await axios.post(`${API_URL}/login`, {
      email,
      password,
    });
    if (response.data.accessToken) {
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  googleLogin: async (credential) => {
    const response = await axios.post(`${API_URL}/google`, {
      credential,
    });
    if (response.data.accessToken) {
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    return JSON.parse(localStorage.getItem('user'));
  },

  getAuthHeader: () => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user && user.accessToken) {
      return { Authorization: 'Bearer ' + user.accessToken };
    }
    return {};
  },
};

export default authService;
