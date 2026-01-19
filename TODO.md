# Future Work & TODO Items

## High Priority

### 1. Implement Cancel Booking Feature
**Location:** `BookingDetailActivity.java:111`

**Requirements:**
- Add API endpoint to backend: `DELETE /api/bookings/{id}` or `PUT /api/bookings/{id}/cancel`
- Implement confirmation dialog before cancellation
- Update booking status to "CANCELLED"
- Refresh booking list after cancellation
- Handle edge cases (already completed bookings cannot be cancelled)

**Estimated Effort:** 2-3 hours

**Implementation:**
```java
private void handleCancelBooking() {
    new AlertDialog.Builder(this)
        .setTitle("Cancel Booking")
        .setMessage("Are you sure you want to cancel this booking?")
        .setPositiveButton("Yes", (dialog, which) -> {
            // Call API to cancel
            bookingRepository.cancelBooking(bookingId).enqueue(...);
        })
        .setNegativeButton("No", null)
        .show();
}
```

---

### 2. Implement Reschedule Booking Feature
**Location:** `BookingDetailActivity.java:116`

**Requirements:**
- Add date/time picker UI
- Validate new time is in the future
- Call API endpoint: `PUT /api/bookings/{id}/reschedule`
- Update booking details after successful reschedule
- Show confirmation message

**Estimated Effort:** 3-4 hours

**UI Components Needed:**
- MaterialDatePicker
- MaterialTimePicker
- Confirmation dialog

---

### 3. Implement Notifications System
**Location:** `CustomerProfileFragment.java:82`

**Requirements:**
- **Backend:** Set up push notification service (FCM)
- **Android:** Integrate Firebase Cloud Messaging
- **Features:**
  - Booking confirmation notifications
  - Booking reminder (1 day before)
  - Booking status updates
  - Wallet transaction notifications

**Estimated Effort:** 1-2 days

**Dependencies:**
```gradle
implementation 'com.google.firebase:firebase-messaging:23.x.x'
```

---

## Medium Priority

### 4. Complete View Binding Migration
**Status:** 3/6 activities migrated

**Remaining Files:**
- `CarListActivity.java` (4 findViewById calls)
- `WalletActivity.java` (7 findViewById calls)
- `BookingActivity.java` (15 findViewById calls)
- `LoginFragment.java` (4 findViewById calls)
- `RegisterFragment.java` (6 findViewById calls)
- `CustomerHomeFragment.java`
- `CustomerProfileFragment.java`
- `CustomerServicesFragment.java`
- `CustomerBookingsFragment.java`
- All adapters (CarAdapter, BookingAdapter, ServiceAdapter, TransactionAdapter)

**Estimated Effort:** 4-6 hours

**Benefits:**
- Type-safe view access
- Eliminates ~60 findViewById calls
- Reduces null pointer exceptions

---

### 5. Add Integration Tests
**Current Coverage:** Unit tests only

**Test Scenarios:**
```java
// Integration test example
@Test
public void testLoginToBookingFlow() {
    // 1. Mock API responses
    mockWebServer.enqueue(new MockResponse()
        .setBody(loginResponseJson)
        .setResponseCode(200));

    // 2. Perform login
    authRepo.loginUser(loginRequest).execute();

    // 3. Verify token saved
    assertNotNull(tokenManager.getToken());

    // 4. Verify subsequent API calls include token
    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("Bearer test-token", request.getHeader("Authorization"));
}
```

**Estimated Effort:** 1-2 days

---

### 6. Add UI Tests (Espresso)
**Current Coverage:** None

**Test Scenarios:**
- Login flow end-to-end
- Car creation and deletion
- Booking creation
- Navigation between tabs

**Example:**
```java
@Test
public void testLoginFlow() {
    onView(withId(R.id.emailEditText)).perform(typeText("test@example.com"));
    onView(withId(R.id.passwordEditText)).perform(typeText("password123"));
    onView(withId(R.id.loginButton)).perform(click());

    // Verify navigation to CustomerActivity
    intended(hasComponent(CustomerActivity.class.getName()));
}
```

**Estimated Effort:** 2-3 days

---

## Low Priority

### 7. Migrate to Kotlin
**Rationale:**
- Null safety
- Coroutines for cleaner async code
- Data classes
- Extension functions
- Less boilerplate

**Migration Strategy:**
1. Start with data models (low risk)
2. Migrate utilities (TokenManager, etc.)
3. Migrate repositories
4. Migrate ViewModels
5. Migrate UI components last

