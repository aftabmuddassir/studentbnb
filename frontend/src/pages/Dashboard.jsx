import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './Dashboard.css';

function Dashboard() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (!currentUser) {
      navigate('/login');
    } else {
      setUser(currentUser);
    }
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const handleViewProfile = () => {
    navigate('/profile');
  };

  if (!user) {
    return <div>Loading...</div>;
  }

  return (
    <div className="dashboard-container">
      <nav className="dashboard-nav">
        <h1>StudentBnB</h1>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button onClick={handleViewProfile} className="btn-profile">
            Profile
          </button>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </nav>

      <div className="dashboard-content">
        <div className="welcome-card">
          <h2>Welcome to StudentBnB! 🎉</h2>
          <div className="user-info">
            <p>
              <strong>Email:</strong> {user.email}
            </p>
            <p>
              <strong>Role:</strong> {user.role}
            </p>
            <p>
              <strong>User ID:</strong> {user.userId}
            </p>
          </div>
          <p className="success-message">You have successfully logged in!</p>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
