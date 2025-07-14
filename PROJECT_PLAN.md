# Android Overlay Ruler App - Complete Project Documentation

## Project Overview

This Android application provides 7 adjustable horizontal ruler lines that overlay on top of other apps in landscape mode. Users can drag the lines to measure items on their screen, with persistent positioning between sessions.

## Key Features Implemented

### 1. Always Visible Lines with Landscape-Only Adjustment
- **Always Visible**: Lines remain visible in both portrait and landscape orientations
- **Position Preservation**: Line positions stay exactly the same when rotating device
- **Landscape-Only Adjustment**: Drag handles only appear and work in landscape mode
- **Configuration Change Handling**: Properly handles orientation changes during use

### 2. 7 Distinct Colored Lines
- **Color System**: Each line has a unique, high-contrast color:
  - Line 1: Red (255, 0, 0)
  - Line 2: Orange (255, 165, 0)
  - Line 3: Yellow (255, 255, 0)
  - Line 4: Green (0, 255, 0)
  - Line 5: Deep Sky Blue (0, 191, 255)
  - Line 6: Blue Violet (138, 43, 226)
  - Line 7: Magenta (255, 0, 255)

### 3. Line Ordering Constraints
- **Hierarchical Positioning**: Line N cannot be positioned above Line N-1
- **Minimum Spacing**: 30px minimum spacing between adjacent lines
- **Smooth Dragging**: Constraints applied in real-time during drag operations

### 4. Dual Handle System
- **Left and Right Handles**: Handles on both sides of screen for easier access
- **Synchronized Movement**: Both handles move the same line
- **Visual Design**: Rounded rectangles with white border and line number

### 5. Persistent Positioning
- **SharedPreferences Storage**: Line positions saved automatically
- **Session Restoration**: Positions restored when app restarts
- **Real-time Saving**: Positions saved immediately after drag completion

## Technical Architecture

### Core Components

#### 1. MainActivity (`MainActivity.kt`)
- **Permission Management**: Handles SYSTEM_ALERT_WINDOW permission requests
- **Service Lifecycle**: Manages RulerService start/stop operations
- **UI State Management**: Updates button states based on permissions and service status
- **First Launch Experience**: Shows helpful instructions on first use

#### 2. RulerService (`RulerService.kt`)
- **Foreground Service**: Ensures overlay persistence
- **Dual View System**: Separates interactive controls from line rendering
- **Orientation Handling**: Monitors configuration changes
- **Window Management**: Creates and manages overlay windows

#### 3. RulerLinesView (Inner Class)
- **Non-interactive Rendering**: Draws only the ruler lines
- **Color-coded Lines**: Each line uses its designated color
- **Performance Optimized**: Minimal drawing operations

#### 4. RulerControlsView (Inner Class)
- **Touch Handling**: Manages drag operations and close button
- **Handle Rendering**: Draws numbered handles on both sides
- **Constraint Application**: Enforces line ordering rules
- **Visual Feedback**: Provides immediate response to user input

### Key Technical Decisions

#### Overlay Window Configuration
```kotlin
WindowManager.LayoutParams(
    WindowManager.LayoutParams.MATCH_PARENT,
    WindowManager.LayoutParams.MATCH_PARENT,
    TYPE_APPLICATION_OVERLAY, // Android 8+ compatibility
    FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_IN_SCREEN,
    PixelFormat.TRANSLUCENT
)
```

#### Permission Requirements
- **SYSTEM_ALERT_WINDOW**: Required for overlay functionality
- **FOREGROUND_SERVICE**: Ensures service persistence
- **FOREGROUND_SERVICE_SPECIAL_USE**: Android 14+ requirement

#### State Management
- **Line Positions**: Stored as Float array in SharedPreferences
- **Service Status**: Tracked via static boolean and SharedPreferences
- **Orientation State**: Monitored via Configuration.orientation

## Installation and Build Process

### Prerequisites
- Android Studio 4.0 or later
- Android SDK with minimum API 26 (Android 8.0)
- Target SDK 33 (Android 13)
- Kotlin 1.8.0

