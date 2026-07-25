package com.fussy.examfeverplanner.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.fussy.examfeverplanner.MainActivity;
import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.utils.LocaleHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    public static final String PREF_FILE = "exam_fever_prefs";
    public static final String KEY_THEME = "theme"; // 0=System,1=Light,2=Dark
    public static final String KEY_LANGUAGE = "lang_code"; // ISO-639-1 code: en, sw, fr …
    public static final String KEY_NOTIF_URI = "notif_sound"; // Uri string
    public static final String KEY_NOTIF_ON = "notif_enabled";

    private SharedPreferences prefs;
    private TextView tvTheme, tvLanguage, tvSound;
    private SwitchMaterial switchNotif;

    private final ActivityResultLauncher<Intent> ringtoneLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Uri uri = result.getData().getParcelableExtra(
                            RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        prefs.edit().putString(KEY_NOTIF_URI, uri.toString()).apply();
                        updateSoundLabel(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTheme = findViewById(R.id.tv_theme_value);
        tvLanguage = findViewById(R.id.tv_language_value);
        tvSound = findViewById(R.id.tv_sound_value);
        switchNotif = findViewById(R.id.switch_notifications);

        loadCurrentValues();

        // ── THEME ──────────────────────────────────────────────────────────
        final String[] themes = getResources().getStringArray(R.array.theme_options);
        findViewById(R.id.row_theme).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.choose_theme)
                .setItems(themes, (d, which) -> {
                    prefs.edit().putInt(KEY_THEME, which).apply();
                    tvTheme.setText(themes[which]);
                    applyTheme(which);
                }).show());

        // ── LANGUAGE ───────────────────────────────────────────────────────
        final String[] langNames = {
                "English", "Swahili", "French", "Arabic",
                "Spanish", "Portuguese", "Chinese"
        };
        final String[] langCodes = { "en", "sw", "fr", "ar", "es", "pt", "zh" };

        findViewById(R.id.row_language).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.choose_language)
                .setItems(langNames, (d, which) -> {
                    String code = langCodes[which];
                    String name = langNames[which];

                    // Save as ISO code so LocaleHelper can use it
                    prefs.edit().putString(KEY_LANGUAGE, code).apply();
                    tvLanguage.setText(name);

                    // Apply immediately and restart the whole app from MainActivity
                    LocaleHelper.setLocale(this, code);
                    Intent restart = new Intent(this, MainActivity.class);
                    restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(restart);
                    finishAffinity(); // close all back stack
                }).show());

        // ── NOTIFICATION SOUND ─────────────────────────────────────────────
        findViewById(R.id.row_notif_sound).setOnClickListener(v -> {
            String saved = prefs.getString(KEY_NOTIF_URI, null);
            Uri existingUri = (saved != null)
                    ? Uri.parse(saved)
                    : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_notification_sound));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            ringtoneLauncher.launch(intent);
        });

        // ── NOTIFICATIONS TOGGLE ───────────────────────────────────────────
        switchNotif.setChecked(prefs.getBoolean(KEY_NOTIF_ON, true));
        switchNotif.setOnCheckedChangeListener(
                (btn, isChecked) -> prefs.edit().putBoolean(KEY_NOTIF_ON, isChecked).apply());
    }

    private void loadCurrentValues() {
        int themeIdx = prefs.getInt(KEY_THEME, 0);
        String[] themes = getResources().getStringArray(R.array.theme_options);
        tvTheme.setText(themes[Math.min(themeIdx, themes.length - 1)]);

        // Show display name for the stored code
        String code = prefs.getString(KEY_LANGUAGE, "en");
        tvLanguage.setText(LocaleHelper.codeToName(code));

        String savedUri = prefs.getString(KEY_NOTIF_URI, null);
        if (savedUri != null)
            updateSoundLabel(Uri.parse(savedUri));
        else
            tvSound.setText(R.string.sound_default);

        switchNotif.setChecked(prefs.getBoolean(KEY_NOTIF_ON, true));
    }

    private void updateSoundLabel(Uri uri) {
        try {
            Ringtone ring = RingtoneManager.getRingtone(this, uri);
            tvSound.setText(ring != null ? ring.getTitle(this) : getString(R.string.sound_custom));
        } catch (Exception e) {
            tvSound.setText(R.string.sound_custom);
        }
    }

    private void applyTheme(int index) {
        int mode;
        switch (index) {
            case 1:
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case 2:
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
        recreate();
    }

    /** Call from Application / MainActivity to restore saved theme on cold start */
    public static void applyStoredTheme(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        int idx = p.getInt(KEY_THEME, 0);
        int mode;
        switch (idx) {
            case 1:
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case 2:
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
