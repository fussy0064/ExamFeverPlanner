package com.fussy.examfeverplanner.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.fussy.examfeverplanner.MainActivity;
import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.ui.SettingsActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID_TASKS = "exam_fever_tasks";
    public static final String CHANNEL_ID_EXAMS = "exam_fever_exams";

    // ─── Channel setup ────────────────────────────────────────────────────────

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);

            NotificationChannel taskChannel = new NotificationChannel(
                    CHANNEL_ID_TASKS, context.getString(R.string.channel_task_reminders),
                    NotificationManager.IMPORTANCE_HIGH);
            taskChannel.setDescription(context.getString(R.string.channel_task_reminders_desc));
            taskChannel.enableVibration(true);
            nm.createNotificationChannel(taskChannel);

            NotificationChannel examChannel = new NotificationChannel(
                    CHANNEL_ID_EXAMS, context.getString(R.string.channel_exam_alerts),
                    NotificationManager.IMPORTANCE_HIGH);
            examChannel.setDescription(context.getString(R.string.channel_exam_alerts_desc));
            examChannel.enableVibration(true);
            nm.createNotificationChannel(examChannel);
        }
    }

    // ─── Immediate notification (fires right now, no alarm needed) ───────────

    /**
     * Post a notification immediately — used for exam-added confirmation and
     * very short-lead reminders (< 60 s away).
     */
    public static void sendNow(Context context, int notifId,
            String title, String message, String channelId) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(context, notifId, openIntent, piFlags);

        Uri soundUri = getSoundUri(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] { 0, 250, 250, 250 });

        if (soundUri != null)
            builder.setSound(soundUri);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null)
            nm.notify(notifId, builder.build());
    }

    // ─── Scheduled alarm ─────────────────────────────────────────────────────

    /**
     * Schedule an exact alarm at {@code triggerAtMs}.
     * If {@code triggerAtMs} is already past, fires immediately instead.
     */
    public static void scheduleAlarm(Context context, long triggerAtMs,
            int requestCode, String title, String message,
            String channelId) {
        // If time already passed, just notify now
        if (triggerAtMs <= System.currentTimeMillis()) {
            sendNow(context, requestCode, title, message, channelId);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, title);
        intent.putExtra(AlarmReceiver.EXTRA_MESSAGE, message);
        intent.putExtra(AlarmReceiver.EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(AlarmReceiver.EXTRA_NOTIF_ID, requestCode);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
        }
    }

    // ─── Cancel ──────────────────────────────────────────────────────────────

    public static void cancelAlarm(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        if (pi != null) {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Read the user's chosen notification sound; fall back to system default. */
    private static Uri getSoundUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREF_FILE, Context.MODE_PRIVATE);
        boolean notifOn = prefs.getBoolean(SettingsActivity.KEY_NOTIF_ON, true);
        if (!notifOn)
            return null; // user turned off sounds

        String savedUri = prefs.getString(SettingsActivity.KEY_NOTIF_URI, null);
        if (savedUri != null) {
            return Uri.parse(savedUri);
        }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }
}