**Estimated Effort:** 2-3 weeks (incremental)

---

### 8. Add Local Database Caching (Room)
**Use Case:** Offline support

**Implementation:**
```java
@Entity
public class Car {
    @PrimaryKey
    private int id;
    private String model;
    // ... other fields
}

@Dao
public interface CarDao {
    @Query("SELECT * FROM car")
    LiveData<List<Car>> getAllCars();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Car> cars);
}
```

**Benefits:**
- App works offline
- Faster load times
- Better UX

**Estimated Effort:** 1 week

---

### 9. Implement Pagination for Large Lists
**Current Issue:** Loading all bookings/transactions at once

**Solution:** Use Paging 3 library

```java
@Query("SELECT * FROM booking ORDER BY scheduled_time DESC")
PagingSource<Integer, Booking> pagingSource();
```

**Estimated Effort:** 3-4 days

---

### 10. Add Image Upload for Profile/Cars
**Requirements:**
- Image picker
- Image compression
- Upload to server
- Display in UI

**Libraries:**
- Glide or Coil for image loading
- CameraX for camera integration

**Estimated Effort:** 1 week

---

### 11. Implement Pull-to-Refresh
**Files to Update:**
- CarListActivity
- CustomerBookingsFragment
- WalletActivity (transactions)

**Implementation:**
```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <!-- Content -->
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

```java
swipeRefresh.setOnRefreshListener(() -> {
    viewModel.loadCars();
});
```

**Estimated Effort:** 2-3 hours

---

### 12. Add Search & Filter Functionality
**Features:**
- Search cars by model/plate
- Filter bookings by status/date
- Filter transactions by type
- Search services by name/category

**Estimated Effort:** 1 week

---

### 13. Improve Error Messages
**Current:** Generic error messages

**Enhancement:** Show specific, actionable errors
- "Check your internet connection" → Show retry button
- "Session expired" → Auto-navigate to login
- "Server error" → Show "Report Problem" option

**Estimated Effort:** 2-3 days

---

### 14. Add Analytics
**Track:**
- Screen views
- Button clicks
- API call success/failure rates
- Crash reports

**Tools:**
- Firebase Analytics
- Crashlytics

**Estimated Effort:** 1-2 days

---

### 15. Implement Biometric Authentication
**Feature:** Login with fingerprint/face

```java
BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
    .setTitle("Biometric login")
    .setSubtitle("Log in using your fingerprint")
    .setNegativeButtonText("Use password")
    .build();
```

**Estimated Effort:** 3-4 days

---

## Technical Debt

### 1. Remove Volley Dependency
**Current:** Using both Retrofit and Volley

**Action:** Migrate WalletActivity to use Retrofit
- Create WalletRepository using Retrofit
- Update WalletViewModel
- Remove Volley dependency

**Benefit:** Single HTTP client, consistent patterns

**Estimated Effort:** 4-6 hours

---

### 2. Consolidate String Resources
**Current:** Some hardcoded strings

**Action:** Move all strings to `strings.xml`

```xml
<string name="error_network">Unable to connect to server</string>
<string name="error_generic">Something went wrong</string>
```

**Benefit:** Easier localization, consistency

**Estimated Effort:** 2-3 hours

---

### 3. Add Proguard Rules
**Current:** No obfuscation in release builds

**Action:** Configure Proguard for production

```proguard
-keep class com.example.myapplication.data.models.** { *; }
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}
```

**Estimated Effort:** 2-3 hours

---

### 4. Setup CI/CD Pipeline
**Tools:** GitHub Actions

**Pipeline:**
```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Run tests
        run: ./gradlew test
      - name: Build APK
        run: ./gradlew assembleDebug
```

**Estimated Effort:** 1 day

---

## Priority Matrix

| Task | Priority | Effort | Impact |
|------|----------|--------|--------|
| Cancel Booking | High | Low | High |
| Reschedule Booking | High | Medium | High |
| Notifications | High | High | High |
| View Binding Migration | Medium | Medium | Medium |
| Integration Tests | Medium | High | Medium |
| Remove Volley | Medium | Low | Medium |
| Kotlin Migration | Low | High | High (long-term) |
| Room Database | Low | High | Medium |
| UI Tests | Medium | High | Medium |
| Analytics | Low | Low | Low |

---

## Notes

- Tasks marked with file locations reference specific TODOs in code
- Effort estimates assume familiarity with Android development
- Priority based on user value and technical impact
- Update this file as items are completed
