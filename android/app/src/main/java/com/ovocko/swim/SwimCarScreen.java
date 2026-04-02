package com.ovocko.swim;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.ParkedOnlyOnClickListener;
import androidx.car.app.model.Template;

public class SwimCarScreen extends Screen {

    public SwimCarScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        OnClickListener openOnPhone = ParkedOnlyOnClickListener.create(() -> {
            Intent launchIntent = getCarContext().getPackageManager().getLaunchIntentForPackage(getCarContext().getPackageName());
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getCarContext().startActivity(launchIntent);
            }
        });

        return new MessageTemplate.Builder("Swim Tracker is available in phone mode for full map tracking. Use this car app entry to launch quickly when parked.")
                .setTitle("Swim Tracker")
                .addAction(new Action.Builder()
                        .setTitle("Open On Phone")
                        .setOnClickListener(openOnPhone)
                        .build())
                .setHeaderAction(Action.APP_ICON)
                .setActionStrip(new ActionStrip.Builder()
                        .addAction(new Action.Builder()
                                .setTitle("Refresh")
                                .setOnClickListener(() -> invalidate())
                                .build())
                        .build())
                .build();
    }
}
