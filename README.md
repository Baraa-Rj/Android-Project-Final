# Car Service Booking Android App

A modern Android application for booking car services, built with clean architecture principles and MVVM pattern.

## Features

- **User Authentication**: Secure JWT-based login and registration
- **Car Management**: Add, view, and delete personal vehicles
- **Service Booking**: Browse services and create bookings
- **Wallet System**: Manage credits and view transaction history
- **Booking History**: View past and upcoming service bookings

## Architecture

This project follows **MVVM (Model-View-ViewModel)** architecture with a clean separation of concerns:

```
app/
├── data/
│   ├── models/          # Data classes (Car, Booking, User, etc.)
│   ├── repository/      # Data layer abstraction
│   │   ├── AuthRepo.java
│   │   ├── CarRepository.java
│   │   ├── BookingRepository.java
│   │   └── WalletRepository.java
│   └── api/            # Retrofit API interfaces
│       └── ApiService.java
│
├── ui/
│   ├── auth/           # Authentication screens
│   │   ├── AuthViewModel.java
│   │   ├── LoginFragment.java
│   │   └── RegisterFragment.java
│   │
│   └── customer/       # Customer features
│       ├── viewmodel/  # ViewModels
│       │   ├── CarListViewModel.java
│       │   └── BookingViewModel.java
│       ├── adapter/    # RecyclerView/ListView adapters
│       └── *.java      # Activities & Fragments
│
├── utils/              # Utilities
│   ├── TokenManager.java
│   └── AuthInterceptor.java
│
└── network/            # Network clients
    ├── RetrofitClient.java
    └── VolleyClient.java
```

## Tech Stack

### Core
- **Language**: Java 11
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36

### Architecture & Components
- **MVVM Pattern**: ViewModel + LiveData for reactive UI
- **View Binding**: Type-safe view access (partial migration)
- **Lifecycle Components**: AndroidX ViewModel & LiveData

### Networking
- **Retrofit 2**: RESTful API calls (auth, cars, bookings, services)
- **OkHttp 3**: HTTP client with logging interceptor
- **Volley**: Alternative HTTP client (wallet operations)
- **Gson**: JSON serialization/deserialization

### Authentication
- **JWT Tokens**: Secure token-based authentication
- **SharedPreferences**: Local token storage
- **AuthInterceptor**: Automatic token injection

### UI/UX
- **Material Design 3**: Modern UI components
- **RecyclerView**: Efficient list rendering
- **Bottom Navigation**: Tab-based navigation
- **FloatingActionButton**: Quick actions

### Testing
- **JUnit 4**: Unit testing framework
- **Mockito**: Mocking framework
- **Arch Core Testing**: LiveData testing utilities
- **MockWebServer**: API mocking for tests

**Test Coverage**: 30 unit tests with 100% pass rate
- AuthViewModel: 9 tests
- CarListViewModel: 8 tests
- TokenManager: 12 tests
- ExampleUnitTest: 1 test

## Project Setup

### Prerequisites
- Android Studio Ladybug or later
- JDK 11+
- Gradle 8.13+

### Build & Run

1. Clone the repository:
```bash
git clone https://github.com/Baraa-Rj/Android-Project-Final.git
cd Android-Project-Final
```

2. Open in Android Studio

3. Sync Gradle dependencies

4. Run tests:
```bash
./gradlew test
```

5. Build the app:
```bash
./gradlew assembleDebug
```

6. Run on emulator or device

### Backend Configuration

Update the base URL in `RetrofitClient.java`:
```java
private static final String BASE_URL = "http://your-backend-url/api/";
```

## API Integration

### Authentication Flow
```java
// 1. Login via AuthViewModel
authViewModel.login(email, password);

// 2. Token saved automatically via TokenManager
tokenManager.saveToken(token, userId, email);

// 3. Token injected in all requests via AuthInterceptor
@Override
public Response intercept(Chain chain) {
    Request original = chain.request();
    String token = tokenManager.getToken();

    Request.Builder requestBuilder = original.newBuilder()
        .header("Authorization", "Bearer " + token);

    return chain.proceed(requestBuilder.build());
}
```

### Repository Pattern
```java
// Repository abstracts API calls
public class CarRepository {
    private final ApiService apiService;

    public Call<List<Car>> getCars() {
        return apiService.getCars();
    }
}

// ViewModel consumes repository
public class CarListViewModel extends AndroidViewModel {
    private final CarRepository repository;

    public void loadCars() {
        repository.getCars().enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(Call<List<Car>> call, Response<List<Car>> response) {
                carsLiveData.setValue(response.body());
            }
        });
    }
}
```

### LiveData Observation
```java
// Fragment/Activity observes ViewModel
carListViewModel.getCarsLiveData().observe(this, cars -> {
    if (cars != null) {
        carAdapter.updateCars(cars);
    }
});
```

## Error Handling

### FastAPI Error Parsing
The app handles FastAPI validation errors with field-level detail:

```java
private String parseErrorMessage(Response<?> response) {
    // Parses {"detail": [{"loc": ["field"], "msg": "error"}]}
    // Returns user-friendly messages like:
    // "Invalid email: not a valid email address"
}
```

### Network Error Handling
```java
@Override
public void onFailure(Call<T> call, Throwable t) {
    if (t.getMessage().contains("Unable to resolve host")) {
        errorLiveData.setValue("Cannot connect to server");
    } else {
        errorLiveData.setValue("Connection failed");
    }
}
```

## Memory Management

All Activities/ViewModels properly clean up resources:

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (currentCall != null) {
        currentCall.cancel();
        currentCall = null;
    }
    VolleyClient.getInstance(this).cancelPendingRequests(REQUEST_TAG);
}
```

## Testing

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport

# View reports
open app/build/reports/tests/testDebugUnitTest/index.html
```

### Writing Tests
```java
@RunWith(MockitoJUnitRunner.class)
public class AuthViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private AuthRepo authRepo;

    @Test
    public void testLoginSuccess() {
        // Arrange
        when(authRepo.loginUser(any())).thenReturn(call);

        // Act
        viewModel.login("test@example.com", "password");

        // Assert
        verify(tokenManager).saveToken(anyString(), anyInt(), anyString());
    }
}
```

## Known Limitations & Future Work

### TODO Items
1. **Notifications** (CustomerProfileFragment:82)
   - Implement push notifications when backend API is ready

2. **Cancel Booking** (BookingDetailActivity:111)
   - Add API endpoint and implement cancel functionality

3. **Reschedule Booking** (BookingDetailActivity:116)
   - Add date/time picker and rescheduling logic

### Architectural Improvements
- Complete View Binding migration (3/6 activities migrated)
- Migrate remaining activities: CarListActivity, WalletActivity, BookingActivity
- Migrate fragments and adapters to View Binding
- Consider Kotlin migration for null safety and conciseness

### Testing Enhancements
- Add integration tests with MockWebServer
- Add UI tests with Espresso
- Increase code coverage to >80%

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards
- Follow MVVM architecture pattern
- Use ViewModels for UI logic
- Keep Activities/Fragments lightweight
- Write unit tests for ViewModels and Repositories
- Use meaningful variable names
- Add comments for complex logic

## License

This project is part of an academic final project.

## Acknowledgments

- Built with [Retrofit](https://square.github.io/retrofit/)
- UI components from [Material Design](https://material.io/)
- Architecture guidance from [Android Architecture Components](https://developer.android.com/topic/architecture)
