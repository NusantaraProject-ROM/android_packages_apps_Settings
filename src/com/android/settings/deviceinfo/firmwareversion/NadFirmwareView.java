/*
 * Copyright (C) 2021 Nusantara Android Development
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
 
package com.android.settings.deviceinfo.firmwareversion;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import java.util.Random;

public class NadFirmwareView extends Preference {

    public NadFirmwareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(context.getResources().
                getIdentifier("layout/nad_firmware_view", null, context.getPackageName()));

    }

    // system prop
    public static String getSystemProperty(String key) {
        String value = null;

        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    // nad system prop
    private static void setInfoNad(String prop, String prop2, String prop3, TextView textview) {
        if (TextUtils.isEmpty(getSystemProperty(prop))) {
            textview.setText("Unknown");
        } else {
            textview.setText(String.format("v%s | %s | %s", getSystemProperty(prop), getSystemProperty(prop2), getSystemProperty(prop3)));
        }
    }

    // device name
    private static void setInfo(String prop, TextView textview) {
        if (TextUtils.isEmpty(getSystemProperty(prop))) {
            textview.setText("Unknown");
        } else {
            textview.setText(getSystemProperty(prop));
        }

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final boolean selectable = false;
        final Context context = getContext();
        TextView androidVersion = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/android_version", null, context.getPackageName()));
        TextView nadVersion = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/nad_version", null, context.getPackageName()));
        nadVersion.setSelected(true);
        TextView buildNumber = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/build_number", null, context.getPackageName()));
        buildNumber.setSelected(true);
        TextView maintainerName = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/maintainer_name", null, context.getPackageName()));
        maintainerName.setSelected(true);

        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

        LinearLayout versiAN = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_versi_android", null, context.getPackageName()));
        versiAN.setClickable(true);
        versiAN.setOnClickListener(view -> {
            String[] randomStrings = new String[]{"Android Snow Cone", "Nusantara Android Development", "Android 12", "Material You Design", "Silky Style", "Nusantara Project OS"};
            Toast.makeText(context.getApplicationContext(), randomStrings[new Random().nextInt(randomStrings.length)], Toast.LENGTH_SHORT).show();

        });

        LinearLayout lnadVersion = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_nad_version", null, context.getPackageName()));
        lnadVersion.setClickable(true);
        lnadVersion.setOnClickListener(v -> {

            Dialog customDialog = new Dialog(getContext(), R.style.CustomDialog);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setCanceledOnTouchOutside(true);
            Window window = customDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.getDecorView().setSystemUiVisibility(uiOptions);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.7F);
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            customDialog.setContentView(context.getResources().
                    getIdentifier("layout/nad_version_dialog", null, context.getPackageName()));
            TextView dialogBuildDate = customDialog.findViewById(context.getResources().
                    getIdentifier("id/dialog_build_date", null, context.getPackageName()));
            setInfo("ro.nad.build.date", dialogBuildDate);
            TextView dialogNadCode = customDialog.findViewById(context.getResources().
                    getIdentifier("id/dialog_nad_codename", null, context.getPackageName()));
            setInfo("ro.nad.build_codename", dialogNadCode);
            TextView dialogNadVer = customDialog.findViewById(context.getResources().
                    getIdentifier("id/dialog_nad_version", null, context.getPackageName()));
            dialogNadVer.setText(String.format("v%s", getSystemProperty("ro.nad.build.version")));
            customDialog.setCancelable(true);

            ImageView mGithub = customDialog.findViewById(context.getResources().
                    getIdentifier("id/goGithub", null, context.getPackageName()));
            ImageView mTwitter = customDialog.findViewById(context.getResources().
                    getIdentifier("id/goTwitter", null, context.getPackageName()));
            ImageView mTelegram = customDialog.findViewById(context.getResources().
                    getIdentifier("id/goTelegram", null, context.getPackageName()));
            ImageView mInstagram = customDialog.findViewById(context.getResources().
                    getIdentifier("id/goInstagram", null, context.getPackageName()));
            ImageView mNusantara = customDialog.findViewById(context.getResources().
                    getIdentifier("id/goNad", null, context.getPackageName()));

            customDialog.show();
            mGithub.setOnClickListener(v1 -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/Nusantara-ROM"));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            });
            mTwitter.setOnClickListener(v1 -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/NusantaraROM"));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            });
            mTelegram.setOnClickListener(v1 -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://t.me/NusantaraCommunity"));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            });
            mInstagram.setOnClickListener(v1 -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.instagram.com/nusantararom/"));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            });

            mNusantara.setOnClickListener(v1 -> {
                ValueAnimator anim = new ValueAnimator();
                anim.setIntValues(Color.LTGRAY, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED);
                anim.setEvaluator(new ArgbEvaluator());
                anim.addUpdateListener(valueAnimator -> mNusantara.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.addUpdateListener(valueAnimator -> mInstagram.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.addUpdateListener(valueAnimator -> mGithub.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.addUpdateListener(valueAnimator -> mTelegram.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.addUpdateListener(valueAnimator -> mTwitter.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.setDuration(5000);
                anim.start();
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://nusantararom.org/"));
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                        anim.cancel();
                        mGithub.setColorFilter(null);
                        mInstagram.setColorFilter(null);
                        mTwitter.setColorFilter(null);
                        mTelegram.setColorFilter(null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }


                });
            });
        });


        LinearLayout lNameMaintener = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_nama_maintener", null, context.getPackageName()));
        lNameMaintener.setClickable(true);
        lNameMaintener.setOnClickListener(v -> {
           // Intent intent = new Intent();
        //    intent.setComponent(new ComponentName("com.android.settings", "com.nusantara.wings.fragments.team.TeamActivity"));
         //   context.startActivity(intent);
         Dialog customDialog = new Dialog(getContext(), R.style.CustomDialog);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setCanceledOnTouchOutside(true);
            Window window = customDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.getDecorView().setSystemUiVisibility(uiOptions);
            window.setBackgroundDrawableResource(R.drawable.bottom_sheet_background);
            window.setDimAmount(0.7F);
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            customDialog.setContentView(context.getResources().
                    getIdentifier("layout/maintener_layout", null, context.getPackageName()));
         customDialog.setCancelable(true);
        
         TextView devName = customDialog.findViewById(context.getResources().
                    getIdentifier("id/mName", null, context.getPackageName()));
            ImageView git = customDialog.findViewById(context.getResources().
                    getIdentifier("id/devGithub", null, context.getPackageName()));
            ImageView tele = customDialog.findViewById(context.getResources().
                    getIdentifier("id/devTelegram", null, context.getPackageName()));
            
            ImageView mProfile = customDialog.findViewById(context.getResources().
                    getIdentifier("id/mProfile", null, context.getPackageName()));
            devName.setText(context.getResources().getString(R.string.maintainer_name));
            String teleLink = context.getResources().getString(R.string.telegram_username);
            String gitLink = context.getResources().getString(R.string.github_username);
                    
                    
         customDialog.show();
         
        tele.setOnClickListener(v1 -> {
        	try {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://t.me/" + teleLink));
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
        });
         
        git.setOnClickListener(v1 -> {
        	try {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/" + gitLink));
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
        });
        
        });


        LinearLayout lbuildNumber = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_build_number", null, context.getPackageName()));
        lbuildNumber.setClickable(true);
        lbuildNumber.setOnClickListener(v -> {

            // Custom Title
            TextView mTitle = new TextView(context);
            mTitle.setText("Build Number");
            mTitle.setPadding(5, 35, 5, 10);
            mTitle.setTextSize(26);
            mTitle.setTypeface(Typeface.DEFAULT_BOLD);
            mTitle.setGravity(Gravity.CENTER);

            // Custom Message
            TextView mMessage = new TextView(context);
            mMessage.setText(String.format(getSystemProperty("ro.system.build.id")));
            mMessage.setGravity(Gravity.CENTER);

            AlertDialog.Builder customDialog = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
            customDialog.setCustomTitle(mTitle);
            customDialog.setView(mMessage);
            customDialog.setIcon(context.getResources().
                    getIdentifier("drawable/ic_build_number", null, context.getPackageName()));
            AlertDialog alertDialog = customDialog.create();
            alertDialog.show();
            alertDialog.getWindow().setGravity(Gravity.BOTTOM);
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            alertDialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            alertDialog.getWindow().setDimAmount(0.7F);
            alertDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.bottom_sheet_background);

            final Handler handler = new Handler();
            final Runnable runnable = () -> {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            };
            alertDialog.setOnDismissListener(dialog -> handler.removeCallbacks(runnable));

            handler.postDelayed(runnable, 2000);
            buildNumber.setSelected(true);
        });

        setInfo("ro.build.version.release", androidVersion);
        setInfoNad("ro.nad.build.version", "ro.nad.build_codename", "ro.nad.build.type", nadVersion);
        setInfo("ro.system.build.id", buildNumber);
    }

}

