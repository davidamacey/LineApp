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
import kotlin.math.min

class RulerService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "RulerServiceChannel"
        private const val TAG = "RulerService"
        private const val PREFS_NAME = "RulerPreferences"
        private const val LINE_POSITION_PREFIX = "line_position_" // More generic name for storing positions
        private const val NUM_LINES = 7
        private const val HANDLE_WIDTH = 60
        private const val HANDLE_HEIGHT = 40
        private const val LINE_WIDTH = 2
        private const val CLOSE_BUTTON_SIZE = 80
        var isServiceRunning = false // Flag to indicate service status
    }

    private lateinit var windowManager: WindowManager
    private lateinit var rulerLinesView: RulerLinesView // View for non-interactive ruler lines
    private lateinit var controlsView: RulerControlsView // View for interactive controls
    private lateinit var sharedPreferences: SharedPreferences
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var linePositions = FloatArray(NUM_LINES) // Y positions of the lines (vertical position on screen)
    private var isLandscape = false
    private var naturalWidth: Int = 0 // Natural width (shorter dimension)
    private var naturalHeight: Int = 0 // Natural height (longer dimension)
    private var currentOrientation: Int = Configuration.ORIENTATION_UNDEFINED
    
    // Non-interactive view that just draws the ruler lines
    private inner class RulerLinesView(context: Context) : View(context) {
        private val linePaint = Paint().apply { 
            color = Color.rgb(255, 165, 0) // Orange
            strokeWidth = LINE_WIDTH.toFloat()
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            
            // Draw horizontal ruler lines
            for (i in 0 until NUM_LINES) {
                val position = linePositions[i]
                
                // Draw horizontal lines (from left to right across the screen)
                canvas.drawLine(0f, position, width.toFloat(), position, linePaint)
            }
        }
    }
    
    // Interactive view for handles and close button
    private inner class RulerControlsView(context: Context) : View(context) {
        private val handlePaint = Paint().apply {
            color = Color.rgb(255, 165, 0) // Orange
            style = Paint.Style.FILL
        }
        
        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 16 * resources.displayMetrics.density
            textAlign = Paint.Align.CENTER
        }
        
        private val closePaint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
        }
        
        private val closeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14 * resources.displayMetrics.density
            textAlign = Paint.Align.CENTER
        }
        
        private lateinit var closeButtonRect: Rect
        private val handleRects = Array(NUM_LINES) { Rect() } // Array of handle rectangles for hit detection
        
        private var activeHandleIndex = -1
        private var initialTouchPos = 0f // Stores Y position for vertical movement
        private var isMovingHandle = false
        
        init {
            updateCloseButtonPosition()
        }
        
        fun updateCloseButtonPosition() {
            // Position close button in the bottom right corner
            val margin = 20
            closeButtonRect = Rect(
                screenWidth - CLOSE_BUTTON_SIZE - margin,
                screenHeight - CLOSE_BUTTON_SIZE - margin,
                screenWidth - margin,
                screenHeight - margin
            )
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            
            // Draw handles for dragging
            for (i in 0 until NUM_LINES) {
                val position = linePositions[i]
                
                // Calculate handle position for horizontal line
                val handleLeft = 0
                val handleTop = position.toInt() - HANDLE_HEIGHT / 2
                val handleRight = HANDLE_WIDTH
                val handleBottom = handleTop + HANDLE_HEIGHT
                
                // Store handle rect for hit testing
                handleRects[i].set(handleLeft, handleTop, handleRight, handleBottom)
                
                // Draw handle with number
                canvas.drawRect(handleRects[i], handlePaint)
                canvas.drawText((i + 1).toString(), handleLeft + HANDLE_WIDTH / 2f, 
                               handleTop + HANDLE_HEIGHT / 2f + textPaint.textSize / 3, textPaint)
            }
            
            // Draw close button (always in top-right corner)
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
                    // Check if close button was tapped
                    if (closeButtonRect.contains(x.toInt(), y.toInt())) {
                        // Close button tapped - stop service
                        stopSelf()
                        return true
                    }
                    
                    // Check if any handle was tapped
                    for (i in 0 until NUM_LINES) {
                        if (handleRects[i].contains(x.toInt(), y.toInt())) {
                            activeHandleIndex = i
                            initialTouchPos = y
                            isMovingHandle = true
                            return true
                        }
                    }
                }
                
                MotionEvent.ACTION_MOVE -> {
                    if (isMovingHandle && activeHandleIndex != -1) {
                        // Calculate new line position
                        val deltaY = y - initialTouchPos
                        
                        // Update line position (but keep it within screen bounds)
                        var newPosition = linePositions[activeHandleIndex] + deltaY
                        
                        // Constrain to screen bounds with padding
                        newPosition = newPosition.coerceIn(40f, height.toFloat() - 40f)
                        
                        // Only update if position changed
                        if (newPosition != linePositions[activeHandleIndex]) {
                            linePositions[activeHandleIndex] = newPosition
                            initialTouchPos = y
                            
                            // Redraw both views
                            invalidate()
                            rulerLinesView.invalidate()
                        }
                        
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
            
            return super.onTouchEvent(event)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating ruler service")
        try {
            // Mark service as running
            isServiceRunning = true
            
            // Get window manager and display metrics for screen dimensions
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = display ?: windowManager.defaultDisplay
                display.getRealMetrics(metrics)
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
            
            // Always use natural dimensions for consistent positioning
            naturalWidth = min(screenWidth, screenHeight)
            naturalHeight = max(screenWidth, screenHeight)
            
            // Get shared preferences for storing ruler positions
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Set up the overlay view
            setupOverlayView()
            
            // Restore line positions (or use defaults)
            restoreLinePositions()
            
            // Create a notification for foreground service
            setupNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            
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
            
            // Create layout parameters for controls
            val controlsParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or // Non-focusable but touchable
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            
            // Position the controls at 0,0
            controlsParams.gravity = Gravity.TOP or Gravity.START
            
            // Add controls view to window manager
            windowManager.addView(controlsView, controlsParams)
            Log.d(TAG, "Added controls view")
            
            // Initialize close button position in controls view
            controlsView.updateCloseButtonPosition()
            
            // Force an initial update to both views
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
            Log.d(TAG, "Restoring line positions. Screen height: $screenHeight")

            for (i in 0 until NUM_LINES) {
                // Default to evenly spaced positions
                val defaultPosition = if (screenHeight > 0) {
                    (screenHeight.toFloat() / (NUM_LINES + 1)) * (i + 1)
                } else {
                    100f + (i * 100f) // Fallback if screenHeight isn't available yet
                }

                // Restore from preferences or use default
                linePositions[i] = sharedPreferences.getFloat(LINE_POSITION_PREFIX + i, defaultPosition)
                
                Log.d(TAG, "Restored position for line $i: ${linePositions[i]} (default: $defaultPosition)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring line positions, using defaults", e)
            
            // If restoration fails, set default positions - evenly spaced vertically
            for (i in 0 until NUM_LINES) {
                linePositions[i] = (screenHeight.toFloat() / (NUM_LINES + 1)) * (i + 1)
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

    override fun onDestroy() {
        try {
            Log.d(TAG, "Destroying ruler service")
            isServiceRunning = false // Set flag to false
            
            // Save positions before shutting down
            saveLinePositions()
            
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
