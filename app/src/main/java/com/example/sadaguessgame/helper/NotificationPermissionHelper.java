package com.example.sadaguessgame.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sadaguessgame.R;

/**
 * NotificationPermissionHelper — Feature 6.
 *
 * Android 13 (API 33) introduced the POST_NOTIFICATIONS runtime permission.
 * This helper wraps the request flow with a rationale dialog so the user
 * understands why the app is asking before the system dialog appears.
 *
 * Usage (from MainActivity.onCreate or SplashActivity):
 *
 *   NotificationPermissionHelper.requestIfNeeded(this, REQUEST_CODE_NOTIF);
 *
 * Then handle the result in onRequestPermissionsResult:
 *
 *   NotificationPermissionHelper.onRequestPermissionsResult(requestCode, grantResults);
 */
public class NotificationPermissionHelper {

    public static final int REQUEST_CODE = 9001;

    /**
     * Checks whether POST_NOTIFICATIONS is granted and, if not, shows a
     * rationale dialog then requests the permission.
     *
     * Safe to call on all API levels — does nothing below API 33.
     */
    public static void requestIfNeeded(@NonNull Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return; // API < 33, no-op

        String permission = Manifest.permission.POST_NOTIFICATIONS;

        if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return; // Already granted
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // User previously denied — show rationale
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.notif_permission_rationale_title)
                    .setMessage(R.string.notif_permission_rationale_body)
                    .setPositiveButton(R.string.notif_permission_allow, (d, w) ->
                            ActivityCompat.requestPermissions(
                                    activity,
                                    new String[]{permission},
                                    requestCode))
                    .setNegativeButton(R.string.notif_permission_deny, null)
                    .show();
        } else {
            // First time — request directly
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{permission},
                    requestCode);
        }
    }

    /**
     * Call from Activity.onRequestPermissionsResult to log the outcome.
     * Returns true if the permission was granted.
     */
    public static boolean onRequestPermissionsResult(int requestCode,
                                                     @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE) return false;
        return grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}