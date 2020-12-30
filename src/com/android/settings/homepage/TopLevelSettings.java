/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.homepage;

import static com.android.settings.search.actionbar.SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR;
import static com.android.settingslib.search.SearchIndexable.MOBILE;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.os.UserManager;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.UserInfo;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.support.SupportPreferenceController;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.display.TimeoutPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settings.network.TetherPreferenceController;
import com.android.settings.wifi.TopLevelWifiPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

import java.util.ArrayList;
import java.util.List;


@SearchIndexable(forTarget = MOBILE)
public class TopLevelSettings extends DashboardFragment implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "TopLevelSettings";
    private static final String KEY_USER_INFO = "user_info";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";

    public TopLevelSettings() {
        final Bundle args = new Bundle();
        // Disable the search icon because this page uses a full search view in actionbar.
        args.putBoolean(NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        setArguments(args);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.top_level_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DASHBOARD_SUMMARY;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(SupportPreferenceController.class).setActivity(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        initUserInfo();
    }
    
    @Override
    public int getHelpResource() {
        // Disable the help icon because this page uses a full search view in actionbar.
        return 0;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), mMetricsFeatureProvider);
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        new SubSettingLauncher(getActivity())
                .setDestination(pref.getFragment())
                .setArguments(pref.getExtras())
                .setSourceMetricsCategory(caller instanceof Instrumentable
                        ? ((Instrumentable) caller).getMetricsCategory()
                        : Instrumentable.METRICS_CATEGORY_UNKNOWN)
                .setTitleRes(-1)
                .launch();
        return true;
    }

    @Override
    protected boolean shouldForceRoundedIcon() {
        return getContext().getResources()
                .getBoolean(R.bool.config_force_rounded_icon_TopLevelSettings);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, MetricsFeatureProvider metricsFeatureProvider) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new TimeoutPreferenceController(context, KEY_SCREEN_TIMEOUT));
        controllers.add(new TetherPreferenceController(context, lifecycle));
        controllers.add(new TopLevelWifiPreferenceController(context, metricsFeatureProvider));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.top_level_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(Context
                        context) {
                    return buildPreferenceControllers(context, null /* lifecycle */, null /* metricsFeatureProvider */);
                }
            
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Never searchable, all entries in this page are already indexed elsewhere.
                    return false;
                }
            };


    private void initUserInfo() {
        // TODO: Migrate into its own controller.
        final LayoutPreference headerPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(KEY_USER_INFO);
        final View appSnippet = headerPreference.findViewById(R.id.entity_header);
        final TextView tv = headerPreference.findViewById(R.id.summary);
              tv.setSelected(true);
        final Activity context = getActivity();
        final Bundle bundle = getArguments();
        final EntityHeaderController controller = EntityHeaderController
                .newInstance(context, this, appSnippet)
                .setRecyclerView(getListView(), getSettingsLifecycle())
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);
        
        appSnippet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$UserSettingsActivity"));
                startActivity(intent);
            }
        });

        // TODO: There may be an avatar setting action we can use here.
        final int iconId = bundle.getInt("icon_id", 0);
        if (iconId == 0) {
            final UserManager userManager = (UserManager) getActivity().getSystemService(
                    Context.USER_SERVICE);
            final UserInfo info = Utils.getExistingUser(userManager,
                    android.os.Process.myUserHandle());
            controller.setLabel(info.name);
            controller.setIcon(
                    com.android.settingslib.Utils.getUserIcon(getActivity(), userManager, info));
        }

        controller.done(context, true /* rebindActions */);
    }
}
