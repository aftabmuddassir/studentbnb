# StudentBnB Backend

Microservices-based backend for StudentBnB platform built with Spring Boot.

## Architecture

This backend follows a microservices architecture with the following services:

- **Auth Service** (Port 8081) - Authentication and authorization
- **User Service** (Port 8083) - User profile management
- **Listing Service** (Port 8082) - Property listings management
- **PostgreSQL** (Port 5432) - Shared database
- **Redis** (Port 6379) - Caching layer
- **PgAdmin** (Port 5050) - Database administration

## Tech Stack

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Authentication**: JWT (JSON Web Tokens)
- **OAuth**: Google OAuth 2.0
- **Build Tool**: Maven
- **Container**: Docker & Docker Compose

## Prerequisites

- Docker Desktop installed and running
- Java 17 (for local development)
- Maven 3.6+ (for local development)
- Google OAuth 2.0 credentials (for OAuth features)

## Quick Start with Docker

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd studentbnb/backend
```

### 2. Configure Environment Variables

Copy the example env file and configure it:

```bash
cp .env.example .env
```

Edit `.env` with your credentials:

```env
# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-here

# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/studentbnb
DATABASE_USERNAME=dev
DATABASE_PASSWORD=devpass123

# PgAdmin Configuration
PGADMIN_DEFAULT_EMAIL=admin@studentbnb.com
PGADMIN_DEFAULT_PASSWORD=studentbnb

# Google OAuth Configuration (Optional)
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Service Ports
AUTH_SERVICE_PORT=8081
USER_SERVICE_PORT=8083
LISTING_SERVICE_PORT=8082

# Environment
SPRING_PROFILES_ACTIVE=docker
```

**IMPORTANT**:
- Never commit your `.env` file to git
- Generate a secure JWT_SECRET (at least 256 bits / 64 characters)
- Get Google OAuth credentials from [Google Cloud Console](https://console.cloud.google.com/apis/credentials)

### 3. Create `.env.example` Template

```bash
# Copy your .env to create an example file (remove sensitive values)
cp .env .env.example
# Then manually edit .env.example to replace real values with placeholders
```

### 4. Start All Services

```bash
docker-compose up --build
```

This will start all services. Wait for all services to be healthy.

### 5. Verify Services

Check if all services are running:

```bash
docker-compose ps
```

You should see:
-  studentbnb-postgres (healthy)
-  studentbnb-redis (healthy)
-  studentbnb-auth-service (healthy)
-  studentbnb-user-service (healthy)
-  studentbnb-listing-service (healthy)
-  studentbnb-pgadmin (healthy)

## Service Endpoints

### Auth Service (Port 8081)

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/register` | POST | No | Register new user |
| `/api/auth/login` | POST | No | Login with email/password |
| `/api/auth/google` | POST | No | Login/Register with Google OAuth |
| `/api/auth/refresh` | POST | No | Refresh JWT access token |
| `/api/auth/profile` | GET | Yes | Get authenticated user profile |
| `/actuator/health` | GET | No | Health check |

### User Service (Port 8083)

*To be documented*

### Listing Service (Port 8082)

*To be documented*

## API Examples

### Register a New User

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123",
    "role": "STUDENT"
  }'
```

**Response:**
```json
{
  "message": "User registered successfully",
  "userId": 1,
  "email": "student@example.com",
  "role": "STUDENT"
}
```

### Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "message": "Login successful",
  "userId": 1,
  "email": "student@example.com",
  "role": "STUDENT",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Get Profile (Protected)

```bash
curl -X GET http://localhost:8081/api/auth/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Google OAuth Login

```bash
curl -X POST http://localhost:8081/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "credential": "GOOGLE_ID_TOKEN"
  }'
```

## Database Access

### Using PgAdmin

1. Open http://localhost:5050
2. Login with credentials from `.env`:
   - Email: `admin@studentbnb.com`
   - Password: `studentbnb`
3. Add server:
   - Host: `postgres`
   - Port: `5432`
   - Database: `studentbnb`
   - Username: `dev`
   - Password: `devpass123`

### Using psql

