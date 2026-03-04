# Deployment Guide

## Architecture

- Backend: Spring Boot service deployed on Render.
- Frontend: React (Vite) app deployed on Vercel.
- Database: MySQL (Aiven) connected through backend environment variables.

## 1) Deploy Backend on Render

1. Open Render and create a new **Web Service** from this GitHub repository.
2. Configure:
   - Root Directory: `backend`
   - Runtime: `Docker` (already supported by `render.yaml`)
3. Add these environment variables in Render:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `APP_JWT_SECRET` (use a strong random value)
   - `APP_JWT_EXPIRATION_MS` (optional, default `86400000`)
4. Deploy and wait until service is healthy.

## 2) Deploy Frontend on Vercel

1. Import the same GitHub repository into Vercel.
2. Set project root to `frontend`.
3. Add environment variable:
   - `VITE_API_BASE_URL` = your Render backend URL (example: `https://your-backend.onrender.com`)
4. Deploy.

## 3) CORS Note

If frontend requests are blocked in production, allow your Vercel domain in backend CORS settings.

## 4) Post-Deployment Smoke Check

1. Open frontend URL.
2. Register a new user and login.
3. Login as admin and verify:
   - User management loads
   - Booking management loads
   - Service management loads
4. Login as provider and verify:
   - Add service works
   - Profile update works
   - Password change works

## 5) Security Checklist

- Never commit real credentials in source files.
- Keep all secrets in Render/Vercel environment variables.
- Rotate `APP_JWT_SECRET` if leaked.
