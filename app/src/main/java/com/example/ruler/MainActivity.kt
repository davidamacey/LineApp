package com.example.ruler

import android.content.Intent
import android.content.SharedPreferences
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
        private const val APP_PREFS_NAME = "app_prefs" // Shared preferences file name
        private const val SERVICE_RUNNING_KEY = "service_running_status" // Key for service status
    }

    private lateinit var startRulerButton: Button
    private lateinit var stopRulerButton: Button
    private lateinit var instructionsTextView: TextView
    private lateinit var appPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "Creating MainActivity")
            setContentView(R.layout.activity_main)

            startRulerButton = findViewById(R.id.startRulerButton)
            stopRulerButton = findViewById(R.id.stopRulerButton)
            instructionsTextView = findViewById(R.id.instructionsTextView)
            appPrefs = getSharedPreferences(APP_PREFS_NAME, MODE_PRIVATE)

            // updateUiStates() will be called in onResume, which is always called after onCreate

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

            if (appPrefs.getBoolean("first_launch", true)) {
                showFirstTimeInstructions()
                appPrefs.edit().putBoolean("first_launch", false).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during MainActivity creation", e)
            Toast.makeText(this, "An error occurred while setting up the app", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_permission_title))
            .setMessage(getString(R.string.dialog_permission_message))
            .setPositiveButton(getString(R.string.dialog_permission_positive_button)) { _, _ -> requestOverlayPermission() }
            .setNegativeButton(getString(R.string.dialog_permission_negative_button), null)
            .show()
    }

    private fun requestOverlayPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Requesting overlay permission")
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
                // Toast moved to onActivityResult or handled by system
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting overlay permission", e)
            Toast.makeText(this, "Could not open permission settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUiStates() {
        val hasPermission = checkOverlayPermission()
        val isServiceExpectedToRun = appPrefs.getBoolean(SERVICE_RUNNING_KEY, false)

        Log.d(TAG, "updateUiStates: hasPermission=$hasPermission, isServiceExpectedToRun=$isServiceExpectedToRun")

        if (!hasPermission) {
            instructionsTextView.text = getString(R.string.permission_required_instruction)
            startRulerButton.text = getString(R.string.grant_permission_button) // "Grant Permission"
            startRulerButton.isEnabled = true
            startRulerButton.visibility = View.VISIBLE
            stopRulerButton.visibility = View.GONE
        } else { // Has permission
            instructionsTextView.text = getString(R.string.ruler_usage_instruction_7_lines)
            if (isServiceExpectedToRun) {
                startRulerButton.visibility = View.GONE
                stopRulerButton.visibility = View.VISIBLE
                stopRulerButton.isEnabled = true
                // stopRulerButton.text = getString(R.string.stop_service_button) // Optional: if you want to set text
            } else {
                startRulerButton.text = getString(R.string.start_service_button) // "Start Ruler"
                startRulerButton.isEnabled = true
                startRulerButton.visibility = View.VISIBLE
                stopRulerButton.visibility = View.GONE
            }
        }
    }

    private fun showFirstTimeInstructions() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_first_time_title))
            .setMessage(getString(R.string.dialog_first_time_message))
            .setPositiveButton(getString(R.string.dialog_first_time_positive_button), null)
            .show()
    }

    private fun startRulerService() {
        if (!checkOverlayPermission()) {
            showPermissionExplanationDialog()
            return
        }
        try {
            Log.d(TAG, "Starting ruler service")
            val intent = Intent(this, RulerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Ruler service started", Toast.LENGTH_SHORT).show()
            appPrefs.edit().putBoolean(SERVICE_RUNNING_KEY, true).apply()
            updateUiStates()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ruler service", e)
            Toast.makeText(this, "Failed to start ruler: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRulerService() {
        try {
            Log.d(TAG, "Stopping ruler service")
            val intent = Intent(this, RulerService::class.java)
            stopService(intent)
            Toast.makeText(this, "Ruler service stopped", Toast.LENGTH_SHORT).show()
            appPrefs.edit().putBoolean(SERVICE_RUNNING_KEY, false).apply()
            updateUiStates()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop ruler service", e)
            Toast.makeText(this, "Failed to stop ruler: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        // Check if service should be running and restart if necessary
        val shouldBeRunning = appPrefs.getBoolean(SERVICE_RUNNING_KEY, false)
        val hasPermission = checkOverlayPermission()
        Log.d(TAG, "onResume: shouldBeRunning=$shouldBeRunning, hasPermission=$hasPermission, RulerService.isServiceRunning=${RulerService.isServiceRunning}")

        if (hasPermission && shouldBeRunning && !RulerService.isServiceRunning) {
            Log.i(TAG, "Service was expected to run but isn't. Restarting RulerService.")
            startRulerService() // This will also update UI states
        } else {
            updateUiStates() // Standard UI update
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                Log.d(TAG, "Overlay permission granted onActivityResult")
                Toast.makeText(this, "Overlay permission granted.", Toast.LENGTH_SHORT).show()
                startRulerService() // Proceed to start service now that permission is granted
            } else {
                Log.d(TAG, "Overlay permission denied onActivityResult")
                appPrefs.edit().putBoolean(SERVICE_RUNNING_KEY, false).apply()
                updateUiStates()
                Toast.makeText(this, "Permission denied. Ruler cannot function.", Toast.LENGTH_LONG).show()
                // Optionally, show a more persistent dialog explaining the consequences
                 AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_permission_denied_title))
                    .setMessage(getString(R.string.dialog_permission_denied_message))
                    .setPositiveButton(getString(R.string.dialog_permission_denied_positive_button)) { _, _ -> requestOverlayPermission() }
                    .setNegativeButton(getString(R.string.dialog_permission_negative_button), null)
                    .show()
            }
        }
    }
}

// Note: Ensure you have the following string resources in your strings.xml:
// <string name="permission_required_instruction">Permission required: This app needs permission to display over other apps.</string>
// <string name="grant_permission_button">Grant Permission</string>
// <string name="ruler_usage_instruction_7_lines">This app displays 7 adjustable vertical ruler lines. Use the orange handles to drag them. Close via the overlay\'s \'Close\' button or here.</string>
// <string name="start_service_button">Start Ruler</string>
// <string name="stop_service_button">Stop Ruler</string> // Optional, if you set text on stop button
// <string name="overlay_permission_required">Overlay permission is required to display the ruler. Please enable it in settings.</string> // Already used
