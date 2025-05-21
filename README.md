# Ruler App

A simple Android application that displays 7 adjustable ruler lines that stay on top of other apps. This app is designed to be used in landscape mode to measure or mark positions on the screen.

## Features

- 7 adjustable horizontal lines, each with a different color
- Lines stay on top of all other apps (overlay functionality)
- Each line can be individually adjusted by dragging its handle
- Works in landscape orientation
- Simple interface with start/stop controls

## Requirements

- Android Studio Dolphin (2021.3.1) or newer
- Android SDK 33 (Tiramisu) or newer
- Minimum Android version supported: Android 8.0 (API level 26)

## Building the App

1. Clone this repository
2. Open the project in Android Studio
3. Build the project using Gradle:
   ```
   ./gradlew assembleDebug
   ```
4. The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`

## Testing the App

### Using an Emulator

1. Create an AVD (Android Virtual Device) in Android Studio with:
   - API level 26 or higher
   - Landscape orientation
2. Start the emulator and install the app
3. Run the app and grant the overlay permission when prompted

### Using a Physical Device (Development)

1. Enable Developer Options on your Android device:
   - Go to Settings > About phone
   - Tap on "Build number" 7 times to enable Developer Options
2. Enable USB Debugging in Developer Options
3. Connect your device to your computer and authorize the USB debugging connection
4. Install the app using Android Studio or ADB:
   ```
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
5. Run the app and grant the overlay permission when prompted

### Sideloading the App (For End Users)

To install the app directly on an Android device without using Android Studio:

1. **Build the APK** on your development machine using the instructions above
2. **Enable installation from unknown sources** on the Android device:
   - **For Android 8.0 or higher**:
     - Go to Settings > Apps > Special access > Install unknown apps
     - Select the app you'll use to install the APK (e.g., Files, Chrome, or your file manager)
     - Toggle "Allow from this source" to ON
   - **For Android 7.0 or lower**:
     - Go to Settings > Security (or Privacy)
     - Enable "Unknown sources"
3. **Transfer the APK** to the Android device using one of these methods:
   - Email the APK to yourself and download it on the device
   - Upload the APK to Google Drive or Dropbox and download it on the device
   - Transfer using a USB cable and copy to device storage
   - Share via Bluetooth or other sharing methods
4. **Install the APK**:
   - Navigate to the APK file using a file manager
   - Tap on the APK file
   - Follow the prompts to install
   - When installation is complete, tap "Open" to launch the app
5. **Grant permissions**:
   - When you first try to use the ruler functionality, you'll be prompted to grant the overlay permission
   - Follow the on-screen instructions to enable this permission

## Usage

1. Open the app
2. Tap "Start Ruler" to display the ruler overlay
3. Grant the permission to draw over other apps if prompted
4. Use the circular handles on the right side to adjust the position of each line
5. Switch to any other app - the ruler lines will remain visible
6. To close the ruler, tap the "Close" button in the bottom right corner or return to the app and tap "Stop Ruler"

## Permissions

This app requires the `SYSTEM_ALERT_WINDOW` permission to draw the ruler lines over other apps.

## License

This project is open source and available under the MIT License.
