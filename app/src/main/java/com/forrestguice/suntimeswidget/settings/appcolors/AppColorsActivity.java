/**
    Copyright (C) 2017 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/ 

package com.forrestguice.suntimeswidget.settings.appcolors;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.AppSettings;

public class AppColorsActivity extends AppCompatActivity
{
    public static final String KEY_THEMEID = "themeID";
    public static final String KEY_SELECTED = "selected";
    public static final String KEY_MODE = "colorsmode";

    private AppColorsView appColors;
    private int appTheme;

    public AppColorsActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            appTheme = extras.getInt(KEY_THEMEID, AppSettings.loadTheme(this));
        } else {
            appTheme = AppSettings.loadTheme(this);
        }

        setTheme(appTheme);
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);

        initLocale(this);
        setContentView(R.layout.layout_activity_colorconfig);
        initViews(this);

        if (extras != null)
        {
            appColors.setSelectedAppColors(extras.getString(KEY_SELECTED, AppColors.DEFAULT_NAME));
            appColors.setMode(appColors.getViewModeString(extras.getString(KEY_MODE, AppColorsView.AppColorsViewMode.MODE_CUSTOM_SELECT.name())));
        }
    }

    private void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        appColors.saveSettings(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        appColors.loadSettings(this, savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    protected void initViews(Context context)
    {
        appColors = (AppColorsView)findViewById(R.id.colorconfig);
        appColors.setHideTitle(true);
        appColors.setTheme(appTheme);
        appColors.setFragmentManager(getSupportFragmentManager());
        appColors.setSelectedThemeChangedListener(new AppColorsView.SelectedThemeChangedListener()
        {
            @Override
            public void onSelectedThemeChanged(int themeId)
            {
                super.onSelectedThemeChanged(themeId);
                restartActivity(themeId);
            }
        });
    }

    protected void restartActivity(int themeID)
    {
        Intent intent = new Intent(AppColorsActivity.this, AppColorsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_THEMEID, themeID);
        bundle.putString(KEY_SELECTED, appColors.selectedAppColors());
        bundle.putString(KEY_MODE, appColors.getMode().name());
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        AppColorsActivity.this.finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

}
