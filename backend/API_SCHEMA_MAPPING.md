# Database Schema vs Backend Models Mapping

## ✅ USERS TABLE
**Database Columns:**
- id (int, auto_increment)
- name (varchar)
- email (varchar)
- phone (varchar)
- role (enum: customer, employee, manager)
- created_at, updated_at (timestamps)

**Backend Model:** `UserCreate`
- ✅ name: str
- ✅ email: str
- ✅ phone: str
- ✅ role: str

**Endpoints:**
- GET /api/users
- POST /api/users

---

## ✅ CARS TABLE
**Database Columns:**
- id (int, auto_increment)
- user_id (int)
- model (varchar)
- plate_number (varchar)
- color (varchar)
- year (int, nullable)
- created_at, updated_at (timestamps)

**Backend Model:** `CarCreate`
- ✅ user_id: int
- ✅ model: str
- ✅ plate_number: str
- ✅ color: str
- ✅ year: Optional[int]

**Endpoints:**
- GET /api/cars
- POST /api/cars

---

## ✅ SERVICES TABLE
**Database Columns:**
- id (int, auto_increment)
- name (varchar)
- description (text)
- price (decimal)
- duration (int) - in minutes
- is_active (tinyint)
- created_at, updated_at (timestamps)

**Endpoints:**
- GET /api/services (returns only active services)

---

## ✅ BOOKINGS TABLE
**Database Columns:**
- id (int, auto_increment)
- user_id (int)
- car_id (int)
- service_id (int)
- team_id (int, nullable)
- vehicle_id (int, nullable)
- location (varchar)
- location_lat (decimal, nullable)
- location_lng (decimal, nullable)
- scheduled_time (datetime)
- status (enum: pending, assigned, in_progress, completed, cancelled)
- total_price (decimal)
- notes (text, nullable)
- created_at, updated_at, completed_at (timestamps)

**Backend Model:** `BookingCreate`
- ✅ user_id: int
- ✅ car_id: int
- ✅ service_id: int
- ✅ team_id: Optional[int]
- ✅ vehicle_id: Optional[int]
- ✅ location: str
- ✅ location_lat: Optional[float]
- ✅ location_lng: Optional[float]
- ✅ scheduled_time: datetime
- ✅ total_price: float
- ✅ notes: Optional[str]

**Endpoints:**
- GET /api/bookings (all bookings)
- GET /api/bookings/user/{user_id} (user's bookings)
- POST /api/bookings

---

## ✅ TEAMS TABLE
**Database Columns:**
- id (int, auto_increment)
- name (varchar)
- employee_id (int, nullable)
- status (enum: available, busy, offline)
- created_at, updated_at (timestamps)

**Endpoints:**
- GET /api/teams

---

## 🔧 Available Endpoints

### Base
- `GET /` - API health check

### Users
- `GET /api/users` - Get all users
- `POST /api/users` - Create new user

### Cars
- `GET /api/cars` - Get all cars
- `POST /api/cars` - Create new car

### Services
- `GET /api/services` - Get active services

### Bookings
- `GET /api/bookings` - Get all bookings
- `GET /api/bookings/user/{user_id}` - Get bookings for specific user
- `POST /api/bookings` - Create new booking

### Teams
- `GET /api/teams` - Get all teams

---

## 📝 Test Your API

**Server:** http://localhost:8000
**API Docs:** http://localhost:8000/docs
**OpenAPI Spec:** http://localhost:8000/openapi.json
