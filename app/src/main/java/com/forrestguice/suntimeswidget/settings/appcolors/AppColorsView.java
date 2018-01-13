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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.ColorChooser;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;

public class AppColorsView extends LinearLayout
{
    public static final String KEY_DIALOGMODE = "dialogmode";
    public static final String KEY_SELECTED = "selected";
    public static final String KEY_MODIFIED = "modified";
    public static final String KEY_SUNRISECOLOR = "sunrisecolor";
    public static final String KEY_SUNSETCOLOR = "sunsetcolor";

    private ViewFlipper flipScheme;
    private EditText editScheme;
    private TextView labelScheme;
    private Spinner spinScheme;
    private ImageButton btnAddScheme, btnEditScheme, btnDeleteScheme, btnSaveScheme, btnCancel;

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
        initSchemeAdapter(context);

        btnAddScheme = (ImageButton)findViewById(R.id.addButton);
        btnAddScheme.setOnClickListener(addSchemeClick);

        btnEditScheme = (ImageButton)findViewById(R.id.editButton);
        btnEditScheme.setOnClickListener(editSchemeClick);

        btnDeleteScheme = (ImageButton)findViewById(R.id.deleteButton);
        btnDeleteScheme.setOnClickListener(deleteSchemeClick);

        btnSaveScheme = (ImageButton)findViewById(R.id.saveButton);
        btnSaveScheme.setOnClickListener(saveSchemeClick);

        btnCancel = (ImageButton)findViewById(R.id.cancelButton);
        btnCancel.setOnClickListener(cancelClick);

        flipScheme = (ViewFlipper)findViewById(R.id.flip_scheme);

        labelScheme = (TextView)findViewById(R.id.label_scheme);
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

    protected ArrayAdapter<String> initSchemeAdapter(Context context)
    {
        ArrayAdapter<String> adapter = AppColors.createAppColorsAdapter(context);
        spinScheme.setAdapter(adapter);
        return adapter;
    }

