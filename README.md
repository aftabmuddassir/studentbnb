# StudentBnB

A full-stack platform for student accommodation rentals, connecting students with landlords for short-term and long-term housing solutions.

## Architecture

StudentBnB is built using a modern microservices architecture:

```
┌─────────────────────────────────────────────────────────┐
│                     Frontend (React)                     │
│                    Port 5173/5175                        │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ REST API
                       │
┌──────────────────────┴──────────────────────────────────┐
│                  Backend Services                        │
├──────────────────────────────────────────────────────────┤
│  Auth Service    │  User Service   │  Listing Service   │
│  (Port 8081)     │  (Port 8083)    │  (Port 8082)       │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
┌───────▼────────┐           ┌────────▼────────┐
│   PostgreSQL   │           │      Redis      │
│   (Port 5432)  │           │   (Port 6379)   │
└────────────────┘           └─────────────────┘
```

##  Features

### Authentication & Authorization
-  Email/Password registration and login
-  Google OAuth 2.0 integration
-  JWT-based authentication
-  Role-based access control (Student/Landlord)
-  Secure password hashing with BCrypt
-  Token refresh mechanism

### User Management
-  User profile management
-  Profile picture upload
-  Email verification
-  Password reset

### Listings (Coming Soon)
-  Create and manage property listings
-  Search and filter accommodations
-  Booking system
-  Reviews and ratings

### Frontend
-  Responsive React UI with Vite
-  Protected routes
-  User dashboard
-  Google OAuth integration
-  Modern authentication pages

##  Tech Stack

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **Routing**: React Router DOM
- **HTTP Client**: Axios
- **OAuth**: @react-oauth/google
- **Styling**: CSS3

### Backend
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Authentication**: JWT, Google OAuth 2.0
- **Build Tool**: Maven
- **API Library**: Google API Client 2.2.0

### DevOps
- **Containerization**: Docker & Docker Compose
- **Database Admin**: PgAdmin 4
- **Version Control**: Git & GitHub

##  Quick Start

### Prerequisites

- **Docker Desktop** (required)
- **Node.js 18+** and **npm** (for frontend development)
- **Java 17** and **Maven** (for backend development)
- **Git**

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/studentbnb.git
cd studentbnb
```

### 2. Setup Backend

#### Configure Environment Variables

```bash
cd backend
cp .env.example .env
```

Edit `backend/.env` with your credentials:

```env
# Generate a 256-bit JWT secret
JWT_SECRET=your-secure-256-bit-secret-key-here

# Database credentials
DATABASE_USERNAME=dev
DATABASE_PASSWORD=devpass123

# Google OAuth (optional, for OAuth features)
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

#### Start Backend Services

```bash
docker-compose up --build
```

Wait for all services to start (check with `docker-compose ps`).

### 3. Setup Frontend

```bash
cd ../frontend
cp .env.example .env
```

Edit `frontend/.env`:

```env
VITE_GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
VITE_API_URL=http://localhost:8081
```

#### Install Dependencies and Start

```bash
npm install
npm run dev
```

### 4. Access the Application

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8081
- **PgAdmin**: http://localhost:5050
- **Health Check**: http://localhost:8081/actuator/health

##  Documentation

Detailed documentation for each component:

- [Backend Documentation](./backend/README.md) - Microservices, API endpoints, deployment
- [Frontend Documentation](./frontend/README.md) - React app, components, setup

##  Google OAuth Setup

To enable Google Sign-In:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Google+ API**
4. Create **OAuth 2.0 Client ID** credentials
5. Add authorized origins:
   - `http://localhost:5173`
   - `http://localhost:5174`
   - `http://localhost:5175`
   - `http://localhost:3000`
6. Copy **Client ID** and **Client Secret**
7. Update both `backend/.env` and `frontend/.env`
8. Restart services

See detailed instructions in [Backend README](./backend/README.md#google-oauth-setup).

##  Testing

### Backend Tests

```bash
cd backend/auth-service
./mvnw test
```

### Frontend Tests

```bash
cd frontend
npm test
```

### Manual Testing with API

**Register a new user:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123",
    "role": "STUDENT"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123"
  }'
