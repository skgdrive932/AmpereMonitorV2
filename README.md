# AmpereMonitor 🔋

An Android battery and charging monitor inspired by the classic Ampere-style interface.

## Features

- Live battery percentage
- Charging / discharging status
- Plug type
- Battery health
- Battery technology
- Temperature
- Voltage
- Device manufacturer and model
- Android version and build ID
- Attempts to read live current in mA using `BatteryManager.BATTERY_PROPERTY_CURRENT_NOW`
- Automatic minimum and maximum current tracking during the session

## Important

The Android hardware/API does not expose charging current on every device. If the phone does not provide a usable value, the app displays `Unavailable`.

Current values can also vary by device, charger, cable, temperature, and battery-management behavior.

## Build

Open this project in Android Studio and let Gradle sync.

Recommended:
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35

## Package

`com.example.amperemonitor`

## GitHub Actions

The repository includes `.github/workflows/android.yml`. It checks out the project, installs JDK 17, makes `gradlew` executable, downloads Gradle 8.11.1 on first use, and builds the debug APK.
