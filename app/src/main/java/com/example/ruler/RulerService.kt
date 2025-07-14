package com.example.ruler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

class RulerService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "RulerServiceChannel"
        private const val TAG = "RulerService"
        private const val PREFS_NAME = "RulerPreferences"
        private const val LINE_POSITION_PREFIX = "line_position_"
        private const val NUM_LINES = 7
        private const val HANDLE_WIDTH = 60
        private const val HANDLE_HEIGHT = 40
        private const val LINE_WIDTH = 3
        private const val CLOSE_BUTTON_SIZE = 80
        private const val MIN_LINE_SPACING = 30
        private const val SCREEN_PADDING = 40f
        private const val CLOSE_BUTTON_MARGIN = 20
        
        var isServiceRunning = false
        
        private val LINE_COLORS = intArrayOf(
            Color.RED,
            0xFFFFA500.toInt(), // Orange
            0xFFFFD700.toInt(), // Gold (less bright than yellow)
            Color.GREEN,
            0xFF00BFFF.toInt(), // Deep Sky Blue
            0xFF8A2BE2.toInt(), // Blue Violet
            Color.MAGENTA
        )
    }

    private lateinit var windowManager: WindowManager
    private lateinit var rulerLinesView: RulerLinesView // View for non-interactive ruler lines
    private lateinit var controlsView: RulerControlsView // View for interactive controls
    private lateinit var sharedPreferences: SharedPreferences
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var linePositions = FloatArray(NUM_LINES) // Y positions of the lines (vertical position on screen)
    private var isLandscape = false
    private var currentOrientation: Int = Configuration.ORIENTATION_UNDEFINED
    
    // Non-interactive view that just draws the ruler lines
    private inner class RulerLinesView(context: Context) : View(context) {
        private val linePaint = Paint().apply {
            strokeWidth = LINE_WIDTH.toFloat()
            isAntiAlias = true
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            
            for (i in 0 until NUM_LINES) {
                linePaint.color = LINE_COLORS[i]
                if (isLandscape) {
                    // Landscape: horizontal lines (left to right)
                    canvas.drawLine(0f, linePositions[i], width.toFloat(), linePositions[i], linePaint)
                } else {
                    // Portrait: vertical lines (top to bottom)  
                    canvas.drawLine(linePositions[i], 0f, linePositions[i], height.toFloat(), linePaint)
                }
            }
        }
    }
    
    // Interactive view for handles and close button
    private inner class RulerControlsView(context: Context) : View(context) {
        private val handlePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        private val handleStrokePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        
        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 16 * resources.displayMetrics.density
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        private val closePaint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        private val closeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14 * resources.displayMetrics.density
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        private var closeButtonRect = Rect() // Initialize here directly
        private val leftHandleRects = Array(NUM_LINES) { Rect() } // Left handle rectangles
        private val rightHandleRects = Array(NUM_LINES) { Rect() } // Right handle rectangles
        
        private var activeHandleIndex = -1
        private var initialTouchPos = 0f // Stores Y position for vertical movement
        private var isMovingHandle = false
        private var isLeftHandle = true // Track which handle is being dragged
        
        private fun updateCloseButtonPosition() {
            closeButtonRect.set(
                width - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_MARGIN,
                height - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_MARGIN,
                width - CLOSE_BUTTON_MARGIN,
                height - CLOSE_BUTTON_MARGIN
            )
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            
            // Update close button position based on current view size
            updateCloseButtonPosition()
            
            // Only draw handles in landscape mode (for dragging)
            if (isLandscape) {
                // Draw handles for dragging (both left and right sides)
                for (i in 0 until NUM_LINES) {
                    val position = linePositions[i]
                    
                    // Left handle
                    val leftHandleLeft = 0
                    val leftHandleTop = position.toInt() - HANDLE_HEIGHT / 2
                    val leftHandleRight = HANDLE_WIDTH
                    val leftHandleBottom = leftHandleTop + HANDLE_HEIGHT
                    
                    leftHandleRects[i].set(leftHandleLeft, leftHandleTop, leftHandleRight, leftHandleBottom)
                    
                    // Right handle
                    val rightHandleLeft = width - HANDLE_WIDTH
                    val rightHandleTop = position.toInt() - HANDLE_HEIGHT / 2
                    val rightHandleRight = width
                    val rightHandleBottom = rightHandleTop + HANDLE_HEIGHT
                    
                    rightHandleRects[i].set(rightHandleLeft, rightHandleTop, rightHandleRight, rightHandleBottom)
                    
                    // Draw left handle with rounded corners
                    handlePaint.color = LINE_COLORS[i]
                    val leftRect = leftHandleRects[i]
                    canvas.drawRoundRect(leftRect.left.toFloat(), leftRect.top.toFloat(),
                                       leftRect.right.toFloat(), leftRect.bottom.toFloat(),
                                       8f, 8f, handlePaint)
                    canvas.drawRoundRect(leftRect.left.toFloat(), leftRect.top.toFloat(),
                                       leftRect.right.toFloat(), leftRect.bottom.toFloat(),
                                       8f, 8f, handleStrokePaint)
                    
                    // Draw right handle with rounded corners
                    val rightRect = rightHandleRects[i]
                    canvas.drawRoundRect(rightRect.left.toFloat(), rightRect.top.toFloat(),
                                       rightRect.right.toFloat(), rightRect.bottom.toFloat(),
                                       8f, 8f, handlePaint)
                    canvas.drawRoundRect(rightRect.left.toFloat(), rightRect.top.toFloat(),
                                       rightRect.right.toFloat(), rightRect.bottom.toFloat(),
                                       8f, 8f, handleStrokePaint)
                    
                    // Draw line numbers on both handles
                    canvas.drawText((i + 1).toString(), 
                                   leftHandleLeft + HANDLE_WIDTH / 2f, 
                                   leftHandleTop + HANDLE_HEIGHT / 2f + textPaint.textSize / 3, textPaint)
                    canvas.drawText((i + 1).toString(), 
                                   rightHandleLeft + HANDLE_WIDTH / 2f, 
                                   rightHandleTop + HANDLE_HEIGHT / 2f + textPaint.textSize / 3, textPaint)
                }
            }
            
            // Draw close button (always in bottom-right corner)
            canvas.drawOval(closeButtonRect.left.toFloat(), closeButtonRect.top.toFloat(), 
                           closeButtonRect.right.toFloat(), closeButtonRect.bottom.toFloat(), closePaint)
            canvas.drawText("×", closeButtonRect.exactCenterX(), 
                           closeButtonRect.exactCenterY() + closeTextPaint.textSize / 3, closeTextPaint)
        }
        
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Check if close button was tapped (always works, any orientation)
                    if (closeButtonRect.contains(x.toInt(), y.toInt())) {
                        // Save positions before stopping service
                        saveLinePositions()
                        // Close button tapped - stop service
                        stopSelf()
                        return true
                    }
                    
                    // Only allow handle dragging in landscape mode
                    if (!isLandscape) return false // Let touch pass through
                    
                    // Check if any left handle was tapped
                    for (i in 0 until NUM_LINES) {
                        if (leftHandleRects[i].contains(x.toInt(), y.toInt())) {
                            activeHandleIndex = i
                            initialTouchPos = y
                            isMovingHandle = true
                            isLeftHandle = true
                            return true
                        }
                    }
                    
                    // Check if any right handle was tapped
                    for (i in 0 until NUM_LINES) {
                        if (rightHandleRects[i].contains(x.toInt(), y.toInt())) {
                            activeHandleIndex = i
                            initialTouchPos = y
                            isMovingHandle = true
                            isLeftHandle = false
                            return true
                        }
                    }
                    
                    // If no UI element was touched, let the touch pass through
                    return false
                }
                
                MotionEvent.ACTION_MOVE -> {
                    if (isMovingHandle && activeHandleIndex != -1) {
                        // Calculate new line position
                        val deltaY = y - initialTouchPos
                        var newPosition = linePositions[activeHandleIndex] + deltaY
                        newPosition = newPosition.coerceIn(SCREEN_PADDING, height.toFloat() - SCREEN_PADDING)
                        // Allow lines to cross each other - no ordering constraints
                        
                        linePositions[activeHandleIndex] = newPosition
                        initialTouchPos = y
                        
                        invalidate()
                        rulerLinesView.invalidate()
                        
                        return true
                    }
                }
                
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isMovingHandle && activeHandleIndex != -1) {
                        // Save line positions when user releases
                        saveLinePositions()
                        activeHandleIndex = -1
                        isMovingHandle = false
                        return true
                    }
                }
            }
            
            return false // Let unhandled touches pass through
        }
        
        // Lines can now freely cross each other - no ordering constraints
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating ruler service")
        try {
            // Create notification and start foreground immediately
            setupNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            
            // Mark service as running
            isServiceRunning = true
            
            // Get window manager and display metrics for screen dimensions
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                metrics.widthPixels = bounds.width()
                metrics.heightPixels = bounds.height()
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(metrics)
            }
            
            // Get screen dimensions
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            
            // Determine current orientation
            currentOrientation = resources.configuration.orientation
            isLandscape = currentOrientation == Configuration.ORIENTATION_LANDSCAPE
            
            Log.d(TAG, "Screen dimensions: $screenWidth x $screenHeight, isLandscape: $isLandscape")
            
            // Get shared preferences for storing ruler positions
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Set up the overlay view
            setupOverlayView()
            
            // Restore line positions (or use defaults)
            restoreLinePositions()
            
            Log.d(TAG, "Ruler service created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during service creation", e)
            Toast.makeText(this, "Error creating ruler overlay: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }
    
    private fun setupOverlayView() {
        try {
            Log.d(TAG, "Setting up overlay views")
            
            // Get the appropriate overlay type based on Android version
            val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            
            // Create and add the non-interactive ruler lines view first
            rulerLinesView = RulerLinesView(this)
            
            // Create layout parameters for ruler lines
            val rulerLinesParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or // This view won't receive touches
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            
            // Position the ruler lines at 0,0
            rulerLinesParams.gravity = Gravity.TOP or Gravity.START
            
            // Add ruler lines view to window manager
            windowManager.addView(rulerLinesView, rulerLinesParams)
            Log.d(TAG, "Added ruler lines view")
            
            // Create and add interactive controls view
            controlsView = RulerControlsView(this)
            
            // Create layout parameters for controls - make it not block touches
            val controlsParams = WindowManager.LayoutParams(
                if (isLandscape) WindowManager.LayoutParams.MATCH_PARENT else CLOSE_BUTTON_SIZE + CLOSE_BUTTON_MARGIN * 2,
                if (isLandscape) WindowManager.LayoutParams.MATCH_PARENT else CLOSE_BUTTON_SIZE + CLOSE_BUTTON_MARGIN * 2,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            
            // Position based on orientation
            controlsParams.gravity = if (isLandscape) {
                Gravity.TOP or Gravity.START
            } else {
                Gravity.BOTTOM or Gravity.END  // Just close button in portrait
            }
            
            // Add controls view to window manager
            windowManager.addView(controlsView, controlsParams)
            Log.d(TAG, "Added controls view")
            
            // Trigger initial draw
            rulerLinesView.invalidate()
            controlsView.invalidate()
            
            Log.d(TAG, "Overlay setup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up overlay views", e)
            Toast.makeText(this, "Failed to create overlay: ${e.message}", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }
    
    private fun saveLinePositions() {
        try {
            Log.d(TAG, "Saving line positions to preferences")
            val editor = sharedPreferences.edit()
            
            for (i in 0 until NUM_LINES) {
                // Save position of each line
                editor.putFloat(LINE_POSITION_PREFIX + i, linePositions[i])
            }
            editor.apply()
            Log.d(TAG, "Saved line positions to preferences")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving line positions", e)
        }
    }

    private fun restoreLinePositions() {
        try {
            Log.d(TAG, "Restoring line positions. Screen width: $screenWidth, height: $screenHeight")

            for (i in 0 until NUM_LINES) {
                // Default positions depend on orientation
                val defaultPosition = if (isLandscape) {
                    // Landscape: evenly space across height
                    if (screenHeight > 0) {
                        SCREEN_PADDING + ((screenHeight - 2 * SCREEN_PADDING) / (NUM_LINES + 1)) * (i + 1)
                    } else {
                        100f + (i * 50f)
                    }
                } else {
                    // Portrait: evenly space across width
                    if (screenWidth > 0) {
                        SCREEN_PADDING + ((screenWidth - 2 * SCREEN_PADDING) / (NUM_LINES + 1)) * (i + 1)
                    } else {
                        100f + (i * 50f)
                    }
                }

                // Restore from preferences or use default
                val savedPosition = sharedPreferences.getFloat(LINE_POSITION_PREFIX + i, defaultPosition)
                
                // Ensure saved position is within bounds
                val maxDimension = if (isLandscape) screenHeight.toFloat() else screenWidth.toFloat()
                linePositions[i] = savedPosition.coerceIn(SCREEN_PADDING, maxDimension - SCREEN_PADDING)
                
                // If saved position is way out of bounds, use default
                if (savedPosition > maxDimension || savedPosition < 0) {
                    linePositions[i] = defaultPosition
                }
                
                Log.d(TAG, "Line $i position: ${linePositions[i]} (default: $defaultPosition, saved: $savedPosition)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring line positions, using defaults", e)
            
            // If restoration fails, set default positions
            val dimension = if (isLandscape) screenHeight.toFloat() else screenWidth.toFloat()
            for (i in 0 until NUM_LINES) {
                linePositions[i] = SCREEN_PADDING + ((dimension - 2 * SCREEN_PADDING) / (NUM_LINES + 1)) * (i + 1)
            }
        }
    }
    
    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Ruler Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for Ruler Service"
                setShowBadge(false) // No badge on app icon
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Line App Active")
            .setContentText("Lines are overlaid on your screen.")
            .setSmallIcon(R.drawable.ic_launcher) // Using available drawable
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true) // Makes the notification non-dismissible by swiping
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Ruler service started")
        return START_STICKY // If the service is killed, restart it
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed: orientation = ${newConfig.orientation}")
        
        val newOrientation = newConfig.orientation
        if (newOrientation != currentOrientation) {
            currentOrientation = newOrientation
            isLandscape = currentOrientation == Configuration.ORIENTATION_LANDSCAPE
            
            // Update screen dimensions
            val metrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                metrics.widthPixels = bounds.width()
                metrics.heightPixels = bounds.height()
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(metrics)
            }
            
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            
            Log.d(TAG, "New screen dimensions: $screenWidth x $screenHeight, isLandscape: $isLandscape")
            
            // Restore positions when orientation changes
            restoreLinePositions()
            
            // Force redraw and re-layout
            if (::rulerLinesView.isInitialized) {
                rulerLinesView.invalidate()
            }
            if (::controlsView.isInitialized) {
                // Re-create layout params for new orientation
                val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                
                val controlsParams = WindowManager.LayoutParams(
                    if (isLandscape) WindowManager.LayoutParams.MATCH_PARENT else CLOSE_BUTTON_SIZE + CLOSE_BUTTON_MARGIN * 2,
                    if (isLandscape) WindowManager.LayoutParams.MATCH_PARENT else CLOSE_BUTTON_SIZE + CLOSE_BUTTON_MARGIN * 2,
                    overlayType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                )
                
                controlsParams.gravity = if (isLandscape) {
                    Gravity.TOP or Gravity.START
                } else {
                    Gravity.BOTTOM or Gravity.END
                }
                
                // Update the window params
                windowManager.updateViewLayout(controlsView, controlsParams)
                controlsView.invalidate()
            }
        }
    }

    override fun onDestroy() {
        try {
            Log.d(TAG, "Destroying ruler service")
            isServiceRunning = false // Set flag to false
            
            // Save positions before shutting down (only if initialized)
            if (::sharedPreferences.isInitialized) {
                saveLinePositions()
            }
            
            // Remove both overlay views
            if (::windowManager.isInitialized) {
                if (::rulerLinesView.isInitialized && rulerLinesView.windowToken != null) {
                    windowManager.removeView(rulerLinesView)
                }
                if (::controlsView.isInitialized && controlsView.windowToken != null) {
                    windowManager.removeView(controlsView)
                }
                Log.d(TAG, "Removed overlay views")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during service destruction", e)
        } finally {
            super.onDestroy()
            Log.d(TAG, "RulerService onDestroy completed. isServiceRunning: $isServiceRunning")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
