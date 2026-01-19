# API Integration Guide

## Base URL Configuration

Located in `RetrofitClient.java`:
```java
private static final String BASE_URL = "http://your-backend-url/api/";
```

## Authentication

### JWT Token Flow

1. **Login/Register** → Receive access token
2. **Save token** → TokenManager stores in SharedPreferences
3. **Auto-inject** → AuthInterceptor adds `Authorization: Bearer {token}` header
4. **All requests** → Authenticated automatically

### Token Storage

**TokenManager.java** (`app/src/main/java/com/example/myapplication/utils/TokenManager.java`)

```java
// Save token after login
tokenManager.saveToken(token, userId, email);

// Retrieve token
String token = tokenManager.getToken();
int userId = tokenManager.getUserId();
String email = tokenManager.getUserEmail();

// Check login status
boolean isLoggedIn = tokenManager.isLoggedIn();

// Clear token (logout)
tokenManager.clearToken();
```

**Storage Keys:**
- `jwt_token`: Access token
- `user_id`: User ID
- `user_email`: User email

### AuthInterceptor

**Location:** `app/src/main/java/com/example/myapplication/utils/AuthInterceptor.java`

Automatically adds authentication headers to all requests:
```java
Authorization: Bearer {token}
Accept: application/json
```

## API Endpoints

### Authentication Endpoints

#### POST /auth/register
**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "password": "securePassword123"
}
```

**Response (200):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "role": "customer",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer"
}
```

**Android Implementation:**
```java
// AuthViewModel.java
public void register(String name, String email, String phone, String password) {
    RegisterRequest request = new RegisterRequest(name, email, phone, password);
    authRepo.registerUser(request).enqueue(new Callback<AuthResponse>() {
        @Override
        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = response.body();
                tokenManager.saveToken(
                    authResponse.getToken(),
                    authResponse.getId(),
                    authResponse.getEmail()
                );
                authResponseLiveData.setValue(authResponse);
            }
        }
    });
}
```

---

#### POST /auth/login
**Request:**
```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response (200):** Same as register

**Error (401):**
```json
{
  "detail": "Incorrect email or password"
}
```

**Android Implementation:**
```java
public void login(String email, String password) {
    LoginRequest request = new LoginRequest(email, password);
    authRepo.loginUser(request).enqueue(...);
}
```

---

### Car Management Endpoints

#### GET /cars
**Description:** Get all cars for authenticated user

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200):**
```json
[
  {
    "id": 1,
    "model": "Toyota Camry",
    "plate_number": "ABC123",
    "color": "Silver",
    "year": 2020,
    "user_id": 1
  }
]
```

**Android Implementation:**
```java
// CarRepository.java
public Call<List<Car>> getCars() {
    return apiService.getCars();
}

// CarListViewModel.java
public void loadCars() {
    carRepository.getCars().enqueue(new Callback<List<Car>>() {
        @Override
        public void onResponse(Call<List<Car>> call, Response<List<Car>> response) {
            if (response.isSuccessful()) {
                carsLiveData.setValue(response.body());
            }
        }
    });
}
```

---

#### POST /cars
**Description:** Add a new car

**Request:**
```json
{
  "model": "Honda Civic",
  "plate_number": "XYZ789",
  "color": "Blue",
  "year": 2021
}
```

**Response (201):**
```json
{
  "id": 2,
  "model": "Honda Civic",
  "plate_number": "XYZ789",
  "color": "Blue",
  "year": 2021,
  "user_id": 1
}
```

**Validation Error (422):**
```json
{
  "detail": [
    {
      "loc": ["body", "year"],
      "msg": "ensure this value is greater than 1900",
      "type": "value_error.number.not_gt"
    }
  ]
}
```

**Android Implementation:**
```java
public void addCar(CarRequest request) {
    carRepository.addCar(request).enqueue(new Callback<Car>() {
        @Override
        public void onResponse(Call<Car> call, Response<Car> response) {
            if (response.isSuccessful()) {
                carAddedLiveData.setValue(true);
                loadCars(); // Refresh list
            } else {
                String error = parseErrorMessage(response);
                errorLiveData.setValue(error);
            }
        }
    });
}
```

---

#### DELETE /cars/{id}
**Description:** Delete a car

**Response (204):** No content

**Android Implementation:**
```java
public void deleteCar(int carId) {
    carRepository.deleteCar(carId).enqueue(new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                carDeletedLiveData.setValue(true);
                loadCars(); // Refresh list
            }
        }
    });
}
```

---

### Service Endpoints

#### GET /services
**Description:** Get all available services

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Oil Change",
    "description": "Complete oil change service",
    "price": 49.99,
    "duration": 30
  }
]
```

