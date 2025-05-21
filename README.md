# Ruler App

A powerful Android application that displays 7 adjustable ruler lines that stay on top of other apps. Perfect for designers, developers, and anyone who needs precise screen measurements. The app is optimized for landscape mode and remembers your line positions between sessions.

## Features

- **7 Adjustable Lines**: Horizontal lines, each with a distinct color for easy identification
- **Persistent State**: Line positions are automatically saved and restored between app sessions
- **Overlay Functionality**: Lines stay on top of all other apps
- **Intuitive Controls**: Drag handles make line adjustment simple and precise
- **First-Run Guidance**: Helpful instructions for first-time users
- **Robust Error Handling**: Clear error messages and recovery options
- **Landscape Optimized**: Perfect for wide-screen measurements
- **Minimal Battery Impact**: Efficient implementation with low resource usage

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

### Getting Started
1. **First Launch**
   - Open the app to see the welcome screen
   - Read the brief introduction about the app's features
   - Tap "Get Started" to proceed

2. **Starting the Ruler**
   - Tap the "Start Ruler" button
   - The app will request permission to display over other apps
   - Follow the on-screen instructions to grant the permission
   - The ruler overlay will appear with 7 horizontal lines

### Using the Ruler
- **Adjusting Lines**
  - Each line has a circular handle on the right side
  - Touch and hold a handle, then drag up or down to adjust the line's position
  - Release to set the line in its new position
  - The position is automatically saved

- **Working with Other Apps**
  - The ruler stays on top of other apps
  - Switch to any app and the ruler will remain visible
  - Tap the floating control button to show/hide the ruler handles

- **Stopping the Ruler**
  - Return to the Ruler app
  - Tap "Stop Ruler" to remove the overlay
  - Or use the floating control button and tap "Close"

### Tips & Tricks
- Double-tap a line's handle to quickly reset it to its default position
- The app remembers your line positions between sessions
- For precise adjustments, use two fingers to "zoom in" on the ruler area
- The app is optimized for landscape mode - rotate your device for the best experience

## Permissions

This app requires the following permissions:
- `SYSTEM_ALERT_WINDOW`: To draw the ruler lines over other apps
- `INTERNET` (optional): For crash reporting and analytics (if implemented in future versions)

The app follows best practices for user privacy and only requests permissions that are essential for its core functionality.

## Advanced Features

### Persistent Line Positions
- Line positions are automatically saved when adjusted
- Positions are restored when you restart the app
- Reset all lines to default positions from the app settings

### Error Recovery
- Clear error messages help you understand and resolve issues
- Automatic recovery from common error conditions
- Detailed logging for troubleshooting (in debug builds)

## Troubleshooting

### Common Issues
1. **Lines not appearing**
   - Make sure you've granted the overlay permission
   - Restart the app if you just granted the permission

2. **Lines not saving positions**
   - Check if storage permission is granted
   - Try restarting the app

3. **App crashes or behaves unexpectedly**
   - Clear app data from device settings
   - Reinstall the app if issues persist

## Contributing

We welcome contributions! Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on how to submit pull requests, report issues, or suggest new features.

## License

This project is open source and available under the MIT License.
