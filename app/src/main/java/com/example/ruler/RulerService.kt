package com.example.ruler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat

class RulerService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "RulerServiceChannel"
        private const val TAG = "RulerService"
        private const val PREFS_NAME = "RulerPreferences"
        private const val LINE_POSITION_PREFIX = "line_position_"
    }
    
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var sharedPreferences: SharedPreferences
    
    // Arrays to store references to our ruler lines and handles
    private lateinit var lines: Array<View>
    private lateinit var handles: Array<View>
    private lateinit var labels: Array<View>
    private lateinit var initialY: IntArray
    
    override fun onCreate() {
        super.onCreate()
        try {
            Log.d(TAG, "Creating ruler service")
            // Initialize shared preferences
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            setupNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            setupOverlayView()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating ruler service", e)
            Toast.makeText(this, "Failed to start ruler: ${e.message}", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }
    
    private fun setupOverlayView() {
        try {
            Log.d(TAG, "Setting up overlay view")
            // Inflate the overlay layout
            overlayView = LayoutInflater.from(this).inflate(R.layout.ruler_overlay_layout, null)
            
            // Setup window parameters for overlay
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else 
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.START
            
            // Initialize references to lines, handles, and labels
            lines = Array(7) { i ->
                overlayView.findViewById<View>(resources.getIdentifier("line${i+1}", "id", packageName))
            }
            
            handles = Array(7) { i ->
                overlayView.findViewById<View>(resources.getIdentifier("line${i+1}_handle", "id", packageName))
            }
            
            labels = Array(7) { i ->
                overlayView.findViewById<View>(resources.getIdentifier("line${i+1}_label", "id", packageName))
            }
            
            initialY = IntArray(7)
            
            // Restore saved positions from SharedPreferences
            restoreLinePositions()
            
            // Set up touch listeners for handles
            setupHandleTouchListeners()
            
            // Setup close button
            val closeButton = overlayView.findViewById<Button>(R.id.closeButton)
            closeButton.setOnClickListener {
                // Save positions before closing
                saveLinePositions()
                stopSelf()
            }
            
            // Add the view to the window
            windowManager.addView(overlayView, params)
            Log.d(TAG, "Overlay view added to window")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up overlay view", e)
            Toast.makeText(this, "Failed to setup ruler: ${e.message}", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }
    
    private fun setupHandleTouchListeners() {
        // Set touch listeners for each handle to adjust line position
        for (i in handles.indices) {
            handles[i].setOnTouchListener(createTouchListener(i))
        }
    }
    
    private fun createTouchListener(lineIndex: Int): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var initialTouchY = 0f
            private var initialLineY = 0
            
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Capture initial touch position and line position
                        initialTouchY = event.rawY
                        initialLineY = lines[lineIndex].y.toInt()
                    }
                    
                    MotionEvent.ACTION_MOVE -> {
                        // Calculate the distance moved
                        val deltaY = event.rawY - initialTouchY
                        
                        // Update the position of the line
                        val newY = (initialLineY + deltaY).toInt()
                        lines[lineIndex].y = newY.toFloat()
                        handles[lineIndex].y = newY - 15f // Offset to align handle with line
                        labels[lineIndex].y = newY - 15f // Update label position directly from array
                    }
                    
                    MotionEvent.ACTION_UP -> {
                        // Save positions when user stops dragging
                        saveLinePositions()
                    }
                }
                return true
            }
        }
    }
    
    /**
     * Saves the current positions of all ruler lines to SharedPreferences
     */
    private fun saveLinePositions() {
        try {
            val editor = sharedPreferences.edit()
            for (i in lines.indices) {
                val position = lines[i].y.toInt()
                editor.putInt(LINE_POSITION_PREFIX + i, position)
                Log.d(TAG, "Saving position for line $i: $position")
            }
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving line positions", e)
        }
    }
    
    /**
     * Restores saved line positions from SharedPreferences
     */
    private fun restoreLinePositions() {
        try {
            for (i in lines.indices) {
                // Default positions are based on the layout XML values
                val defaultPosition = 50 + (i * 50) // Default spacing between lines
                val savedPosition = sharedPreferences.getInt(LINE_POSITION_PREFIX + i, defaultPosition)
                
                // Apply saved position to views
                lines[i].y = savedPosition.toFloat()
                handles[i].y = savedPosition - 15f // Offset to align handle with line
                labels[i].y = savedPosition - 15f
                
                Log.d(TAG, "Restored position for line $i: $savedPosition")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring line positions, using defaults", e)
            // If restoration fails, lines will use their default positions from the layout XML
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
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Ruler App Active")
            .setContentText("Tap to return to the app")
            .setSmallIcon(R.drawable.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onDestroy() {
        try {
            Log.d(TAG, "Destroying ruler service")
            // Save line positions before destroying
            if (::lines.isInitialized) {
                saveLinePositions()
            }
            
            // Remove the overlay view
            if (::windowManager.isInitialized && ::overlayView.isInitialized) {
                windowManager.removeView(overlayView)
                Log.d(TAG, "Removed overlay view")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during service destruction", e)
        } finally {
            super.onDestroy()
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