### Build Commands
```bash
# Clean project
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Sideloading Instructions
1. Enable "Developer Options" on target device
2. Enable "Install unknown apps" for your file manager
3. Transfer APK to device via USB/cloud storage
4. Navigate to APK file and install
5. Grant overlay permission when prompted

## User Workflow

### Initial Setup
1. **Install App**: Sideload APK to Android device
2. **Launch App**: Open "Ruler App" from app drawer
3. **Grant Permission**: Tap "Grant Permission" and enable overlay
4. **Start Service**: Tap "Start Ruler" to activate overlay

### Daily Usage
1. **Activate Lines**: Start ruler service from main app
2. **Rotate Device**: Switch to landscape mode to see lines
3. **Position Lines**: Drag numbered handles to desired positions
4. **Use Other Apps**: Switch to measurement/reference apps
5. **Close When Done**: Tap × button or stop from main app

### Line Adjustment
- **Left/Right Handles**: Drag either handle to move a line
- **Ordering Constraints**: Lines maintain proper order automatically
- **Visual Feedback**: Handles show line numbers and colors
- **Persistence**: Positions saved automatically

## Known Limitations

### Device Compatibility
- **Minimum Android 8.0**: Uses modern overlay APIs
- **Landscape Only**: No support for portrait mode measurements
- **Screen Size**: Optimized for typical phone/tablet dimensions

### Performance Considerations
- **Battery Usage**: Foreground service may impact battery life
- **Memory Usage**: Minimal overhead with efficient drawing
- **Touch Latency**: Immediate response to drag operations

### Feature Limitations
- **7 Lines Maximum**: Fixed number of measurement lines
- **Horizontal Lines Only**: No vertical line support
- **No Export**: No screenshot or measurement export features

## Future Enhancement Opportunities

### Potential Features
1. **Variable Line Count**: Allow users to choose 3-10 lines
2. **Portrait Mode Support**: Add vertical lines for portrait use
3. **Measurement Display**: Show pixel distances between lines
4. **Export Functionality**: Screenshot with measurements overlay
5. **Color Customization**: Let users choose line colors
6. **Grid Mode**: Option for grid instead of just lines

### Technical Improvements
1. **Performance Optimization**: Further reduce battery usage
2. **Accessibility Support**: Add TalkBack and other accessibility features
3. **Tablet Optimization**: Enhanced UI for larger screens
4. **Android 14+ Compatibility**: Update for latest platform features

## Troubleshooting

### Common Issues

#### Overlay Not Appearing
- **Check Permissions**: Ensure SYSTEM_ALERT_WINDOW is granted
- **Verify Orientation**: Lines only show in landscape mode
- **Restart Service**: Stop and start ruler service

#### Lines Not Moving
- **Touch Targets**: Ensure touching handle areas (left/right edges)
- **Ordering Constraints**: Lines cannot cross other lines
- **Service Status**: Verify ruler service is running

#### Performance Issues
- **Close Other Apps**: Free up device memory
- **Restart Service**: Stop and restart ruler service
- **Device Reboot**: Restart device if issues persist

## Development Notes

### Code Organization
- **Single Activity**: MainActivity handles all user interactions
- **Service-Based**: Core functionality in background service
- **Inner Classes**: View classes nested in service for tight coupling
- **Separation of Concerns**: Drawing separate from touch handling

### Testing Strategy
- **Manual Testing**: Test on multiple devices and orientations
- **Permission Testing**: Verify behavior with/without permissions
- **Configuration Testing**: Test orientation changes thoroughly
- **Persistence Testing**: Verify position saving/loading

### Maintenance Considerations
- **Android Updates**: Monitor for overlay API changes
- **Permission Changes**: Watch for new permission requirements
- **Performance Monitoring**: Track battery and memory usage
- **User Feedback**: Monitor for common usage patterns

## Release Checklist

### Pre-Release Testing
- [ ] Test on Android 8.0 through 14
- [ ] Verify permission flows work correctly
- [ ] Test orientation changes in all scenarios
- [ ] Confirm line ordering constraints work
- [ ] Validate position persistence
- [ ] Test with various other apps running

### Build Configuration
- [ ] Update version code and name
- [ ] Enable ProGuard for release builds
- [ ] Sign APK with release keystore
- [ ] Test signed APK installation
- [ ] Verify file size is reasonable

### Documentation
- [ ] Update CLAUDE.md with any changes
- [ ] Create user guide if needed
- [ ] Document known issues
- [ ] Prepare release notes

This implementation provides a robust, user-friendly overlay ruler app that meets all specified requirements while maintaining excellent performance and reliability.