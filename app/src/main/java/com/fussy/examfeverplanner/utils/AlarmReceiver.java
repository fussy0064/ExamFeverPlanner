package com.fussy.examfeverplanner.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    public static final String EXTRA_NOTIF_ID = "extra_notif_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        int notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 0);

        // Check if notifications are enabled in Settings
        SharedPreferences prefs = context.getSharedPreferences(
                SettingsActivity.PREF_FILE, Context.MODE_PRIVATE);
        boolean notifOn = prefs.getBoolean(SettingsActivity.KEY_NOTIF_ON, true);
        if (!notifOn)
            return;

        // Tap-to-open → MainActivity
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, openIntent, piFlags);

        // Pick user's chosen sound or system default
        String savedUri = prefs.getString(SettingsActivity.KEY_NOTIF_URI, null);
        Uri soundUri = (savedUri != null)
                ? Uri.parse(savedUri)
                : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(soundUri)
                .setVibrate(new long[] { 0, 300, 200, 300 })
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null)
            nm.notify(notifId, builder.build());
    }
}
