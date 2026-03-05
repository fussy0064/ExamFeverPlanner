package com.fussy.examfeverplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import com.fussy.examfeverplanner.ui.SettingsActivity;

import java.util.Locale;

/**
 * Wraps a Context with the user-chosen locale so every Activity gets the right
 * language
 * as soon as it inflates its layout.
 */
public class LocaleHelper {

    /**
     * Returns a new Context with the saved locale applied.
     * Call this from every Activity's attachBaseContext().
     */
    public static Context wrap(Context context) {
        String langCode = getSavedLangCode(context);
        return applyLocale(context, langCode);
    }

    /** Persist the chosen locale and immediately apply it to the given context. */
    public static Context setLocale(Context context, String langCode) {
        saveLangCode(context, langCode);
        return applyLocale(context, langCode);
    }

    /** Language name → ISO-639-1 code */
    public static String nameToCode(String name) {
        switch (name) {
            case "Swahili":
                return "sw";
            case "French":
                return "fr";
            case "Arabic":
                return "ar";
            case "Spanish":
                return "es";
            case "Portuguese":
                return "pt";
            case "Chinese":
                return "zh";
            default:
                return "en";
        }
    }

    /** ISO-639-1 code → display name */
    public static String codeToName(String code) {
        switch (code) {
            case "sw":
                return "Swahili";
            case "fr":
                return "French";
            case "ar":
                return "Arabic";
            case "es":
                return "Spanish";
            case "pt":
                return "Portuguese";
            case "zh":
                return "Chinese";
            default:
                return "English";
        }
    }

    // ─── private helpers ────────────────────────────────────────────────────

    private static Context applyLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        } else {
            config.locale = locale;
        }
        return context.createConfigurationContext(config);
    }

    private static String getSavedLangCode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SettingsActivity.PREF_FILE, Context.MODE_PRIVATE);
        return prefs.getString(SettingsActivity.KEY_LANGUAGE, "en");
    }

    private static void saveLangCode(Context context, String code) {
        context.getSharedPreferences(SettingsActivity.PREF_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(SettingsActivity.KEY_LANGUAGE, code)
                .apply();
    }
}
