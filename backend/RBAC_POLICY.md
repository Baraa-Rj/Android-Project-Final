# Role-Based Access Control (RBAC) Policy

## Roles

### 1. Customer
- **Description:** Regular users who book car wash services
- **Permissions:**
  - View and edit their own profile
  - View and manage their own cars
  - View and manage their own bookings
  - View available services
  - View teams (read-only)

### 2. Employee
- **Description:** Car wash service employees
- **Permissions:**
  - All customer permissions
  - View all users, cars, and bookings
  - Manage all bookings (for work assignments)
  - View all services
  - View and manage teams
  - **Cannot:** Delete users, modify services, manage other employees

### 3. Manager
- **Description:** Administrators with full system access
- **Permissions:**
  - Full access to all endpoints
  - User management (create, update, delete)
  - Service management (create, update, delete, activate/deactivate)
  - Team management
  - View all system data

---

## Endpoint Access Control

### Authentication Endpoints (`/api/auth`)
- **Public:** `/register`, `/login`
- **Authenticated:** `/me` (any role)

### Users Endpoints (`/api/users`)
| Endpoint | Customer | Employee | Manager |
|----------|----------|----------|---------|
| GET `/api/users` | ❌ | ✅ | ✅ |
| POST `/api/users` | ❌ | ❌ | ✅ |
| GET `/api/users/{id}` | Own only | ✅ | ✅ |
| PUT `/api/users/{id}` | Own only | Own only | ✅ |
| DELETE `/api/users/{id}` | ❌ | ❌ | ✅ |

### Cars Endpoints (`/api/cars`)
| Endpoint | Customer | Employee | Manager |
|----------|----------|----------|---------|
| GET `/api/cars` | Own only* | ✅ | ✅ |
| POST `/api/cars` | ✅ (own) | ✅ | ✅ |
| GET `/api/cars/{id}` | Own only | ✅ | ✅ |
| PUT `/api/cars/{id}` | Own only | ✅ | ✅ |
| DELETE `/api/cars/{id}` | Own only | ✅ | ✅ |
| GET `/api/cars/user/{user_id}` | Own only | ✅ | ✅ |
| POST `/api/cars/user/{user_id}` | Own only | ✅ | ✅ |
| PUT `/api/cars/user/{user_id}/car/{id}` | Own only | ✅ | ✅ |
| DELETE `/api/cars/user/{user_id}/car/{id}` | Own only | ✅ | ✅ |

*Customers calling GET /api/cars will be filtered to show only their cars

### Services Endpoints (`/api/services`)
| Endpoint | Customer | Employee | Manager |
|----------|----------|----------|---------|
| GET `/api/services` | ✅ | ✅ | ✅ |
| GET `/api/services/active` | ✅ | ✅ | ✅ |
| GET `/api/services/{id}` | ✅ | ✅ | ✅ |
| POST `/api/services` | ❌ | ❌ | ✅ |
| PUT `/api/services/{id}` | ❌ | ❌ | ✅ |
| DELETE `/api/services/{id}` | ❌ | ❌ | ✅ |
| POST `/api/services/activate/{id}` | ❌ | ❌ | ✅ |
| POST `/api/services/deactivate/{id}` | ❌ | ❌ | ✅ |

### Bookings Endpoints (`/api/bookings`)
| Endpoint | Customer | Employee | Manager |
|----------|----------|----------|---------|
| GET `/api/bookings` | Own only* | ✅ | ✅ |
| POST `/api/bookings` | ✅ (own) | ✅ | ✅ |
| GET `/api/bookings/{id}` | Own only | ✅ | ✅ |
| PUT `/api/bookings/{id}` | Own only | ✅ | ✅ |
| DELETE `/api/bookings/{id}` | Own only | ✅ | ✅ |
| GET `/api/bookings/user/{user_id}` | Own only | ✅ | ✅ |
| GET `/api/bookings/service/{id}` | ❌ | ✅ | ✅ |
| GET `/api/bookings/team/{id}` | ❌ | ✅ | ✅ |
| GET `/api/bookings/car/{id}` | Own only | ✅ | ✅ |
| Other GET filters | Own only | ✅ | ✅ |

*Customers calling GET /api/bookings will be filtered to show only their bookings

### Teams Endpoints (`/api/teams`)
| Endpoint | Customer | Employee | Manager |
|----------|----------|----------|---------|
| GET `/api/teams` | ✅ | ✅ | ✅ |
| GET `/api/teams/{id}` | ✅ | ✅ | ✅ |
| POST `/api/teams` | ❌ | ✅ | ✅ |
| PUT `/api/teams/{id}` | ❌ | ✅ | ✅ |
| DELETE `/api/teams/{id}` | ❌ | ❌ | ✅ |
| GET `/api/teams/{id}/members` | ✅ | ✅ | ✅ |
| POST `/api/teams/{id}/members` | ❌ | ✅ | ✅ |
| DELETE `/api/teams/{id}/members/{user_id}` | ❌ | ✅ | ✅ |

---

## Implementation Strategy

1. **Authentication Required:** All endpoints except `/auth/register` and `/auth/login`
2. **Role Checking:** Use `Depends(require_role(...))` for role-based restrictions
3. **Resource Ownership:** Customers can only access resources they own
4. **Hierarchical Access:** Manager > Employee > Customer

## Error Responses

- **401 Unauthorized:** Missing or invalid token
- **403 Forbidden:** Valid token but insufficient permissions
- **404 Not Found:** Resource doesn't exist OR user doesn't have permission (prevents information leakage)