    private AdapterView.OnItemSelectedListener selectedSchemeChanged = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            selectedScheme = parent.getItemAtPosition(position).toString();
            Log.d("DEBUG", "selectedSchemeChanged :: " + selectedScheme + " (" + position + ")");
            loadSettings(getContext());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    private View.OnClickListener addSchemeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setMode(AppColorsViewMode.MODE_CUSTOM_ADD);
        }
    };

    private View.OnClickListener editSchemeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setMode(AppColorsViewMode.MODE_CUSTOM_EDIT);
        }
    };

    private View.OnClickListener cancelClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            loadSettings(getContext());
            setMode(AppColorsViewMode.MODE_CUSTOM_SELECT);
        }
    };

    private View.OnClickListener saveSchemeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (saveScheme(context))
            {
                ArrayAdapter<String> adapter = initSchemeAdapter(context);
                int p = ordinal(adapter, editScheme.getText().toString());
                if (p >= 0)
                {
                    spinScheme.setSelection(p);
                }
                setMode(AppColorsViewMode.MODE_CUSTOM_SELECT);
            }
        }
    };

    protected boolean saveScheme(Context context)
    {
        if (validateInput())
        {
            AppColors colors = new AppColors(context);
            colors.loadAppColors(context, editScheme.getText().toString());

            colors.schemeName = editScheme.getText().toString();
            if (appTheme == R.style.AppTheme_Light)
            {
                colors.light_sunriseColor = sunriseColor.getColor();
                colors.light_sunsetColor = sunsetColor.getColor();
            } else {
                colors.dark_sunriseColor = sunriseColor.getColor();
                colors.dark_sunsetColor = sunsetColor.getColor();
            }

            colors.saveAppColors(context);
            isModified = false;
            return true;
        }
        return false;
    }

    private View.OnClickListener deleteSchemeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (AppColorsView.this.selectedColors != null && !AppColorsView.this.selectedColors.isDefault())
            {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Delete Colors");                                              // TODO: i18n
                alertDialog.setMessage("Are you sure you want to delete " + selectedScheme + "?");  // TODO: i18n
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",                        // TODO: i18n
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Context context = getContext();
                                AppColors colors = new AppColors(context);
                                colors.loadAppColors(context, selectedScheme);
                                colors.deleteAppColors(context);
                                Toast.makeText(context, selectedScheme + " was deleted.", Toast.LENGTH_LONG).show();  // TODO

                                selectedScheme = AppColors.DEFAULT_NAME;
                                initSchemeAdapter(context);
                                mode = AppColorsViewMode.MODE_CUSTOM_SELECT;
                                loadSettings(context);
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",       // TODO: i18n
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener selectedThemeChanged = new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes final int checkedId)
        {
            if (isModified)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Save changes?");                              // TODO: i18n
                alertDialog.setMessage("Colors were modified. Save the changes?");  // TODO: i18n
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save",          // TODO: i18n
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                boolean saved = saveScheme(getContext());
                                if (saved)
                                {
                                    mode = AppColorsViewMode.MODE_CUSTOM_EDIT;
                                    selectedScheme = editScheme.getText().toString();
                                    setThemeTab(checkedId);
                                    signalSelectedThemeChanged(checkedId);
                                }
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Discard",       // TODO: i18n
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                setThemeTab(checkedId);
                                signalSelectedThemeChanged(checkedId);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",         // TODO: i18n
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        });

                setThemeTab(appTheme);
                alertDialog.show();

            } else {
                signalSelectedThemeChanged(checkedId);
            }
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
    }

    protected void loadSettings(Context context)
    {
        if (isInEditMode())
            return;

        selectedColors = new AppColors(context);
        selectedColors.loadAppColors(context, selectedScheme);

        if (appTheme == R.style.AppTheme_Light)
        {
            sunriseColor.setColor(selectedColors.getLightSunriseColor());
            sunsetColor.setColor(selectedColors.getLightSunsetColor());

        } else {
            sunriseColor.setColor(selectedColors.getDarkSunriseColor());
            sunsetColor.setColor(selectedColors.getDarkSunsetColor());
        }

        isModified = false;
        updateViews(context);
        setMode(mode);
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
            setMode(getViewModeString(viewModeString));
        }

        /**String schemeName = bundle.getString(KEY_SELECTED, "");
        int i = findScheme(spinScheme, schemeName);
        if (i >= 0 && i < spinScheme.getCount()) {
            spinScheme.setSelection(i);
        }*/

        sunriseColor.setColor(bundle.getInt(KEY_SUNRISECOLOR, sunriseColor.getColor()));
        sunsetColor.setColor(bundle.getInt(KEY_SUNSETCOLOR, sunsetColor.getColor()));
        isModified = bundle.getBoolean(KEY_MODIFIED, false);

        Log.d("DEBUG", "AppColorsView.loadSettings(context,bundle) :: " + isModified);
        updateViews(getContext());
    }

    private int findScheme(Spinner spinner, String schemeName)
    {
        for (int i=0; i<spinner.getCount(); i++)
        {
            Object item = spinner.getItemAtPosition(i);
            if (item.toString().equals(schemeName))
            {
                return i;
            }
        }
        return -1;
    }

    public AppColorsViewMode getViewModeString(String viewModeString)
    {
        AppColorsViewMode viewMode;
        try {
            viewMode = AppColorsViewMode.valueOf(viewModeString);
        } catch (IllegalArgumentException e) {
            Log.w("DEBUG", "Bundle contained bad viewModeString! " + e.toString());
            viewMode = AppColorsViewMode.MODE_CUSTOM_SELECT;
        }
        return viewMode;
    }

    /**
     * @param bundle a Bundle to save to
     * @return true settings were saved
     */
    protected boolean saveSettings(Bundle bundle)
    {
        bundle.putString(KEY_DIALOGMODE, mode.name());
        bundle.putString(KEY_SELECTED, spinScheme.getSelectedItem().toString());
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
        if (mode == AppColorsViewMode.MODE_CUSTOM_ADD)
        {
            Adapter adapter = spinScheme.getAdapter();
            boolean notUnique = (ordinal(adapter, editScheme.getText().toString()) >= 0);
            if (notUnique)
            {
                editScheme.setError("Name must be unique.");
                isValid = false;
            }
        }
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
    public void setSelectedAppColors( String schemeName )
    {
        selectedScheme = schemeName;
        int p = ordinal(spinScheme.getAdapter(), selectedScheme);
        if (p >= 0)
        {
            spinScheme.setSelection(p);
        }
        loadSettings(getContext());
    }

    protected boolean isDefault()
    {
        return AppColors.DEFAULT_NAME.equals(selectedScheme);
    }

    /** Property: appTheme */
    private int appTheme = R.style.AppTheme_Dark;
    public void setTheme( int themeId )
    {
        appTheme = themeId;
        setThemeTab(themeId);
        loadSettings(getContext());
    }
    public int getTheme()
    {
        return appTheme;
    }

    private void setThemeTab( int themeId )
    {
        RadioButton themeButton = (RadioButton)findViewById(themeIdToTabId(themeId));
        if (themeButton != null)
        {
            selectTheme.setOnCheckedChangeListener(null);
            themeButton.setChecked(true);
            selectTheme.setOnCheckedChangeListener(selectedThemeChanged);
        }
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
                spinScheme.setOnItemSelectedListener(null);
                hideButton(btnAddScheme);
                hideButton(btnEditScheme);
                hideButton(btnDeleteScheme);
                showButton(btnSaveScheme);
                showButton(btnCancel);
                setEditorEnabled(true);
                editScheme.setError(null);
                editScheme.setText(suggestSchemeName());
                editScheme.requestFocus();
                break;

            case MODE_CUSTOM_EDIT:
                spinScheme.setOnItemSelectedListener(null);
                hideButton(btnAddScheme);
                hideButton(btnEditScheme);
                showButton(btnDeleteScheme, !isDefault());
                showButton(btnSaveScheme);
                showButton(btnCancel);
                setEditorEnabled(true);
                editScheme.setError(null);
                editScheme.setText(selectedColors.name());
                editScheme.setEnabled(false);
                btnSaveScheme.requestFocus();
                break;

            case MODE_CUSTOM_SELECT:
            default:
                showButton(btnAddScheme);
                showButton(btnEditScheme, !isDefault());
                hideButton(btnDeleteScheme);
                hideButton(btnSaveScheme);
                hideButton(btnCancel);
                setEditorEnabled(false);
                labelScheme.requestFocus();
                spinScheme.setOnItemSelectedListener(selectedSchemeChanged);
                break;
        }
    }

    private void hideButton( ImageButton button )
    {
        button.setEnabled(false);
        button.setVisibility(View.GONE);
    }

    private void showButton( ImageButton button )
    {
        button.setEnabled(true);
        button.setVisibility(View.VISIBLE);
    }

    private void showButton( ImageButton button, boolean show )
    {
        if (show)
            showButton(button);
        else hideButton(button);
    }

    protected void setEditorEnabled( boolean enabled )
    {
        editScheme.setEnabled(enabled);
        editScheme.setError(null);
        spinScheme.setEnabled(!enabled);
        flipScheme.setDisplayedChild(enabled ? 1 : 0);

        for (ColorChooser chooser : colorsChoosers)
        {
            chooser.setEnabled(enabled);
        }
    }

    protected String suggestSchemeName()
    {
        int i = 1;
        String generatedName;
        Context context = getContext();
        Adapter adapter = spinScheme.getAdapter();
        do {
            generatedName = context.getString(R.string.addcolors_custname, i+"");
            i++;
        } while (ordinal(adapter, generatedName) >= 0);
        return generatedName;
    }

    protected int ordinal(Adapter adapter, String value)
    {
        int n = adapter.getCount();
        for (int i=0; i<n; i++)
        {
            String item = adapter.getItem(i).toString();
            if (item.equals(value))
            {
                return i;
            }
        }
        return -1;
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