**Android Implementation:**
```java
// ServiceRepository.java
public Call<List<Service>> getServices() {
    return apiService.getServices();
}
```

---

### Booking Endpoints

#### GET /bookings
**Description:** Get all bookings for authenticated user

**Response (200):**
```json
[
  {
    "id": 1,
    "service_id": 1,
    "service_name": "Oil Change",
    "car_id": 1,
    "car_model": "Toyota Camry",
    "scheduled_time": "2024-01-20T10:00:00",
    "location": "123 Main St",
    "status": "PENDING",
    "total_price": 49.99,
    "notes": "Please call before arriving"
  }
]
```

**Status Values:**
- `PENDING`: Booking confirmed, awaiting service
- `IN_PROGRESS`: Service in progress
- `COMPLETED`: Service completed
- `CANCELLED`: Booking cancelled

---

#### POST /bookings
**Description:** Create a new booking

**Request:**
```json
{
  "service_id": 1,
  "car_id": 1,
  "scheduled_time": "2024-01-20T10:00:00",
  "location": "123 Main St",
  "notes": "Please call before arriving"
}
```

**Response (201):**
```json
{
  "id": 1,
  "service_id": 1,
  "car_id": 1,
  "scheduled_time": "2024-01-20T10:00:00",
  "location": "123 Main St",
  "status": "PENDING",
  "total_price": 49.99,
  "notes": "Please call before arriving",
  "created_at": "2024-01-15T09:00:00"
}
```

**Android Implementation:**
```java
// BookingViewModel.java
public void createBooking(BookingRequest request) {
    bookingRepository.createBooking(request).enqueue(new Callback<Booking>() {
        @Override
        public void onResponse(Call<Booking> call, Response<Booking> response) {
            if (response.isSuccessful()) {
                Toast.makeText(context, "Booking created successfully", LENGTH_SHORT).show();
                // Navigate to bookings list
            }
        }
    });
}
```

---

### Wallet Endpoints (Volley)

**Note:** Wallet endpoints use Volley instead of Retrofit for educational purposes.

#### GET /wallet/balance
**Description:** Get user's wallet balance

**Implementation:**
```java
// WalletRepository.java (using Volley)
public void getWalletBalance(String requestTag, BalanceCallback callback) {
    String url = BASE_URL + "wallet/balance";

    JsonObjectRequest request = new JsonObjectRequest(
        Request.Method.GET, url, null,
        response -> {
            try {
                WalletBalance balance = gson.fromJson(response.toString(), WalletBalance.class);
                callback.onSuccess(balance);
            } catch (Exception e) {
                callback.onError("Failed to parse balance");
            }
        },
        error -> callback.onError(parseVolleyError(error))
    ) {
        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            String token = tokenManager.getToken();
            if (token != null) {
                headers.put("Authorization", "Bearer " + token);
            }
            headers.put("Accept", "application/json");
            return headers;
        }
    };

    request.setTag(requestTag);
    requestQueue.add(request);
}
```

**Response (200):**
```json
{
  "balance": 150.50,
  "total_credits": 200.00,
  "total_debits": 49.50
}
```

---

#### POST /wallet/deposit
**Description:** Add funds to wallet

**Request:**
```json
{
  "amount": 50.00
}
```

**Response (200):**
```json
{
  "balance": 200.50,
  "transaction_id": 123
}
```

---

#### GET /wallet/transactions
**Description:** Get transaction history

