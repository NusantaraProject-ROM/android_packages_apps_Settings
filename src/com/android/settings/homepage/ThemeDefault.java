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

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.pm.UserInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.internal.util.UserIcons;
import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.homepage.contextualcards.ContextualCardsFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.drawable.CircleFramedDrawable;

import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class ThemeDefault extends AppCompatActivity {

    Context context;
    TextView nameView;
    ImageView avatarView;
    UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();
        mUserManager = context.getSystemService(UserManager.class);

        setContentView(R.layout.activity_default);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout2);
        toolBarLayout.setTitle(getTitle());

        final LinearLayout sbar = findViewById(R.id.cari);
        sbar.setClickable(true);
        sbar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(FeatureFactory.getFactory(context).getSearchFeatureProvider()
                        .buildSearchIntent(context /* activity */, SettingsEnums.SETTINGS_HOMEPAGE));
            }
        });
        
        nameView = findViewById(R.id.user_name);
        
        final UserInfo info = Utils.getExistingUser(mUserManager,
                android.os.Process.myUserHandle());
        nameView.setText(info.name);
        nameView.setSelected(true);
        avatarView = findViewById(R.id.account_avatar);
        //final AvatarViewMixin avatarViewMixin = new AvatarViewMixin(this, avatarView);
        avatarView.setImageDrawable(getCircularUserIcon(context));
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$UserSettingsActivity"));
                startActivity(intent);
            }
        });

        Button button1 = (Button) findViewById(R.id.theme12);
        button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("PrefsFile", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit(); //opens the editor
                editor.putString("tema", "TEMA_SATU");
                editor.commit();
            }
        });

        Button button2 = (Button) findViewById(R.id.theme22);
        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("PrefsFile", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit(); //opens the editor
                editor.putString("tema", "TEMA_DEFAULT");
                editor.commit();
            }
        });
        
        if (!getSystemService(ActivityManager.class).isLowRamDevice()) {
            // Only allow contextual feature on high ram devices.
            showFragment(new ContextualCardsFragment(), R.id.contextual_cards_content2);
        }
        showFragment(new TopLevelSettings(), R.id.main_content2);
        ((FrameLayout) findViewById(R.id.main_content2))
                .getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }
    
    private void showFragment(Fragment fragment, int id) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final Fragment showFragment = fragmentManager.findFragmentById(id);

        if (showFragment == null) {
            fragmentTransaction.add(id, fragment);
        } else {
            fragmentTransaction.show(showFragment);
        }
        fragmentTransaction.commit();
    }

    private Drawable getCircularUserIcon(Context context) {
        Bitmap bitmapUserIcon = mUserManager.getUserIcon(UserHandle.myUserId());

        if (bitmapUserIcon == null) {
            // get default user icon.
            final Drawable defaultUserIcon = UserIcons.getDefaultUserIcon(
                    context.getResources(), UserHandle.myUserId(), false);
            bitmapUserIcon = UserIcons.convertToBitmap(defaultUserIcon);
        }
        Drawable drawableUserIcon = new CircleFramedDrawable(bitmapUserIcon,
                (int) context.getResources().getDimension(R.dimen.circle_avatar_size));

        return drawableUserIcon;
    }

    @Override
    public void onResume() {
        super.onResume();
        avatarView.setImageDrawable(getCircularUserIcon(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        final MenuItem itemMyProfile = menu.findItem(R.id.act_search);
        final Menu finalMenu = menu;

        AppBarLayout appBarLayout = findViewById(R.id.app_bar2);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0)
                {
                    //  Collapsed
                    itemMyProfile.setVisible(true);



                }
                else
                {
                    //Expanded
                    itemMyProfile.setVisible(false);



                }
            }
        });
        
        itemMyProfile.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalMenu.performIdentifierAction(itemMyProfile.getItemId(), 0);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.act_search) {
                startActivity(FeatureFactory.getFactory(context).getSearchFeatureProvider()
                        .buildSearchIntent(context /* activity */, SettingsEnums.SETTINGS_HOMEPAGE));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}