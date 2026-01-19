package com.example.myapplication.ui.customer.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.myapplication.data.models.Car;
import com.example.myapplication.data.models.CarRequest;
import com.example.myapplication.data.repository.CarRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class CarListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private CarRepository carRepository;

    @Mock
    private Call<List<Car>> getCarsCall;

    @Mock
    private Call<Car> addCarCall;

    @Mock
    private Call<Void> deleteCarCall;

    @Mock
    private Observer<List<Car>> carsObserver;

    @Mock
    private Observer<String> errorObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<Boolean> carAddedObserver;

    @Mock
    private Observer<Boolean> carDeletedObserver;

    private CarListViewModel viewModel;

    @Before
    public void setup() {
        viewModel = new CarListViewModel(application, carRepository);

        viewModel.getCarsLiveData().observeForever(carsObserver);
        viewModel.getErrorLiveData().observeForever(errorObserver);
        viewModel.getLoadingLiveData().observeForever(loadingObserver);
        viewModel.getCarAddedLiveData().observeForever(carAddedObserver);
        viewModel.getCarDeletedLiveData().observeForever(carDeletedObserver);
    }

    @Test
    public void testLoadCars_Success() {
        // Given
        Car car1 = new Car();
        car1.setId(1);
        car1.setModel("Toyota Camry");

        Car car2 = new Car();
        car2.setId(2);
        car2.setModel("Honda Accord");

        List<Car> expectedCars = Arrays.asList(car1, car2);

        when(carRepository.getCars()).thenReturn(getCarsCall);

        // When
        viewModel.loadCars();

        // Capture the callback
        ArgumentCaptor<Callback<List<Car>>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(getCarsCall).enqueue(callbackCaptor.capture());

        // Simulate successful response
        callbackCaptor.getValue().onResponse(getCarsCall, Response.success(expectedCars));

        // Then
        verify(carsObserver).onChanged(expectedCars);
        verify(errorObserver, never()).onChanged(any());
    }

    @Test
    public void testLoadCars_Failure() {
        // Given
        when(carRepository.getCars()).thenReturn(getCarsCall);

        // When
        viewModel.loadCars();

        // Capture the callback
        ArgumentCaptor<Callback<List<Car>>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(getCarsCall).enqueue(callbackCaptor.capture());

        // Simulate failure response
        ResponseBody errorBody = ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"error\":\"Server error\"}"
        );
        callbackCaptor.getValue().onResponse(getCarsCall, Response.error(500, errorBody));

        // Then
        verify(errorObserver).onChanged("Failed to load cars");
        verify(carsObserver, never()).onChanged(any());
    }

    @Test
    public void testLoadCars_NetworkError() {
        // Given
        when(carRepository.getCars()).thenReturn(getCarsCall);

        // When
        viewModel.loadCars();

        // Capture the callback
        ArgumentCaptor<Callback<List<Car>>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(getCarsCall).enqueue(callbackCaptor.capture());

        // Simulate network error
        callbackCaptor.getValue().onFailure(getCarsCall, new Exception("Network error"));

        // Then
        verify(errorObserver).onChanged("Connection failed. Please check your internet connection.");
        verify(carsObserver, never()).onChanged(any());
    }

    @Test
    public void testAddCar_Success() {
        // Given
        CarRequest request = new CarRequest("Toyota Camry", "ABC123", "Blue", 2020);
        Car addedCar = new Car();
        addedCar.setId(1);
        addedCar.setModel("Toyota Camry");

        when(carRepository.addCar(any(CarRequest.class))).thenReturn(addCarCall);
        when(carRepository.getCars()).thenReturn(getCarsCall);

        // When
        viewModel.addCar(request);

        // Capture the callback
        ArgumentCaptor<Callback<Car>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(addCarCall).enqueue(callbackCaptor.capture());

        // Simulate successful response
        callbackCaptor.getValue().onResponse(addCarCall, Response.success(addedCar));

        // Then
        verify(carAddedObserver).onChanged(true);
        verify(carRepository).getCars(); // Should reload cars
    }

    @Test
    public void testAddCar_Failure() {
        // Given
        CarRequest request = new CarRequest("Toyota Camry", "ABC123", "Blue", 2020);

        when(carRepository.addCar(any(CarRequest.class))).thenReturn(addCarCall);

        // When
        viewModel.addCar(request);

        // Capture the callback
        ArgumentCaptor<Callback<Car>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(addCarCall).enqueue(callbackCaptor.capture());

        // Simulate failure response
        ResponseBody errorBody = ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"error\":\"Invalid request\"}"
        );
        callbackCaptor.getValue().onResponse(addCarCall, Response.error(400, errorBody));

        // Then
        verify(errorObserver).onChanged("Failed to add car");
        verify(carAddedObserver, never()).onChanged(any());
    }

    @Test
    public void testDeleteCar_Success() {
        // Given
        int carId = 1;

        when(carRepository.deleteCar(anyInt())).thenReturn(deleteCarCall);
        when(carRepository.getCars()).thenReturn(getCarsCall);

        // When
        viewModel.deleteCar(carId);

        // Capture the callback
        ArgumentCaptor<Callback<Void>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(deleteCarCall).enqueue(callbackCaptor.capture());

        // Simulate successful response
        callbackCaptor.getValue().onResponse(deleteCarCall, Response.success(null));

        // Then
        verify(carDeletedObserver).onChanged(true);
        verify(carRepository).getCars(); // Should reload cars
    }

    @Test
    public void testDeleteCar_Failure() {
        // Given
        int carId = 1;

        when(carRepository.deleteCar(anyInt())).thenReturn(deleteCarCall);

        // When
        viewModel.deleteCar(carId);

        // Capture the callback
        ArgumentCaptor<Callback<Void>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(deleteCarCall).enqueue(callbackCaptor.capture());

        // Simulate failure response
        ResponseBody errorBody = ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"error\":\"Server error\"}"
        );
        callbackCaptor.getValue().onResponse(deleteCarCall, Response.error(500, errorBody));

        // Then
        verify(errorObserver).onChanged("Failed to delete car");
        verify(carDeletedObserver, never()).onChanged(any());
    }

    @Test
    public void testLoadingState() {
        // Given
        when(carRepository.getCars()).thenReturn(getCarsCall);

        // When
        viewModel.loadCars();

        // Capture the callback
        ArgumentCaptor<Callback<List<Car>>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(getCarsCall).enqueue(callbackCaptor.capture());
        callbackCaptor.getValue().onResponse(getCarsCall, Response.success(Arrays.asList()));

        // Then
        ArgumentCaptor<Boolean> loadingCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(loadingObserver, times(3)).onChanged(loadingCaptor.capture());

        // First call should be false (initial state)
        assertFalse(loadingCaptor.getAllValues().get(0));
        // Second call should be true (loading started)
        assertTrue(loadingCaptor.getAllValues().get(1));
        // Third call should be false (loading stopped)
        assertFalse(loadingCaptor.getAllValues().get(2));
    }
}