**Response (200):**
```json
[
  {
    "id": 1,
    "type": "CREDIT",
    "amount": 50.00,
    "description": "Wallet deposit",
    "created_at": "2024-01-15T09:00:00"
  },
  {
    "id": 2,
    "type": "DEBIT",
    "amount": 49.99,
    "description": "Oil Change service",
    "created_at": "2024-01-20T10:00:00"
  }
]
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Android Handling |
|------|---------|-----------------|
| 200 | Success | Process response body |
| 201 | Created | Process created resource |
| 204 | No Content | Success, no data to process |
| 400 | Bad Request | Parse error details, show to user |
| 401 | Unauthorized | Clear token, navigate to login |
| 422 | Validation Error | Parse field errors, show to user |
| 500 | Server Error | Show generic error, retry option |

### Error Response Format

**FastAPI Validation Errors:**
```json
{
  "detail": [
    {
      "loc": ["body", "email"],
      "msg": "not a valid email address",
      "type": "value_error.email"
    }
  ]
}
```

**Simple Errors:**
```json
{
  "detail": "Email already registered"
}
```

### Android Error Parsing

```java
private String parseErrorMessage(Response<?> response) {
    try {
        String errorBody = response.errorBody().string();
        JSONObject json = new JSONObject(errorBody);

        if (json.has("detail")) {
            Object detail = json.get("detail");

            // Handle array (validation errors)
            if (detail instanceof JSONArray) {
                JSONArray array = (JSONArray) detail;
                JSONObject firstError = array.getJSONObject(0);

                String field = firstError.getJSONArray("loc").getString(1);
                String msg = firstError.getString("msg");

                return field + ": " + msg;
            }

            // Handle string (simple errors)
            return detail.toString();
        }
    } catch (Exception e) {
        // Fallback
    }

    // Default error messages based on status code
    switch (response.code()) {
        case 400: return "Invalid request";
        case 401: return "Unauthorized. Please login again";
        case 422: return "Validation error";
        case 500: return "Server error";
        default: return "Unknown error occurred";
    }
}
```

---

## Network Configuration

### Timeouts

```java
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();
```

### Logging

```java
HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Log full requests/responses

OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(logging)
    .build();
```

**Production:** Set level to `NONE` or `BASIC`

---

## Testing API Integration

### Unit Tests with MockWebServer

```java
@Before
public void setup() {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    // Point Retrofit to mock server
    String baseUrl = mockWebServer.url("/").toString();
    retrofit = new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
}

@Test
public void testGetCars() throws Exception {
    // Mock response
    String json = "[{\"id\":1,\"model\":\"Toyota\"}]";
    mockWebServer.enqueue(new MockResponse()
        .setBody(json)
        .setResponseCode(200));

    // Make request
    Response<List<Car>> response = apiService.getCars().execute();

    // Assertions
    assertTrue(response.isSuccessful());
    assertEquals(1, response.body().size());
    assertEquals("Toyota", response.body().get(0).getModel());

    // Verify request
    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("/cars", request.getPath());
    assertEquals("Bearer test-token", request.getHeader("Authorization"));
}
```

---

## Best Practices

1. **Always use HTTPS in production**
2. **Store base URL in BuildConfig for different environments**
3. **Handle token expiration** (implement refresh token logic)
4. **Cancel requests on Activity/Fragment destroy**
5. **Use request tags for Volley** (for cancellation)
6. **Show loading indicators** during API calls
7. **Handle network connectivity** before making requests
8. **Implement retry logic** for failed requests
9. **Cache responses** when appropriate
10. **Log errors** for debugging (remove in production)

---

## Future Enhancements

1. **Refresh Token**: Implement token refresh before expiration
2. **Request Retry**: Automatic retry with exponential backoff
3. **Offline Support**: Queue requests when offline, sync when online
4. **Response Caching**: Cache GET requests with Room database
5. **WebSocket**: Real-time updates for booking status
6. **GraphQL**: Consider for complex data requirements
7. **API Versioning**: Handle multiple API versions

---

## Environment Configuration

```java
// BuildConfig usage (recommended)
public class ApiConfig {
    public static final String BASE_URL = BuildConfig.DEBUG
        ? "http://10.0.2.2:8000/api/"  // Android emulator localhost
        : "https://production-api.example.com/api/";
}
```

**build.gradle:**
```groovy
buildTypes {
    debug {
        buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:8000/api/\""
    }
    release {
        buildConfigField "String", "BASE_URL", "\"https://api.example.com/\""
    }
}
```
