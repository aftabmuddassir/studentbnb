# StudentBnB Frontend

React frontend for StudentBnB with Google OAuth integration.

## Setup Instructions

### 1. Install Dependencies
```bash
npm install
```

### 2. Get Google OAuth Client ID

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API:
   - Go to "APIs & Services" > "Library"
   - Search for "Google+ API"
   - Click "Enable"
4. Create OAuth 2.0 credentials:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth client ID"
   - Select "Web application"
   - Add authorized JavaScript origins:
     - `http://localhost:5173`
     - `http://localhost:3000`
   - Add authorized redirect URIs:
     - `http://localhost:5173`
     - `http://localhost:3000`
   - Click "Create"
   - Copy the Client ID

### 3. Configure Environment Variables

Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

Then update `.env` with your Google Client ID:

```env
VITE_GOOGLE_CLIENT_ID=your-actual-client-id.apps.googleusercontent.com
VITE_API_URL=http://localhost:8081
```

**IMPORTANT**: Never commit your `.env` file to git. It's already in `.gitignore`.

### 4. Run the Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:5173`

## Features

- ✅ User Registration
- ✅ User Login
- ✅ Google OAuth Login (requires setup)
- ✅ Dashboard
- ✅ JWT Token Management
- ✅ Protected Routes

## API Endpoints Used

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/google` - Google OAuth login (to be implemented in backend)

## Tech Stack

- React 18
- Vite
- React Router DOM
- Axios
- @react-oauth/google

## Project Structure

```
src/
├── components/       # Reusable components
├── pages/           # Page components
│   ├── Login.jsx
│   ├── Register.jsx
│   ├── Dashboard.jsx
│   └── Auth.css
├── services/        # API services
│   └── authService.js
├── utils/           # Utility functions
├── App.jsx          # Main app component
└── main.jsx         # Entry point
```

## Next Steps

To complete Google OAuth integration:

1. Get your Google Client ID (see instructions above)
2. Implement the backend Google OAuth endpoint at `/api/auth/google`
3. Update the `handleGoogleSuccess` function in Login.jsx and Register.jsx to call the backend

## Testing

Test users created via Postman will work with the login form!

Example:
- Email: `adnan@buffalo.edu`
- Password: `adnan123`
