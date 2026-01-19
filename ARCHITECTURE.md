# Architecture Documentation

## Overview

This application follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns and the Repository pattern for data abstraction.

## Architectural Layers

```
┌─────────────────────────────────────────┐
│           UI Layer (View)               │
│  Activities, Fragments, Adapters        │
└─────────────┬───────────────────────────┘
              │ observes LiveData
              ↓
┌─────────────────────────────────────────┐
│        ViewModel Layer                  │
│  Business Logic, State Management       │
└─────────────┬───────────────────────────┘
              │ calls
              ↓
┌─────────────────────────────────────────┐
│       Repository Layer                  │
│  Data Access Abstraction                │
└─────────────┬───────────────────────────┘
              │ uses
              ↓
┌─────────────────────────────────────────┐
│        Data Source Layer                │
│  Network (Retrofit/Volley), Storage     │
└─────────────────────────────────────────┘
```

## Key Patterns

### 1. MVVM Pattern

**Why MVVM?**
- Separates UI logic from business logic
- Survives configuration changes (screen rotation)
- Testable without UI dependencies
- Reactive data flow with LiveData

**Implementation:**

```java
// ViewModel
public class CarListViewModel extends AndroidViewModel {
    private final CarRepository repository;
    private final MutableLiveData<List<Car>> carsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public void loadCars() {
        loadingLiveData.setValue(true);
        repository.getCars().enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(Call<List<Car>> call, Response<List<Car>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    carsLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Failed to load cars");
                }
            }

            @Override
            public void onFailure(Call<List<Car>> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Network error");
            }
        });
    }
}

// View (Activity)
public class CarListActivity extends AppCompatActivity {
    private CarListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CarListViewModel.class);

        // Observe data
        viewModel.getCarsLiveData().observe(this, cars -> {
            carAdapter.updateCars(cars);
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        // Trigger data load
        viewModel.loadCars();
    }
}
```

### 2. Repository Pattern

**Why Repository?**
- Abstracts data source (network, database, cache)
- Single source of truth
- Easier to test ViewModels
- Can switch data sources without changing ViewModel

**Implementation:**

```java
public class CarRepository {
    private final ApiService apiService;

    public CarRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public Call<List<Car>> getCars() {
        return apiService.getCars();
    }

    public Call<Car> addCar(CarRequest request) {
        return apiService.addCar(request);
    }

    public Call<Void> deleteCar(int carId) {
        return apiService.deleteCar(carId);
    }
}
```

### 3. Dependency Injection (Manual)

**Pattern:** Constructor injection for testability

```java
// Production constructor
public AuthViewModel(@NonNull Application application) {
    super(application);
    this.authRepo = new AuthRepo(application);
    this.tokenManager = new TokenManager(application);
}

// Package-private constructor for testing
AuthViewModel(@NonNull Application application, AuthRepo authRepo, TokenManager tokenManager) {
    super(application);
    this.authRepo = authRepo;
    this.tokenManager = tokenManager;
}

// Test usage
@Test
public void testLogin() {
    AuthViewModel viewModel = new AuthViewModel(application, mockAuthRepo, mockTokenManager);
    // Test with mocked dependencies
}
```

### 4. LiveData Pattern

**Data Flow:**
```
Repository → ViewModel → LiveData → Observer (View)
```

**Benefits:**
- Lifecycle-aware (no memory leaks)
- Automatic UI updates
- No manual subscription management

**Types Used:**
- `MutableLiveData<T>`: Internal ViewModel state (private)
- `LiveData<T>`: Exposed to View (public, immutable)

```java
// In ViewModel
private final MutableLiveData<List<Car>> carsLiveData = new MutableLiveData<>();

public LiveData<List<Car>> getCarsLiveData() {
    return carsLiveData; // Expose as immutable LiveData
}

// In View
viewModel.getCarsLiveData().observe(this, cars -> {
    // Update UI
});
```

## Component Details

### ViewModels

All ViewModels follow these principles:

1. **Extend AndroidViewModel** for Application context
2. **Private MutableLiveData** for internal state
3. **Public LiveData getters** for observation
4. **Package-private constructor** for testing
5. **Cleanup in onCleared()**

```java
public class ExampleViewModel extends AndroidViewModel {
    private final MutableLiveData<Data> dataLiveData = new MutableLiveData<>();
    private Call<Data> currentCall;

    // Production constructor
    public ExampleViewModel(@NonNull Application application) {
        super(application);
    }

    // Test constructor
    ExampleViewModel(@NonNull Application application, Repository repo) {
        super(application);
        this.repository = repo;
    }

    public LiveData<Data> getDataLiveData() {
        return dataLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
```

### Repositories

Repositories abstract data sources and handle API calls:

**Responsibilities:**
- Create API service instances
- Execute network calls
- Return Retrofit `Call<T>` objects
- No business logic

**Example:**
```java
public class BookingRepository {
    private final ApiService apiService;

    public BookingRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public Call<List<Booking>> getUserBookings() {
        return apiService.getUserBookings();
    }

    public Call<Booking> createBooking(BookingRequest request) {
        return apiService.createBooking(request);
    }
}
```

### Network Layer

**Retrofit Configuration:**
```java
public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit.create(ApiService.class);
    }
}
```

**AuthInterceptor:**
```java
public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenManager.getToken();

        if (token != null && !token.isEmpty()) {
            Request.Builder requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .method(original.method(), original.body());

            return chain.proceed(requestBuilder.build());
        }

        return chain.proceed(original);
    }
}
```

