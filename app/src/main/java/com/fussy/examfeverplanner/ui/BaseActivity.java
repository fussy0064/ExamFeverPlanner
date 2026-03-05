package com.fussy.examfeverplanner.ui;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.utils.LocaleHelper;

/**
 * Base activity that:
 * 1. Wraps the Context with the user's saved locale on every attach — so
 * language
 * changes are reflected immediately after an Activity restart.
 * 2. Optionally injects the Settings gear icon into any toolbar that uses it.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply the saved locale before the Activity inflates any layout
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
