# Local Service Provider Web Application

## Project Structure

- `backend`: Spring Boot API with JWT, role-based auth, JPA, MySQL
- `frontend`: React (Vite) app with role-based routing and Axios integration

## Backend Setup

```bash
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

### Database (already configured)

`backend/src/main/resources/application.properties` includes:

- environment-variable based datasource settings (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`)
- `spring.jpa.hibernate.ddl-auto=update`

Use `backend/.env.example` as a reference and set variables in your terminal or deployment environment.

### Default Admin User

- Email: `admin@local.com`
- Password: `admin123`

## Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

The frontend API base URL is controlled by `VITE_API_BASE_URL` (see `frontend/.env.example`).

## Authentication Endpoints

- `POST /auth/register`
- `POST /auth/login`

Login response returns:

- `token`
- `role`

## Main Functional APIs

### USER

- `GET /services`
- `POST /bookings`
- `GET /bookings/me`

### PROVIDER

- `POST /provider/services`
- `GET /provider/bookings`

### ADMIN

- `GET /admin/users`
- `DELETE /admin/users/{id}`
- `GET /admin/services`
- `DELETE /admin/services/{id}`
- `GET /admin/bookings`

## Notes

- JWT is stateless and required for all non-`/auth/**` routes.
- JWT filter skips `/auth/**`.
- Passwords are hashed with BCrypt.
- Database tables are auto-created from JPA entities.