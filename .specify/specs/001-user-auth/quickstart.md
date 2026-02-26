# Quickstart: User Authentication System

**Feature**: 001-user-auth  
**Date**: 2026-02-25  
**Time to Complete**: ~20 minutes

## Purpose

This guide helps developers set up and run the user authentication system locally for development and testing. Follow these steps to have a working authentication API and frontend in under 20 minutes.

---

## Prerequisites

Install the following tools before proceeding:

### Required
- **Java 21** (JDK): [Download from Oracle](https://www.oracle.com/java/technologies/downloads/#java21) or use SDKMAN: `sdk install java 21-open`
- **Maven 3.9+**: [Download Maven](https://maven.apache.org/download.cgi) or use package manager
- **Node.js 20+** and **npm 10+**: [Download Node.js](https://nodejs.org/)
- **PostgreSQL 15+**: [Download PostgreSQL](https://www.postgresql.org/download/)
- **Git**: For cloning the repository

### Optional (Recommended)
- **Docker** & **Docker Compose**: For running PostgreSQL in a container
- **Postman** or **cURL**: For API testing
- **IntelliJ IDEA** or **VS Code**: For development

### Verify Installations
```bash
java --version        # Should show Java 21+
mvn --version         # Should show Maven 3.9+
node --version        # Should show v20+
npm --version         # Should show v10+
psql --version        # Should show PostgreSQL 15+
docker --version      # (Optional) Should show Docker 20+
```

---

## Step 1: Clone Repository and Navigate to Feature Branch

```bash
# Clone the repository
git clone <repository-url>
cd InnovatEPAM

# Checkout the authentication feature branch
git checkout 001-user-auth

# Verify you're on the correct branch
git branch --show-current  # Should output: 001-user-auth
```

---

## Step 2: Set Up PostgreSQL Database

### Option A: Using Docker (Recommended)

Create a `docker-compose.yml` file in the repository root:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    container_name: innovatepam-postgres
    environment:
      POSTGRES_DB: innovatepam
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

Start PostgreSQL:
```bash
docker-compose up -d
```

Verify PostgreSQL is running:
```bash
docker ps  # Should show innovatepam-postgres container
```

### Option B: Using Local PostgreSQL Installation

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database and user
CREATE DATABASE innovatepam;
CREATE USER admin WITH PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE innovatepam TO admin;
\q
```

---

## Step 3: Configure Backend Environment Variables

Create a `.env` file in the `backend/` directory:

```bash
cd backend
cat > .env << 'EOF'
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/innovatepam
DB_USERNAME=admin
DB_PASSWORD=admin123

# JWT Configuration
JWT_SECRET_KEY=YourSuperSecretKeyHereMustBeAtLeast256BitsLong123456789012345678901234567890

# Server Configuration
SERVER_PORT=8080

# Logging Level (optional)
LOGGING_LEVEL=INFO
EOF
```

**Security Note**: Never commit `.env` to version control. Ensure `.gitignore` includes `.env`.

---

## Step 4: Run Backend (Spring Boot API)

### Install Dependencies and Run Flyway Migrations

```bash
# From backend/ directory
mvn clean install

# Flyway will run migrations automatically on application startup
```

### Start the Backend Server

```bash
mvn spring-boot:run
```

**Expected Output:**
```
...
INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
INFO 12345 --- [main] c.i.a.AuthApplication                    : Started AuthApplication in 5.123 seconds
```

### Verify Backend is Running

Open a new terminal and test the health endpoint:
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

Access Swagger UI for interactive API testing:
```
http://localhost:8080/swagger-ui.html
```

---

## Step 5: Configure Frontend Environment Variables

Create a `.env` file in the `frontend/` directory:

```bash
cd ../frontend
cat > .env << 'EOF'
VITE_API_BASE_URL=http://localhost:8080/api/v1
EOF
```

---

## Step 6: Run Frontend (React + Vite)

### Install Dependencies

```bash
# From frontend/ directory
npm install
```

### Start the Development Server

```bash
npm run dev
```

**Expected Output:**
```
VITE v5.0.0  ready in 1234 ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
➜  press h to show help
```

### Access the Frontend

Open your browser and navigate to:
```
http://localhost:5173
```

You should see the InnovatEPAM Portal homepage with links to Register and Login pages.

---

## Step 7: Test the Authentication Flow

### Test User Registration

**Via Frontend:**
1. Navigate to `http://localhost:5173/register`
2. Fill in the form:
   - Email: `test@example.com`
   - Password: `SecurePass123`
   - Role: Select "Submitter"
3. Click "Register"
4. You should be redirected to the dashboard with a success message

**Via API (cURL):**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "api-test@example.com",
    "password": "ApiPass123",
    "role": "submitter"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "api-test@example.com",
  "role": "submitter",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 86400
}
```

### Test User Login

**Via Frontend:**
1. Navigate to `http://localhost:5173/login`
2. Enter credentials:
   - Email: `test@example.com`
   - Password: `SecurePass123`
3. Click "Login"
4. You should be redirected to the dashboard

**Via API (cURL):**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "api-test@example.com",
    "password": "ApiPass123"
  }'
```

### Test Protected Endpoint

Use the token from login/registration:
```bash
TOKEN="<paste-token-here>"

curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "api-test@example.com",
  "role": "submitter",
  "createdAt": "2026-02-25T10:30:00Z"
}
```

---

## Step 8: Run Tests

### Backend Tests

```bash
cd backend

# Run all tests
mvn test

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html  # macOS
# or
start target/site/jacoco/index.html  # Windows
```

**Expected Output:**
```
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Frontend Tests

```bash
cd frontend

# Run all tests
npm test

# Run with coverage
npm run test:coverage

# View coverage report
npm run test:ui  # Opens Vitest UI
```

**Expected Output:**
```
Test Files  12 passed (12)
     Tests  38 passed (38)
 Start at  10:30:00
 Duration  2.45s
```

---

## Troubleshooting

### Backend Issues

**Problem**: `Connection refused to PostgreSQL`
```
Solution: Ensure PostgreSQL is running on port 5432
docker ps  # Check if postgres container is running
docker-compose up -d  # Restart if needed
```

**Problem**: `JWT_SECRET_KEY not found`
```
Solution: Verify .env file exists in backend/ directory
cat backend/.env  # Should show JWT_SECRET_KEY
```

**Problem**: `Table 'users' doesn't exist`
```
Solution: Flyway migrations failed. Check database connection and re-run
mvn flyway:migrate  # Manually run migrations
mvn spring-boot:run  # Restart backend
```

### Frontend Issues

**Problem**: `Network Error when calling API`
```
Solution: Verify backend is running and VITE_API_BASE_URL is correct
curl http://localhost:8080/actuator/health  # Test backend
cat frontend/.env  # Verify API URL
```

**Problem**: `Module not found errors`
```
Solution: Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Database Issues

**Problem**: `Port 5432 already in use`
```
Solution: Another PostgreSQL instance is running
lsof -i :5432  # Find process using port
kill -9 <PID>  # Kill process
docker-compose up -d  # Restart Docker container
```

---

## Default Credentials for Testing

After running the quickstart, you'll have these predefined roles:

| Role | Description |
|------|-------------|
| `submitter` | Users who submit applications/content |
| `evaluator/admin` | Users who review submissions and manage the platform |

**Create test accounts** using the registration flow above with either role.

---

## Next Steps

1. **Explore API Documentation**: Visit `http://localhost:8080/swagger-ui.html`
2. **Review Code Structure**: See [plan.md](plan.md) for project structure
3. **Understand Data Model**: See [data-model.md](data-model.md) for entities
4. **Read API Contracts**: See [contracts/README.md](contracts/README.md)
5. **Run Full Test Suite**: Add integration and E2E tests
6. **Implement Additional Features**: See [spec.md](spec.md) for full requirements

---

## Development Workflow

### Making Changes

1. **Backend Changes**:
   ```bash
   cd backend
   # Edit code in src/main/java/
   mvn spring-boot:run  # Hot reload enabled with spring-boot-devtools
   ```

2. **Frontend Changes**:
   ```bash
   cd frontend
   # Edit code in src/
   # Vite automatically hot-reloads changes
   ```

3. **Database Schema Changes**:
   ```bash
   cd backend/src/main/resources/db/migration
   # Create new migration: V4__description.sql
   mvn flyway:migrate  # Run new migration
   ```

### Running in Production Mode

**Backend:**
```bash
cd backend
mvn clean package
java -jar target/auth-service-1.0.0.jar
```

**Frontend:**
```bash
cd frontend
npm run build  # Creates production build in dist/
npm run preview  # Preview production build locally
```

---

## Useful Commands

```bash
# Check all running services
netstat -an | grep LISTEN | grep -E '5432|8080|5173'

# View backend logs
tail -f backend/logs/application.log

# Reset database (WARNING: Deletes all data)
docker-compose down -v
docker-compose up -d
cd backend && mvn flyway:clean flyway:migrate

# Format code
cd backend && mvn spotless:apply  # Format Java code
cd frontend && npm run format      # Format JavaScript/JSX code
```

---

## Documentation

- **Feature Specification**: [spec.md](spec.md) - Full requirements
- **Implementation Plan**: [plan.md](plan.md) - Technical approach
- **Research**: [research.md](research.md) - Technical decisions
- **Data Model**: [data-model.md](data-model.md) - Entity definitions
- **API Contracts**: [contracts/README.md](contracts/README.md) - API documentation

---

## Support

If you encounter issues not covered in this guide:
1. Check error logs (`backend/logs/application.log` or browser console)
2. Verify all prerequisites are installed correctly
3. Ensure environment variables are set correctly
4. Review the troubleshooting section above
5. Consult the research.md for technical decisions

---

**Status**: ✅ Quickstart guide complete  
**Estimated Setup Time**: 15-20 minutes  
**Last Updated**: 2026-02-25
