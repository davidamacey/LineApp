package com.example.ruler

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1234
        private const val TAG = "MainActivity"
    }
    
    private lateinit var startRulerButton: Button
    private lateinit var stopRulerButton: Button
    
    private lateinit var instructionsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "Creating MainActivity")
            setContentView(R.layout.activity_main)
            
            startRulerButton = findViewById(R.id.startRulerButton)
            stopRulerButton = findViewById(R.id.stopRulerButton)
            instructionsTextView = findViewById(R.id.instructionsTextView)
            
            // Set initial button states based on permission
            updateButtonStates()
            
            startRulerButton.setOnClickListener {
                if (checkOverlayPermission()) {
                    startRulerService()
                } else {
                    showPermissionExplanationDialog()
                }
            }
            
            stopRulerButton.setOnClickListener {
                stopRulerService()
            }
            
            // Show first-time usage instructions if needed
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            if (prefs.getBoolean("first_launch", true)) {
                showFirstTimeInstructions()
                prefs.edit().putBoolean("first_launch", false).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during MainActivity creation", e)
            Toast.makeText(this, "An error occurred while setting up the app", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Checks if the overlay permission is granted
     */
    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true // Before Android M, the permission is granted during app installation
        }
    }
    
    /**
     * Shows a dialog explaining why the overlay permission is needed
     * and provides guidance on how to enable it
     */
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs permission to display ruler lines over other apps. " +
                    "Without this permission, the ruler function cannot work.")
            .setPositiveButton("Grant Permission") { _, _ -> requestOverlayPermission() }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Requests the overlay permission by directing the user to system settings
     */
    private fun requestOverlayPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Requesting overlay permission")
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
                Toast.makeText(
                    this,
                    getString(R.string.overlay_permission_required),
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting overlay permission", e)
            Toast.makeText(this, "Could not open permission settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Updates the button states based on permission status
     * and provides visual feedback to the user
     */
    private fun updateButtonStates() {
        val hasPermission = checkOverlayPermission()
        startRulerButton.isEnabled = true
        stopRulerButton.isEnabled = true
        
        // Update instruction text based on permission status
        if (!hasPermission) {
            instructionsTextView.text = "Permission required: This app needs permission to display over other apps"
            startRulerButton.text = getString(R.string.grant_permission)
        } else {
            instructionsTextView.text = "This app displays 7 adjustable ruler lines that can be used over other apps"
            startRulerButton.text = getString(R.string.start_service)
        }
    }
    
    /**
     * Shows first-time usage instructions to help users understand how to use the app
     */
    private fun showFirstTimeInstructions() {
        AlertDialog.Builder(this)
            .setTitle("Welcome to Ruler App")
            .setMessage("This app helps you measure items on your screen with 7 adjustable lines. \n\n" +
                    "1. Tap 'Start Ruler' to begin\n" +
                    "2. Grant permission when prompted\n" +
                    "3. Use the circular handles to adjust each line\n" +
                    "4. Switch to any other app - the ruler stays visible\n" +
                    "5. Tap the Close button or return here to stop")
            .setPositiveButton("Got it", null)
            .show()
    }
    
    /**
     * Starts the ruler service with appropriate error handling
     */
    private fun startRulerService() {
        try {
            Log.d(TAG, "Starting ruler service")
            val intent = Intent(this, RulerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Ruler started", Toast.LENGTH_SHORT).show()
            
            // Update UI to reflect service state
            stopRulerButton.visibility = View.VISIBLE
            startRulerButton.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ruler service", e)
            Toast.makeText(this, "Failed to start ruler: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Stops the ruler service with appropriate error handling
     */
    private fun stopRulerService() {
        try {
            Log.d(TAG, "Stopping ruler service")
            val intent = Intent(this, RulerService::class.java)
            stopService(intent)
            Toast.makeText(this, "Ruler stopped", Toast.LENGTH_SHORT).show()
            
            // Update UI to reflect service state
            stopRulerButton.visibility = View.GONE
            startRulerButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop ruler service", e)
            Toast.makeText(this, "Failed to stop ruler: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Handles the result of permission request
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                Log.d(TAG, "Overlay permission granted")
                startRulerService()
            } else {
                Log.d(TAG, "Overlay permission denied")
                Toast.makeText(
                    this,
                    "Permission denied. Cannot show ruler without overlay permission.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Show helpful dialog about why permission is needed
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("The ruler cannot function without the overlay permission. " +
                               "Would you like to try again?")
                    .setPositiveButton("Try Again") { _, _ -> requestOverlayPermission() }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    /**
     * Updates UI when activity resumes
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumed")
        // Update button states based on permission status
        updateButtonStates()
    }
}
