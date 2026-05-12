package com.example.sadaguessgame.service;

/*
 * ──────────────────────────────────────────────────────────────────────────────
 *  SadaGuessFirebaseMessagingService — Feature 6: FCM Push Notifications
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *  HOW TO ACTIVATE THIS SERVICE
 *  ─────────────────────────────
 *  1. Add Firebase to your project:
 *       • In Android Studio: Tools → Firebase → Cloud Messaging → Set up
 *       • Or manually add google-services.json and the plugin to build.gradle
 *
 *  2. Add to app/build.gradle dependencies:
 *       implementation 'com.google.firebase:firebase-messaging:24.0.0'
 *
 *  3. Un-comment the import and class body below.
 *
 *  4. Un-comment the <service> block in AndroidManifest.xml.
 *
 *  Until step 1-3 are done this file compiles without any Firebase dependency
 *  (the class body is a safe stub so you don't get build errors).
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *  EXPECTED FCM DATA PAYLOAD FORMAT (from your server / Firebase console):
 *
 *  {
 *    "to": "<device-fcm-token>",
 *    "data": {
 *      "type":  "update",          // "update" | "news" | "promo" | "streak"
 *      "title": "v2.1 is here!",
 *      "body":  "New word packs and theme colours added."
 *    }
 *  }
 *
 *  Notification-type messages (with "notification" key) are handled by the
 *  Firebase SDK automatically and displayed by the system tray.
 *  Data-only messages (with only "data" key) reach this service even when
 *  the app is in the background, giving you full control over the UI.
 * ──────────────────────────────────────────────────────────────────────────────
 */

// UNCOMMENT when Firebase Messaging SDK is added:
// import com.google.firebase.messaging.FirebaseMessagingService;
// import com.google.firebase.messaging.RemoteMessage;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * STUB — replace with real FirebaseMessagingService once the SDK is added.
 *
 * This plain Service subclass keeps the project compiling before Firebase
 * is integrated.  No logic runs from this stub; it is simply a placeholder
 * so the manifest entry and all references compile cleanly.
 *
 * WHEN YOU ADD FIREBASE:
 *   1. Change "extends Service" → "extends FirebaseMessagingService"
 *   2. Delete the stub onBind() override.
 *   3. Un-comment onMessageReceived() and onNewToken().
 */
public class SadaGuessFirebaseMessagingService extends Service {

    private static final String TAG = "SadaFCMService";

    // ─── STUB (delete when extending FirebaseMessagingService) ────────────────

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    // ─── REAL IMPLEMENTATION (un-comment after adding Firebase SDK) ───────────

    /*
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Only handle data messages here; notification messages are auto-shown
        if (remoteMessage.getData().isEmpty()) return;

        String type  = remoteMessage.getData().getOrDefault("type",  "news");
        String title = remoteMessage.getData().getOrDefault("title", getString(R.string.app_name));
        String body  = remoteMessage.getData().getOrDefault("body",  "");

        Log.d(TAG, "FCM data message received — type=" + type);

        AppNotificationManager.getInstance(getApplicationContext())
                .dispatchPushPayload(type, title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // TODO: send the refreshed token to your server so it can target this device
        Log.d(TAG, "FCM registration token refreshed: " + token);
        // Example: MyApiClient.sendFcmTokenToServer(token);
    }
    */
}