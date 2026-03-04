package com.fussy.examfeverplanner.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class NotificationHelper {

    public static final String CHANNEL_ID_TASKS = "exam_fever_tasks";
    public static final String CHANNEL_ID_EXAMS = "exam_fever_exams";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);

            NotificationChannel taskChannel = new NotificationChannel(
                    CHANNEL_ID_TASKS,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            taskChannel.setDescription("Reminders for your to-do tasks");
            nm.createNotificationChannel(taskChannel);

            NotificationChannel examChannel = new NotificationChannel(
                    CHANNEL_ID_EXAMS,
                    "Exam Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            examChannel.setDescription("Countdown alerts for upcoming exams");
            nm.createNotificationChannel(examChannel);
        }
    }

    /**
     * Schedule an exact alarm using AlarmManager.
     *
     * @param context     application context
     * @param triggerAtMs epoch millis when the alarm should fire
     * @param requestCode unique ID (use task/exam id)
     * @param title       notification title
     * @param message     notification body
     * @param channelId   which channel to post to
     */
    public static void scheduleAlarm(Context context, long triggerAtMs,
            int requestCode, String title, String message,
            String channelId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, title);
        intent.putExtra(AlarmReceiver.EXTRA_MESSAGE, message);
        intent.putExtra(AlarmReceiver.EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(AlarmReceiver.EXTRA_NOTIF_ID, requestCode);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
        }
    }

    /** Cancel a previously scheduled alarm. */
    public static void cancelAlarm(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
