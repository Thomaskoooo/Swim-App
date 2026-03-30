package com.ovocko.swim;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

	private static final String WEB_NOTIFY_CHANNEL_ID = "web_notifications";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WebView webView = getBridge().getWebView();
		webView.addJavascriptInterface(new AppBridge(this), "AndroidAppBridge");

		// Keep the app visible and active while open for long tracking sessions.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		createWebNotificationChannel();
		ensureBridgeInjected();
	}

	@Override
	public void onResume() {
		super.onResume();
		ensureBridgeInjected();
		startTrackingServiceIfPermitted();
	}

	private void ensureBridgeInjected() {
		final boolean carModeActive = isCarModeActive();
		WebView webView = getBridge().getWebView();
		webView.postDelayed(() -> webView.evaluateJavascript(
				"(function(){"
						+ "if(window.__ovockoNativeBridgeInstalled){return;}"
						+ "window.__ovockoNativeBridgeInstalled=true;"
						+ "window.SWIM_ANDROID_AUTO=" + (carModeActive ? "true" : "false") + ";"
						+ "function postToNative(title, body){"
						+ "try{if(window.AndroidAppBridge&&window.AndroidAppBridge.showNativeNotification){"
						+ "window.AndroidAppBridge.showNativeNotification(String(title||'Notification'), String(body||''));"
						+ "}}catch(e){}"
						+ "}"
						+ "window.OvockoNative={"
						+ "notify:function(title,body){postToNative(title,body);},"
						+ "setTrackingActive:function(active){"
						+ "try{if(window.AndroidAppBridge&&window.AndroidAppBridge.setTrackingActive){window.AndroidAppBridge.setTrackingActive(!!active);}}catch(e){}"
						+ "},"
						+ "keepScreenOn:function(active){"
						+ "try{if(window.AndroidAppBridge&&window.AndroidAppBridge.keepScreenOn){window.AndroidAppBridge.keepScreenOn(!!active);}}catch(e){}"
						+ "}"
						+ "};"
						+ "if('Notification' in window){"
						+ "var OriginalNotification=window.Notification;"
						+ "var WrappedNotification=function(title,options){"
						+ "postToNative(title, options&&options.body);"
						+ "try{return new OriginalNotification(title,options);}catch(e){return {}; }"
						+ "};"
						+ "WrappedNotification.permission='granted';"
						+ "WrappedNotification.requestPermission=function(){return Promise.resolve('granted');};"
						+ "window.Notification=WrappedNotification;"
						+ "}"
						+ "})();",
				null), 1200);
	}

	private boolean isCarModeActive() {
		UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
		if (uiModeManager == null) {
			return false;
		}
		return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR;
	}

	private void createWebNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					WEB_NOTIFY_CHANNEL_ID,
					"Swim Web Notifications",
					NotificationManager.IMPORTANCE_DEFAULT
			);
			channel.setDescription("Notifications forwarded from mnauslots and mnauchats");

			NotificationManager manager = getSystemService(NotificationManager.class);
			if (manager != null) {
				manager.createNotificationChannel(channel);
			}
		}
	}

	private boolean hasLocationPermission() {
		return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
						== PackageManager.PERMISSION_GRANTED;
	}

	private void startTrackingServiceIfPermitted() {
		if (!hasLocationPermission()) {
			return;
		}

		Intent serviceIntent = new Intent(this, TrackingForegroundService.class);
		try {
			ContextCompat.startForegroundService(this, serviceIntent);
		} catch (Exception ignored) {
			// Never crash app startup if OS blocks foreground service launch.
		}
	}

	public static class AppBridge {
		private final MainActivity activity;

		AppBridge(MainActivity activity) {
			this.activity = activity;
		}

		@JavascriptInterface
		public void showNativeNotification(String title, String body) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
					&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
					!= PackageManager.PERMISSION_GRANTED) {
				return;
			}

			Notification notification = new NotificationCompat.Builder(activity, WEB_NOTIFY_CHANNEL_ID)
					.setSmallIcon(android.R.drawable.ic_dialog_info)
					.setContentTitle(title == null || title.isEmpty() ? "Swim" : title)
					.setContentText(body == null ? "" : body)
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setAutoCancel(true)
					.build();

			NotificationManager manager =
					(NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
			if (manager != null) {
				manager.notify((int) System.currentTimeMillis(), notification);
			}
		}

		@JavascriptInterface
		public void setTrackingActive(boolean active) {
			Intent serviceIntent = new Intent(activity, TrackingForegroundService.class);
			if (active) {
				if (!activity.hasLocationPermission()) {
					return;
				}
				try {
					ContextCompat.startForegroundService(activity, serviceIntent);
				} catch (Exception ignored) {
				}
			} else {
				activity.stopService(serviceIntent);
			}
		}

		@JavascriptInterface
		public void keepScreenOn(boolean active) {
			activity.runOnUiThread(() -> {
				if (active) {
					activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			});
		}
	}
}
