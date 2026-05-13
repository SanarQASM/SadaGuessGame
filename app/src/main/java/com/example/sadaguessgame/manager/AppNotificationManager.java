package com.example.sadaguessgame.manager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sadaguessgame.R;

/**
 * AppNotificationManager — Feature 6: Centralized notification dispatch.
 *
 * Three notification channels:
 *   CHANNEL_GAME  — in-app game events (streak milestones, combo alerts)
 *   CHANNEL_NEWS  — news / updates / what's new (posted by the developer)
 *   CHANNEL_PROMO — optional promotional messages
 *
 * Usage:
 *   AppNotificationManager.getInstance(ctx).notifyStreakMilestone(5);
 *   AppNotificationManager.getInstance(ctx).notifyAppUpdate("v2.1 is here!");
 *
 * To send push notifications from your server connect Firebase Cloud Messaging
 * (FCM) and route payloads through dispatchPushPayload().
 */
public class AppNotificationManager {

    // ─── Channel IDs (must be stable across app updates) ─────────────────────
    public static final String CHANNEL_GAME  = "sada_game_events";
    public static final String CHANNEL_NEWS  = "sada_news_updates";
    public static final String CHANNEL_PROMO = "sada_promotions";

    // ─── Notification IDs ─────────────────────────────────────────────────────
    private static final int ID_STREAK_MILESTONE = 1001;
    private static final int ID_COMBO_RECORD     = 1002;
    private static final int ID_APP_UPDATE       = 2001;
    private static final int ID_NEWS             = 2002;
    private static final int ID_PROMO            = 3001;

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static volatile AppNotificationManager instance;

    public static AppNotificationManager getInstance(@NonNull Context ctx) {
        if (instance == null) {
            synchronized (AppNotificationManager.class) {
                if (instance == null)
                    instance = new AppNotificationManager(ctx.getApplicationContext());
            }
        }
        return instance;
    }

    private final Context                 appContext;
    private final NotificationManagerCompat notifMgr;

    private AppNotificationManager(@NonNull Context ctx) {
        appContext = ctx;
        notifMgr  = NotificationManagerCompat.from(ctx);
        createChannels();
    }

    // ─── Channel creation (idempotent) ────────────────────────────────────────

    private void createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = (NotificationManager)
                appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL_GAME,
                appContext.getString(R.string.notif_channel_game_name),
                NotificationManager.IMPORTANCE_DEFAULT));

        nm.createNotificationChannel(new NotificationChannel(
                CHANNEL_NEWS,
                appContext.getString(R.string.notif_channel_news_name),
                NotificationManager.IMPORTANCE_HIGH));

        NotificationChannel promo = new NotificationChannel(
                CHANNEL_PROMO,
                appContext.getString(R.string.notif_channel_promo_name),
                NotificationManager.IMPORTANCE_LOW);
        promo.setDescription(
                appContext.getString(R.string.notif_channel_promo_desc));
        nm.createNotificationChannel(promo);
    }

    // ─── Game event notifications ─────────────────────────────────────────────

    /**
     * Notifies the user they reached a streak milestone in the background
     * (e.g. if they minimise the app during a game session).
     *
     * @param streak The current streak count.
     */
    public void notifyStreakMilestone(int streak) {
        if (!channelEnabled(CHANNEL_GAME)) return;

        String title = appContext.getString(R.string.notif_streak_title);
        String body  = appContext.getString(R.string.notif_streak_body, streak);

        post(CHANNEL_GAME, ID_STREAK_MILESTONE, title, body,
                buildMainActivityIntent());
    }

    /**
     * Notify after a new personal-best combo multiplier is achieved.
     *
     * @param multiplier The multiplier, e.g. 2.0f for a x2 combo.
     */
    public void notifyComboRecord(float multiplier) {
        if (!channelEnabled(CHANNEL_GAME)) return;

        String title = appContext.getString(R.string.notif_combo_title);
        String body = appContext.getString(R.string.notif_combo_body)
                + " " + String.format("%.1f", multiplier);

        post(CHANNEL_GAME, ID_COMBO_RECORD, title, body, buildMainActivityIntent());
    }

    // ─── News / update notifications ─────────────────────────────────────────

    /**
     * Dispatch a "what's new" notification.  Typically called from an FCM
     * message handler after the developer pushes an update announcement.
     *
     * @param headline Short headline text (≤60 chars recommended).
     * @param detail   Optional longer detail body.
     */
    public void notifyAppUpdate(@NonNull String headline, @NonNull String detail) {
        String title = appContext.getString(R.string.notif_update_title);
        post(CHANNEL_NEWS, ID_APP_UPDATE, title,
                headline + (detail.isEmpty() ? "" : "\n" + detail),
                buildMainActivityIntent());
    }

    /**
     * Generic news notification.
     *
     * @param title Short title.
     * @param body  Notification body text.
     */
    public void notifyNews(@NonNull String title, @NonNull String body) {
        post(CHANNEL_NEWS, ID_NEWS, title, body, buildMainActivityIntent());
    }

    // ─── Promotional notifications ────────────────────────────────────────────

    public void notifyPromo(@NonNull String title, @NonNull String body) {
        post(CHANNEL_PROMO, ID_PROMO, title, body, buildMainActivityIntent());
    }

    // ─── FCM push-payload dispatch ────────────────────────────────────────────

    /**
     * Route an incoming FCM data payload to the correct local notification.
     *
     * Expected keys in {@code data}:
     *   type  → "update" | "news" | "promo" | "streak"
     *   title → notification title
     *   body  → notification body
     *
     * Call this from your FirebaseMessagingService.onMessageReceived().
     */
    public void dispatchPushPayload(@NonNull String type,
                                    @NonNull String title,
                                    @NonNull String body) {
        switch (type) {
            case "update":
                notifyAppUpdate(title, body);
                break;
            case "news":
                notifyNews(title, body);
                break;
            case "promo":
                notifyPromo(title, body);
                break;
            default:
                notifyNews(title, body);
                break;
        }
    }

    // ─── Cancel helpers ───────────────────────────────────────────────────────

    public void cancelAll() {
        notifMgr.cancelAll();
    }

    public void cancelGameNotifications() {
        notifMgr.cancel(ID_STREAK_MILESTONE);
        notifMgr.cancel(ID_COMBO_RECORD);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void post(@NonNull String channelId, int notifId,
                      @NonNull String title, @NonNull String body,
                      @NonNull PendingIntent contentIntent) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setPriority(channelId.equals(CHANNEL_NEWS)
                            ? NotificationCompat.PRIORITY_HIGH
                            : NotificationCompat.PRIORITY_DEFAULT);

            notifMgr.notify(notifId, builder.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS permission not granted on Android 13+; ignore silently
        }
    }

    @NonNull
    private PendingIntent buildMainActivityIntent() {
        // Lazy import to avoid circular dependency — use class name string
        try {
            Class<?> mainClass = Class.forName(
                    "com.example.sadaguessgame.activities.MainActivity");
            Intent intent = new Intent(appContext, mainClass)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            return PendingIntent.getActivity(appContext, 0, intent, flags);
        } catch (ClassNotFoundException e) {
            // Fallback: empty intent
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    ? PendingIntent.FLAG_IMMUTABLE : 0;
            return PendingIntent.getActivity(appContext, 0,
                    new Intent(), flags);
        }
    }

    private boolean channelEnabled(@NonNull String channelId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true;
        NotificationManager nm = (NotificationManager)
                appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return true;
        NotificationChannel ch = nm.getNotificationChannel(channelId);
        return ch != null && ch.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }
}