```bash
docker exec -it studentbnb-postgres psql -U dev -d studentbnb
```

## Development

### Running Services Individually

If you want to run a service locally (without Docker):

```bash
cd auth-service
./mvnw spring-boot:run
```

Make sure to update the database connection in `application.yml` to point to `localhost` instead of `postgres`.

### Rebuilding a Single Service

```bash
docker-compose build auth-service
docker-compose up -d --force-recreate auth-service
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service

# Last 100 lines
docker-compose logs --tail=100 auth-service
```

### Stop Services

```bash
# Stop all services
docker-compose stop

# Stop specific service
docker-compose stop auth-service

# Stop and remove containers
docker-compose down

# Stop, remove containers, and volumes (WARNING: deletes data)
docker-compose down -v
```

## Configuration Files

Each service has its own configuration:

- `auth-service/src/main/resources/application.yml`
- `user-service/src/main/resources/application.yml`
- `listing-service/src/main/resources/application.yml`

Environment-specific profiles:
- `dev` - Local development
- `docker` - Docker container deployment
- `test` - Testing (uses H2 in-memory database)
- `prod` - Production deployment

## Google OAuth Setup

### 1. Create Google OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Navigate to **APIs & Services** > **Credentials**
5. Click **Create Credentials** > **OAuth client ID**
6. Select **Web application**
7. Add **Authorized JavaScript origins**:
   - `http://localhost:5173`
   - `http://localhost:3000`
   - Add any other frontend URLs
8. Add **Authorized redirect URIs** (same as origins for client-side OAuth)
9. Click **Create** and copy the Client ID and Client Secret

### 2. Update Environment Variables

Add to your `.env` file:

```env
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
```

### 3. Restart Auth Service

```bash
docker-compose restart auth-service
```

## Testing

### Run Tests for a Service

```bash
cd auth-service
./mvnw test
```

### Test with Postman

Import the provided Postman collection (if available) or use the API examples above.

## Troubleshooting

### Service Won't Start

**Check logs:**
```bash
docker-compose logs auth-service
```

**Common issues:**
- JWT_SECRET too short (must be at least 256 bits)
- Database connection failed (check PostgreSQL is running)
- Port already in use (stop other services using the port)

### Database Connection Error

**Verify PostgreSQL is running:**
```bash
docker-compose ps postgres
```

**Check database credentials in `.env`**

### JWT Token Errors

**Error: "The specified key byte array is X bits which is not secure enough"**

Solution: Use a JWT_SECRET with at least 256 bits (64 characters):

```bash
# Generate a secure secret
openssl rand -hex 32
```

### OAuth Errors

**Error: "origin_mismatch"**

Solution: Add your frontend origin to Google Cloud Console authorized origins.

**Error: "Invalid Google ID token"**

Solution: Verify GOOGLE_CLIENT_ID matches the one in Google Cloud Console.

## Project Structure

```
backend/
├── auth-service/          # Authentication service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/studentbnb/auth/
│   │   │   │   ├── config/       # Security, CORS configs
│   │   │   │   ├── controller/   # REST controllers
│   │   │   │   ├── dto/          # Data transfer objects
│   │   │   │   ├── entity/       # JPA entities
│   │   │   │   ├── repository/   # Database repositories
│   │   │   │   ├── service/      # Business logic
│   │   │   │   └── util/         # Utilities
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── user-service/          # User management service
├── listing-service/       # Listing management service
├── docker-compose.yml     # Docker orchestration
├── .env                   # Environment variables (gitignored)
├── .env.example           # Environment template
└── README.md             # This file
```

## Security Best Practices

1.  Never commit `.env` files
2.  Use strong JWT secrets (256+ bits)
3.  Rotate secrets regularly
4.  Use HTTPS in production
5.  Enable CORS only for trusted origins
6.  Keep dependencies updated
7.  Use environment-specific configurations
8.  Implement rate limiting (TODO)
9.  Add request validation
10. Log security events

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Update documentation
5. Submit a pull request

## License



## Support

For issues and questions:
- Create an issue on GitHub
- Contact: [m.aftab.muddassir07@gmail.com]