### Authentication Flow

```
1. User enters credentials
   ↓
2. LoginFragment → AuthViewModel.login()
   ↓
3. AuthViewModel → AuthRepo.loginUser()
   ↓
4. AuthRepo → Retrofit API call
   ↓
5. Response → AuthViewModel saves token via TokenManager
   ↓
6. LiveData updates → LoginFragment observes
   ↓
7. Navigate to CustomerActivity
   ↓
8. All subsequent API calls include token (AuthInterceptor)
```

### Error Handling Strategy

**1. Network Errors:**
```java
@Override
public void onFailure(Call<T> call, Throwable t) {
    if (t instanceof IOException) {
        errorLiveData.setValue("Network connection failed");
    } else {
        errorLiveData.setValue("Unexpected error occurred");
    }
}
```

**2. HTTP Errors:**
```java
@Override
public void onResponse(Call<T> call, Response<T> response) {
    if (!response.isSuccessful()) {
        String errorMsg = parseErrorMessage(response);
        errorLiveData.setValue(errorMsg);
    }
}
```

**3. FastAPI Validation Errors:**
```java
private String parseErrorMessage(Response<?> response) {
    try {
        JSONObject json = new JSONObject(response.errorBody().string());
        JSONArray details = json.getJSONArray("detail");
        // Parse field-level validation errors
        String field = details.getJSONObject(0).getJSONArray("loc").getString(1);
        String msg = details.getJSONObject(0).getString("msg");
        return "Invalid " + field + ": " + msg;
    } catch (Exception e) {
        // Fallback to HTTP status code
        return getGenericError(response.code());
    }
}
```

## Memory Management

### Activity Lifecycle

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // Initialize ViewModel (survives rotation)
    viewModel = new ViewModelProvider(this).get(CarListViewModel.class);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    // Clean up resources
    if (currentDialog != null) {
        currentDialog.dismiss();
    }
}
```

### ViewModel Lifecycle

```java
@Override
protected void onCleared() {
    super.onCleared();
    // Cancel ongoing network calls
    if (currentCall != null) {
        currentCall.cancel();
        currentCall = null;
    }
}
```

### Volley Request Cancellation

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    VolleyClient.getInstance(this).cancelPendingRequests(REQUEST_TAG);
}
```

## Testing Strategy

### Unit Tests

**What to Test:**
- ViewModel business logic
- Repository data transformations
- TokenManager storage/retrieval
- Error message parsing

**Example:**
```java
@Test
public void testLoginSuccess() {
    // Arrange
    AuthResponse expectedResponse = new AuthResponse();
    expectedResponse.setToken("test-token");
    when(authRepo.loginUser(any())).thenReturn(mockCall);

    // Act
    viewModel.login("test@example.com", "password");
    verify(mockCall).enqueue(callbackCaptor.capture());
    callbackCaptor.getValue().onResponse(mockCall, Response.success(expectedResponse));

    // Assert
    verify(tokenManager).saveToken("test-token", anyInt(), anyString());
    verify(authResponseObserver).onChanged(expectedResponse);
}
```

### Mock Setup

```java
@Before
public void setup() {
    viewModel = new AuthViewModel(mockApplication, mockAuthRepo, mockTokenManager);
    viewModel.getAuthResponseLiveData().observeForever(authResponseObserver);
    viewModel.getErrorLiveData().observeForever(errorObserver);
}
```

## Design Decisions

### Why Dual HTTP Clients (Retrofit + Volley)?

**Retrofit:**
- Type-safe API definitions
- Automatic JSON parsing
- Better error handling
- Used for: Auth, Cars, Bookings, Services

**Volley:**
- Simpler for basic requests
- Educational purpose (contrast)
- Used for: Wallet operations only

**Recommendation:** Consolidate to Retrofit-only for consistency.

### Why Manual DI Instead of Dagger/Hilt?

- **Simplicity**: Learning project, avoid framework overhead
- **Testability**: Package-private constructors sufficient
- **Control**: Explicit dependency management

**Future:** Consider Hilt for larger projects.

### Why LiveData Instead of RxJava/Flow?

- **Official**: Part of Android Architecture Components
- **Lifecycle-aware**: Automatic cleanup
- **Simple**: Less learning curve
- **Sufficient**: Meets current requirements

**Future:** Consider Kotlin Flow for more complex reactive scenarios.

## Code Organization Principles

1. **Package by Feature**: `ui/auth/`, `ui/customer/`
2. **Separation of Concerns**: View, ViewModel, Repository
3. **Single Responsibility**: Each class has one job
4. **Testability**: Dependencies injected via constructors
5. **Immutability**: LiveData exposed as read-only

## Future Architectural Improvements

1. **Use Cases Layer**: Add interactors for complex business logic
2. **Coroutines**: Replace Retrofit callbacks with suspend functions
3. **Room Database**: Add local caching layer
4. **WorkManager**: Background sync for bookings
5. **Hilt**: Automated dependency injection
6. **Navigation Component**: Type-safe navigation
7. **DataStore**: Replace SharedPreferences for token storage
8. **Paging 3**: Efficient list loading for large datasets

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData Overview](https://developer.android.com/topic/libraries/architecture/livedata)
- [Guide to App Architecture](https://developer.android.com/topic/architecture/intro)
