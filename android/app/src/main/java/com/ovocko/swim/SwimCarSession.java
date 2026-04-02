package com.ovocko.swim;

import androidx.annotation.NonNull;
import androidx.car.app.Screen;
import androidx.car.app.Session;

public class SwimCarSession extends Session {
    @NonNull
    @Override
    public Screen onCreateScreen(@NonNull androidx.car.app.CarContext carContext) {
        return new SwimCarScreen(carContext);
    }
}
