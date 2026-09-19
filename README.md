# Swim APK Wrapper

This folder contains a Capacitor Android wrapper for the Swim app.

## 1) Set your real Swim URL

Edit [capacitor.config.json](capacitor.config.json) and replace:

- https://yap.asociaciairidologov.sk/swim/index.php?app_mode=1
- edit 26 still working hehe

with your URL

## 2) Sync project

Run:

- npm run sync

## 3) Open Android Studio

Run:

- npm run open:android

Then in Android Studio:

- Build > Build Bundle(s) / APK(s) > Build APK(s)

The APK output is usually under:

- android/app/build/outputs/apk/debug/app-debug.apk

## Notes

- This is an installed Android app wrapper (WebView), not a browser tab.
- Location permission prompts are handled by Android app permission flow.
- The app loads your hosted Swim backend URL, so PHP/session features continue working.
- For best tracking reliability on phone, disable battery optimization for this app in Android settings.
