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
package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.forrestguice.suntimeswidget.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class AppColors
{
    public static final String PREFS_COLORS = "com.forrestguice.suntimeswidget.colors";

    public static final String APPCOLORS_KEY = "appcolors_";

    public static final String APPCOLORS_NAME = "name";
    public static final String APPCOLORS_DISPLAY = "display";
    public static final String APPCOLORS_DARK_SUNRISE = "dark_sunrise";
    public static final String APPCOLORS_DARK_SUNSET = "dark_sunset";
    public static final String APPCOLORS_LIGHT_SUNRISE = "light_sunrise";
    public static final String APPCOLORS_LIGHT_SUNSET = "light_sunset";

    public static final String DEFAULT_NAME = "default";
    public static final String DEFAULT_DISPLAY = "Default";
    public static final int DEFAULT_DARK_SUNRISE = R.color.sunIcon_color_rising_dark;
    public static final int DEFAULT_DARK_SUNSET = R.color.sunIcon_color_setting_dark;
    public static final int DEFAULT_LIGHT_SUNRISE = R.color.sunIcon_color_rising_light;
    public static final int DEFAULT_LIGHT_SUNSET = R.color.sunIcon_color_setting_light;

    protected String schemeName;
    protected String displayString;

    protected int dark_sunriseColor;
    protected int dark_sunsetColor;

    protected int light_sunriseColor;
    protected int light_sunsetColor;

    public AppColors(Context context)
    {
        this.schemeName = DEFAULT_NAME;
        this.displayString = DEFAULT_DISPLAY;
        this.dark_sunriseColor = context.getColor(DEFAULT_DARK_SUNRISE);
        this.dark_sunsetColor = context.getColor(DEFAULT_DARK_SUNSET);
        this.light_sunriseColor = context.getColor(DEFAULT_LIGHT_SUNRISE);
        this.light_sunsetColor = context.getColor(DEFAULT_LIGHT_SUNSET);
    }

    public AppColors( AppColors other )
    {
        this.schemeName = other.name();
        this.displayString = other.getDisplayString();
        this.dark_sunriseColor = other.getDarkSunriseColor();
        this.dark_sunsetColor = other.getDarkSunsetColor();
        this.light_sunriseColor = other.getLightSunriseColor();
        this.light_sunsetColor = other.getLightSunsetColor();
    }

    public String name()
    {
        return schemeName;
    }

    public String getDisplayString()
    {
        return displayString;
    }

    public int getDarkSunriseColor()
    {
        return dark_sunriseColor;
    }

    public int getDarkSunsetColor()
    {
        return dark_sunsetColor;
    }

    public int getLightSunriseColor()
    {
        return light_sunriseColor;
    }

    public int getLightSunsetColor()
    {
        return light_sunsetColor;
    }

    public boolean isDefault()
    {
        return schemeName.equals(DEFAULT_NAME);
    }

    public boolean loadAppColors(Context context, String schemeName)
    {
        return loadAppColors(context, appColorsPrefs(context), schemeName);
    }
    public boolean loadAppColors(Context context, SharedPreferences prefs, String schemeName)
    {
        String prefix = appColorsPrefix(this.schemeName);

        this.schemeName = prefs.getString(prefix + APPCOLORS_NAME, DEFAULT_NAME);
        this.displayString = prefs.getString(prefix + APPCOLORS_DISPLAY, DEFAULT_DISPLAY);
        this.dark_sunriseColor = prefs.getInt(prefix + APPCOLORS_DARK_SUNRISE, context.getColor(DEFAULT_DARK_SUNRISE));
        this.dark_sunsetColor = prefs.getInt(prefix + APPCOLORS_DARK_SUNSET, context.getColor(DEFAULT_DARK_SUNSET));
        this.light_sunriseColor = prefs.getInt(prefix + APPCOLORS_LIGHT_SUNRISE, context.getColor(DEFAULT_LIGHT_SUNRISE));
        this.light_sunsetColor = prefs.getInt(prefix + APPCOLORS_LIGHT_SUNSET, context.getColor(DEFAULT_LIGHT_SUNSET));
        return true;
    }

    public void saveAppColors(Context context)
    {
        saveAppColors(appColorsPrefs(context));
    }
    public void saveAppColors(SharedPreferences prefs)
    {
        SharedPreferences.Editor editor = prefs.edit();
        String prefix = appColorsPrefix(this.schemeName);

        editor.putString(prefix + APPCOLORS_NAME, this.schemeName);
        editor.putString(prefix + APPCOLORS_DISPLAY, this.displayString);
        editor.putInt(prefix + APPCOLORS_DARK_SUNRISE, this.dark_sunriseColor);
        editor.putInt(prefix + APPCOLORS_DARK_SUNSET, this.dark_sunsetColor);
        editor.putInt(prefix + APPCOLORS_LIGHT_SUNRISE, this.light_sunriseColor);
        editor.putInt(prefix + APPCOLORS_LIGHT_SUNSET, this.light_sunsetColor);

        editor.apply();
    }

    public void deleteAppColors(Context context)
    {
        deleteAppColors(appColorsPrefs(context));
    }
    public void deleteAppColors(SharedPreferences prefs)
    {
        SharedPreferences.Editor editor = prefs.edit();
        String prefix = appColorsPrefix(this.schemeName);

        editor.remove(prefix + APPCOLORS_NAME);
        editor.remove(prefix + APPCOLORS_DISPLAY);
        editor.remove(prefix + APPCOLORS_DARK_SUNRISE);
        editor.remove(prefix + APPCOLORS_DARK_SUNSET);
        editor.remove(prefix + APPCOLORS_LIGHT_SUNRISE);
        editor.remove(prefix + APPCOLORS_LIGHT_SUNSET);

        editor.apply();
    }

    public static String appColorsPrefix(String schemeName)
    {
        return APPCOLORS_KEY + schemeName + "_";
    }

    public static SharedPreferences appColorsPrefs(Context context)
    {
        return context.getSharedPreferences(PREFS_COLORS, Context.MODE_PRIVATE);
    }

    public static ArrayList<String> scanForAppColors(Context context)
    {
        ArrayList<String> appColorsList = new ArrayList<>();

        SharedPreferences prefs = appColorsPrefs(context);
        Map<String,?> keys = prefs.getAll();
        if (keys != null)
        {
            for (Map.Entry<String, ?> entry : keys.entrySet())
            {
                String key = entry.getKey();
                if (key.endsWith("_" + APPCOLORS_NAME))
                {
                    Object value = entry.getValue();
                    if (value != null)
                    {
                        appColorsList.add(value.toString());
                    }
                }
            }
        }

        appColorsList.remove(DEFAULT_NAME);
        Collections.sort(appColorsList, String.CASE_INSENSITIVE_ORDER);
        appColorsList.add(0, DEFAULT_NAME);

        return appColorsList;
    }
}