```

##  Project Structure

```
studentbnb/
├── backend/
│   ├── auth-service/          # Authentication microservice
│   ├── user-service/          # User management microservice
│   ├── listing-service/       # Listing management microservice
│   ├── docker-compose.yml     # Docker orchestration
│   ├── .env                   # Environment variables (gitignored)
│   ├── .env.example          # Environment template
│   └── README.md             # Backend documentation
├── frontend/
│   ├── src/
│   │   ├── components/       # Reusable components
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   └── App.jsx          # Main app component
│   ├── .env                  # Frontend env vars (gitignored)
│   ├── .env.example         # Frontend env template
│   └── README.md            # Frontend documentation
└── README.md                # This file
```

##  Security

### Best Practices Implemented

-  Environment variables for sensitive data
-  `.env` files in `.gitignore`
-  JWT token-based authentication
-  Password hashing with BCrypt
-  CORS configuration for frontend origins
-  Google OAuth token verification
-  Protected API endpoints
-  Input validation

### Security Checklist

- [ ] Enable HTTPS in production
- [ ] Implement rate limiting
- [ ] Add request validation middleware
- [ ] Enable SQL injection protection
- [ ] Implement CSRF protection
- [ ] Add security headers
- [ ] Set up logging and monitoring
- [ ] Regular dependency updates
- [ ] Security audit

##  Roadmap

### Phase 1: Core Authentication ( Completed)
- [x] User registration and login
- [x] JWT authentication
- [x] Google OAuth integration
- [x] Protected routes
- [x] User dashboard

### Phase 2: User Profiles ( In Progress)
- [ ] Complete user profile management
- [ ] Profile picture upload
- [ ] Email verification
- [ ] Password reset functionality

### Phase 3: Listings
- [ ] Create listing functionality
- [ ] Search and filter listings
- [ ] Listing details page
- [ ] Image upload for listings
- [ ] Favorites/Wishlist

### Phase 4: Booking System
- [ ] Booking requests
- [ ] Availability calendar
- [ ] Payment integration
- [ ] Booking management

### Phase 5: Reviews & Ratings
- [ ] Review system
- [ ] Rating system
- [ ] Review moderation

### Phase 6: Messaging
- [ ] Real-time chat
- [ ] Notification system
- [ ] Email notifications

### Phase 7: Admin Panel
- [ ] Admin dashboard
- [ ] User management
- [ ] Content moderation
- [ ] Analytics

##  Contributing

I'm open to contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: add some amazing feature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Commit Convention

Recommended: [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

##  Troubleshooting

### Common Issues

**Port already in use:**
```bash
# Find and kill process using port
# On Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# On Mac/Linux
lsof -ti:8081 | xargs kill -9
```

**Docker services won't start:**
```bash
# Reset Docker
docker-compose down -v
docker-compose up --build
```

**Frontend can't connect to backend:**
- Check CORS configuration in `SecurityConfig.java`
- Verify `VITE_API_URL` in `frontend/.env`
- Ensure backend is running on port 8081

**JWT token errors:**
- Verify `JWT_SECRET` is at least 64 characters
- Check token expiration time
- Ensure consistent secret across services

See [Backend Troubleshooting](./backend/README.md#troubleshooting) for more details.

##  API Documentation

### Authentication Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | Register new user |
| `/api/auth/login` | POST | Login with credentials |
| `/api/auth/google` | POST | OAuth with Google |
| `/api/auth/refresh` | POST | Refresh access token |
| `/api/auth/profile` | GET | Get user profile (protected) |

Full API documentation: [Backend README](./backend/README.md#service-endpoints)

## License

To be updated

## Team

- **Developer**: [Mohammed Aftab Muddassir ](mailto:m.aftab.muddassir07@gmail.com)


## Support

For questions and support:
-  Email: m.aftab.muddassir07@gmail.com

---
