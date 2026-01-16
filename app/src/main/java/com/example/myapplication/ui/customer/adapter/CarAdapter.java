package com.example.myapplication.ui.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.data.models.Car;

import java.util.List;

public class CarAdapter extends BaseAdapter {
    private Context context;
    private List<Car> cars;
    private onCarDeleteListener listener;

    public interface onCarDeleteListener {
        void onCarDelete(Car car, int position);
    }
    public CarAdapter(Context context, List<Car> cars, onCarDeleteListener listener) {
        this.context = context;
        this.cars = cars;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return cars.size();
    }

    @Override
    public Car getItem(int position) {
        return cars.get(position);
    }

    @Override
    public long getItemId(int position) {
        return cars.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
            holder = new ViewHolder();
            holder.carMakeModel = convertView.findViewById(R.id.carMakeModel);
            holder.carPlateNumber = convertView.findViewById(R.id.carPlateNumber);
            holder.carColor = convertView.findViewById(R.id.carColor);
            holder.btnDeleteCar = convertView.findViewById(R.id.btnDeleteCar);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        Car car = getItem(position);
        String displayName = car.getModel();
        if (car.getYear() != null){
            displayName += " " + car.getYear();
        }
        holder.carMakeModel.setText(displayName);
        holder.carPlateNumber.setText(car.getPlateNumber());
        holder.carColor.setText("Color: " + car.getColor());

        holder.btnDeleteCar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCarDelete(car, position);
            }
        });
        return convertView;
    }
    public void updateCars(List<Car> cars) {
        this.cars = cars;
        notifyDataSetChanged();
    }
    public void removeCar(int position) {
        cars.remove(position);
        notifyDataSetChanged();
    }
    private static class ViewHolder {
        TextView carMakeModel;
        TextView carPlateNumber;
        TextView carColor;
        ImageButton btnDeleteCar;
    }
}
