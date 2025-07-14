# Ruler App

A powerful Android overlay application that displays 7 adjustable ruler lines on top of other apps. Perfect for designers, developers, and anyone who needs to measure and compare on-screen elements. The ruler lines adapt to device orientation and maintain their positions across app sessions.

## Features

- **7 Adjustable Lines**: Each line has a distinct color and number for easy identification
- **Orientation Aware**: 
  - Landscape mode: Horizontal lines with draggable handles on both sides
  - Portrait mode: Vertical lines (view-only, adjust in landscape)
- **Free Movement**: Lines can cross each other - no ordering constraints
- **Persistent State**: Line positions are automatically saved and restored between sessions
- **True Overlay**: Lines stay visible while you interact with other apps underneath
- **Intuitive Controls**: 
  - Numbered handles on both left and right sides in landscape mode
  - Close button (×) always visible in bottom-right corner
- **First-Run Guidance**: Helpful instructions for first-time users
- **Robust Error Handling**: Automatic recovery from orientation changes and service restarts
- **Minimal Battery Impact**: Efficient foreground service implementation

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
4. Note: The app icon should appear in your app drawer after installation

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
   - The ruler overlay will appear with 7 colored lines

### Using the Ruler

#### Line Orientation
- **Portrait Mode**: Lines appear vertically (top to bottom)
- **Landscape Mode**: Lines appear horizontally (left to right)
- Lines maintain their relative positions when rotating the device

#### Adjusting Lines (Landscape Mode Only)
- Each line has numbered handles on both left and right sides
- The number and color help identify each line (1-7)
- Touch and drag any handle to move that line
- Lines can freely cross over each other
- Positions are automatically saved after each adjustment

#### Working with Other Apps
- The ruler overlay remains visible on top of all apps
- You can interact with apps underneath the ruler lines
- Switch between apps normally - the ruler stays in place
- Perfect for comparing measurements across different apps

#### Closing the Ruler
- Tap the × button in the bottom-right corner (visible in any orientation)
- Or return to the Ruler app and tap "Stop Ruler"
- Line positions are saved when closing

### Tips & Tricks
- Rotate to landscape mode to adjust line positions
- Each line's number stays constant even when lines cross
- Line colors: Red (1), Orange (2), Gold (3), Green (4), Sky Blue (5), Violet (6), Magenta (7)
- The app automatically saves positions when you close it with the × button
- Lines are constrained to stay within screen boundaries with padding

## Permissions

This app requires the following permissions:
- `SYSTEM_ALERT_WINDOW`: To draw the ruler lines over other apps
- `FOREGROUND_SERVICE`: To keep the ruler service running reliably
- `FOREGROUND_SERVICE_SPECIAL_USE`: Required for Android 14+ overlay services

The app follows best practices for user privacy and only requests permissions that are essential for its core functionality.

## Advanced Features

### Line Management
- Line positions are saved in real-time as you adjust them
- Positions persist across app restarts and device reboots
- Lines can be freely arranged - they can overlap or cross each other
- Each line maintains its identity (number and color) regardless of position

### Error Recovery
- Clear error messages help you understand and resolve issues
- Automatic recovery from common error conditions
- Detailed logging for troubleshooting (in debug builds)

## Troubleshooting

### Common Issues
1. **Lines not appearing**
   - Ensure you've granted the overlay permission
   - Check that the ruler service is running (notification should be visible)
   - Try stopping and starting the ruler from the main app

2. **Can't adjust lines**
   - Line adjustment only works in landscape mode
   - Rotate your device to landscape orientation
   - Look for the numbered handles on both sides of the screen

3. **App icon not visible**
   - Check your app drawer (swipe up from home screen)
   - Try searching for "Ruler App" in your device search
   - The icon shows 7 colored horizontal lines

4. **Can't interact with apps underneath**
   - This is normal - the overlay allows touch pass-through
   - You can use other apps while the ruler is visible
   - Only the handles and close button capture touches

## Technical Details

### Architecture
- **Service-based**: Uses a foreground service for reliable overlay display
- **Dual-view system**: Separate views for lines (non-interactive) and controls (interactive)
- **Orientation handling**: Proper configuration change management
- **State persistence**: SharedPreferences for storing line positions

### Compatibility
- Minimum SDK: 26 (Android 8.0 Oreo)
- Target SDK: 33 (Android 13 Tiramisu)
- Tested on Android 8.0 through Android 14

## Recent Updates

### Version 2.0 (Latest)
- Fixed orientation handling - lines now display correctly in both portrait and landscape
- Removed line ordering constraints - lines can now freely cross each other
- Improved touch handling - users can now interact with apps beneath the overlay
- Enhanced position persistence - positions save correctly when using the × close button
- Updated line colors - replaced bright yellow with gold for better visibility
- Fixed rotation crashes and service lifecycle issues
- Added proper launcher icon with 7 colored lines design

## Contributing

We welcome contributions! Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on how to submit pull requests, report issues, or suggest new features.

## License

This project is open source and available under the MIT License.
