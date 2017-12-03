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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;

public class AppColorsView extends LinearLayout
{
    public static final String KEY_DIALOGMODE = "dialogmode";
    public static final String KEY_MODIFIED = "modified";
    public static final String KEY_SUNRISECOLOR = "sunrisecolor";
    public static final String KEY_SUNSETCOLOR = "sunsetcolor";

    private ViewFlipper flipScheme;
    private EditText editScheme;
    private Spinner spinScheme;

    private RadioGroup selectTheme;

    private TextView sunriseText, sunsetText;

    private ColorChooser sunriseColor, sunsetColor;
    private ArrayList<ColorChooser> colorsChoosers = new ArrayList<>();

    private boolean isInitialized = false;

    public AppColorsView(Context context)
    {
        super(context);
        init(context, false);
    }

    public AppColorsView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context, false);
    }

    public void init(Context context, boolean asDialog)
    {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate((asDialog ? R.layout.layout_dialog_colorconfig : R.layout.layout_settings_colorconfig), this);
        initViews(context);

        loadSettings(context);
        setMode(mode);
        isInitialized = true;
    }

    public boolean isInitialized() { return isInitialized; }

    /**
     *
     * @param context a context used to access resources
     */
    protected void initViews( Context context )
    {
        spinScheme = (Spinner)findViewById(R.id.spin_scheme_name);
        spinScheme.setAdapter(AppColors.createAppColorsAdapter(context));
        spinScheme.setOnItemSelectedListener(selectedSchemeChanged);
        flipScheme = (ViewFlipper)findViewById(R.id.flip_scheme);
        editScheme = (EditText)findViewById(R.id.edit_scheme_name);

        selectTheme = (RadioGroup)findViewById(R.id.tabs);
        selectTheme.setOnCheckedChangeListener(selectedThemeChanged);

        sunriseText = (TextView)findViewById(R.id.preview_sunrise_text);
        sunsetText = (TextView)findViewById(R.id.preview_sunset_text);

        sunriseColor = new ColorChooser(this, context, R.id.editLabel_sunriseColor, R.id.edit_sunriseColor, R.id.editButton_sunriseColor, SuntimesTheme.THEME_SUNRISECOLOR);
        sunriseColor.setOnColorChangedListener(colorChanged);
        sunriseColor.setCollapsed(true);
        colorsChoosers.add(sunriseColor);

        sunsetColor = new ColorChooser(this, context, R.id.editLabel_sunsetColor, R.id.edit_sunsetColor, R.id.editButton_sunsetColor, SuntimesTheme.THEME_SUNSETCOLOR);
        sunsetColor.setOnColorChangedListener(colorChanged);
        sunsetColor.setCollapsed(true);
        colorsChoosers.add(sunsetColor);
    }

    private AdapterView.OnItemSelectedListener selectedSchemeChanged = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            selectedScheme = parent.getItemAtPosition(position).toString();
            Log.d("DEBUG", "scheme selected: " + selectedScheme);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    private RadioGroup.OnCheckedChangeListener selectedThemeChanged = new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId)
        {
            signalSelectedThemeChanged(checkedId);
        }
    };

    private ColorChooser.OnColorChangedListener colorChanged = new ColorChooser.OnColorChangedListener()
    {
        @Override
        public void onColorChanged(int newColor)
        {
            super.onColorChanged(newColor);
            isModified = true;
            updateViews(getContext());
        }
    };

    private FragmentManager fragmentManager = null;
    public void setFragmentManager( FragmentManager manager )
    {
        fragmentManager = manager;
        for (ColorChooser chooser : colorsChoosers)
        {
            chooser.setFragmentManager(fragmentManager);
        }
    }

    protected void updateViews( Context context )
    {
        if (sunriseText != null)
        {
            sunriseText.setTextColor(sunriseColor.getColor());
        }
        if (sunsetText != null)
        {
            sunsetText.setTextColor(sunsetColor.getColor());
        }
    }

    public void onResume()
    {
        updateViews(getContext());
    }

    public void onStop()
    {
        // TODO
    }

    protected void loadSettings(Context context)
    {
        if (isInEditMode())
            return;

        selectedColors = new AppColors(context);
        if (appTheme == R.style.AppTheme_Light)
        {
            sunriseColor.setColor(Color.BLUE);   // TODO
            sunsetColor.setColor(Color.GREEN);

        } else {
            sunriseColor.setColor(Color.YELLOW);   // TODO
            sunsetColor.setColor(Color.RED);
        }
        updateViews(context);
    }

    /**
     * @param context a context used to access shared prefs
     * @param bundle a Bundle containing saved state
     */
    protected void loadSettings(Context context, Bundle bundle )
    {
        String viewModeString = bundle.getString(KEY_DIALOGMODE);
        if (viewModeString != null)
        {
            AppColorsViewMode viewMode;
            try {
                viewMode = AppColorsViewMode.valueOf(viewModeString);
            } catch (IllegalArgumentException e) {
                Log.w("DEBUG", "Bundle contained bad viewModeString! " + e.toString());
                viewMode = AppColorsViewMode.MODE_CUSTOM_SELECT;
            }
            setMode(viewMode);

            sunriseColor.setColor(bundle.getInt(KEY_SUNRISECOLOR, sunriseColor.getColor()));
            sunsetColor.setColor(bundle.getInt(KEY_SUNSETCOLOR, sunsetColor.getColor()));
        }
        isModified = bundle.getBoolean(KEY_MODIFIED, false);
        updateViews(getContext());
    }

    /**
     * @param bundle a Bundle to save to
     * @return true settings were saved
     */
    protected boolean saveSettings(Bundle bundle)
    {
        bundle.putString(KEY_DIALOGMODE, mode.name());
        bundle.putInt(KEY_SUNRISECOLOR, sunriseColor.getColor());
        bundle.putInt(KEY_SUNSETCOLOR, sunsetColor.getColor());
        bundle.putBoolean(KEY_MODIFIED, isModified);
        return true;
    }

    public int getThemeTab()
    {
        return selectTheme.getCheckedRadioButtonId();
    }

    /**
     * Check text fields for validity; as a side-effect sets an error message on fields with invalid
     * values.
     * @return true if all fields valid, false otherwise
     */
    public boolean validateInput()
    {
        boolean isValid = true;
        // TODO
        return isValid;
    }

    /**
     * Property: isModified
     */
    private boolean isModified = false;
    public boolean isModified()
    {
        return this.isModified;
    }

    /**
     * Property: hide title
     */
    private boolean hideTitle = false;
    public boolean getHideTitle() { return hideTitle; }
    public void setHideTitle(boolean value)
    {
        hideTitle = value;
        TextView groupTitle = (TextView)findViewById(R.id.colorconfig_grouptitle);
        if (groupTitle != null)
        {
            groupTitle.setVisibility((hideTitle ? View.GONE : View.VISIBLE));
        }
    }

    /** Property: selectedAppColors */
    private String selectedScheme = AppColors.DEFAULT_NAME;
    private AppColors selectedColors;
    public String selectedAppColors()
    {
        return selectedScheme;
    }

    /** Property: appTheme */
    private int appTheme = R.style.AppTheme_Dark;
    public void setTheme( int themeId )
    {
        appTheme = themeId;
        RadioButton themeButton = (RadioButton)findViewById(themeIdToTabId(themeId));
        if (themeButton != null)
        {
            selectTheme.setOnCheckedChangeListener(null);
            themeButton.setChecked(true);
            selectTheme.setOnCheckedChangeListener(selectedThemeChanged);
        }
        loadSettings(getContext());
    }
    public int getTheme()
    {
        return appTheme;
    }

    protected int tabIdToStyleId(int tabId)
    {
        switch (tabId)
        {
            case R.id.tabLight:
                return R.style.AppTheme_Light;
            case R.id.tabDark:
            default:
                return R.style.AppTheme_Dark;
        }
    }

    protected int themeIdToTabId(int themeId)
    {
        switch (themeId)
        {
            case R.style.AppTheme_Light:
                return R.id.tabLight;
            case R.style.AppTheme_Dark:
            default:
                return R.id.tabDark;
        }

    }

    /** Property: selectedThemeChangedListener */
    protected SelectedThemeChangedListener listener = null;
    public void setSelectedThemeChangedListener(SelectedThemeChangedListener listener )
    {
        this.listener = listener;
    }
    public void clearSelectedThemeChangedListener()
    {
        this.listener = null;
    }
    private void signalSelectedThemeChanged(int tabId)
    {
        if (listener != null)
        {
            listener.onSelectedThemeChanged(tabIdToStyleId(tabId));
        }
    }

    /** Property: mode (select, edit/add) */
    private AppColorsViewMode mode = AppColorsViewMode.MODE_CUSTOM_SELECT;
    public AppColorsViewMode getMode()
    {
        return mode;
    }
    public void setMode( AppColorsViewMode mode )
    {
        if (this.mode != mode)
        {
            // TODO
        }

        this.mode = mode;
        switch (mode)
        {
            case MODE_CUSTOM_ADD:
            case MODE_CUSTOM_EDIT:
                editScheme.setEnabled(true);
                spinScheme.setEnabled(false);
                flipScheme.setDisplayedChild(1);
                setEditorEnabled(true);
                break;

            case MODE_CUSTOM_SELECT:
            default:
                editScheme.setEnabled(false);
                spinScheme.setEnabled(true);
                flipScheme.setDisplayedChild(0);
                setEditorEnabled(false);
                break;
        }
    }

    protected void setEditorEnabled( boolean enabled )
    {

    }

    /**
     * Enum of possible ui states
     */
    public static enum AppColorsViewMode
    {
        MODE_CUSTOM_SELECT(), MODE_CUSTOM_ADD(), MODE_CUSTOM_EDIT();
        private AppColorsViewMode() {}

        public String toString()
        {
            return this.name();
        }

        public int ordinal( AppColorsViewMode[] array )
        {
            for (int i=0; i<array.length; i++)
            {
                if (array[i].name().equals(this.name()))
                {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     * ThemeTabChangedListener
     */
    public static abstract class SelectedThemeChangedListener
    {
        public void onSelectedThemeChanged(int themeId ) {}
    }

}
