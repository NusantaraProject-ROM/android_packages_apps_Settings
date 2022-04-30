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
import android.content.ContentResolver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.Display;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.provider.Settings;
import com.android.settingslib.Utils;
import com.android.settings.utils.SecureSettings;


import static android.content.ContentValues.TAG;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import java.util.Random;

public class NadFirmwareView extends Preference {
	
	private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
    private static final String FILENAME_PROC_VERSION = "/proc/version";
    static String aproxStorage;

    public NadFirmwareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(context.getResources().
                getIdentifier("layout/nad_firmware_view", null, context.getPackageName()));

    }
    
    // screen pixels
    public static String getScreenRes(Context context) {

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y + getNavigationBarHeight(windowManager);
        return width + " x " + height;
    }

    private static int getNavigationBarHeight(WindowManager wm) {
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        wm.getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }

    // screen inch
    public static String getDisplaySize(Context ctx) {
        double x = 0, y = 0;
        int mWidthPixels, mHeightPixels;
        try {
            WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
            mWidthPixels = realSize.x;
            mHeightPixels = realSize.y;
            DisplayMetrics dm = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(dm);
            x = Math.pow(mWidthPixels / dm.xdpi, 2);
            y = Math.pow(mHeightPixels / dm.ydpi, 2);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return String.format(Locale.US, "%.2f", Math.sqrt(x + y));
    }

    // processor info
    public static Map<String, String> getCpuInfoMap() {
        Map<String, String> map = new HashMap<String, String>();
        try {
            Scanner s = new Scanner(new File("/proc/cpuinfo"));
            while (s.hasNextLine()) {
                String[] vals = s.nextLine().split(": ");
                if (vals.length > 1) map.put(vals[0].trim(), vals[1].trim());
            }
        } catch (Exception e) {
            Log.e("getCpuInfoMap", Log.getStackTraceString(e));
        }
        return map;

    }

    // total ram
    public static String getTotalRAM() {
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";
    }

    // total Internal
    public static String getTotalROM() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        double total = (totalBlocks * blockSize) / 1073741824;
        int lastval = (int) Math.round(total);
        if (lastval > 0 && lastval <= 16) {
            aproxStorage = "16";
        } else if (lastval > 16 && lastval <= 32) {
            aproxStorage = "32";
        } else if (lastval > 32 && lastval <= 64) {
            aproxStorage = "64";
        } else if (lastval > 64 && lastval <= 128) {
            aproxStorage = "128";
        } else if (lastval > 128 && lastval <= 256) {
            aproxStorage = "256";
        } else if (lastval > 256 && lastval <= 512) {
            aproxStorage = "512";
        } else if (lastval > 512) {
            aproxStorage = "512+";
        } else aproxStorage = "null";
        return aproxStorage;
    }

    // battery capacity
    public static int getBatteryCapacity(Context context) {
        Object powerProfile = null;

        double batteryCapacity = 0;
        try {
            powerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(powerProfile, "battery.capacity");

        } catch (Exception e) {
            e.printStackTrace();
        }

        String str = Double.toString(batteryCapacity);
        String[] strArray = str.split("\\.");
        int batteryCapacityInt = Integer.parseInt(strArray[0]);

        return batteryCapacityInt;
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
        
        int colorSurface = Utils.getColorAttr(context,
                com.android.internal.R.attr.colorSurfaceHeader).getDefaultColor();
        int colorPrimary = Utils.getColorAttr(context,
                com.android.internal.R.attr.colorPrimary).getDefaultColor();
        int color = context.getResources().getColor(R.color.settingslib_colorSurfaceHeader);
        
        ContentResolver resolver = context.getContentResolver();
        boolean blurEnabled = Settings.System.getIntForUser(resolver, Settings.System.BLUR_STYLE_PREFERENCE_KEY, 0, -2) == 1;
        boolean clearEnabled = Settings.Secure.getInt(resolver, Settings.Secure.SYSTEM_NUSANTARA_THEME, 0) == 1;
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(28);
        gd.setAlpha(blurEnabled || clearEnabled || blurEnabled && clearEnabled ? 125  :  225);
        gd.setColor(blurEnabled || clearEnabled || blurEnabled && clearEnabled ? colorSurface :  colorPrimary);
        LinearLayout mView1 = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/view1", null, context.getPackageName()));
        LinearLayout mView2 = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/view2", null, context.getPackageName()));
        LinearLayout mView3 = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/view3", null, context.getPackageName()));
        LinearLayout mView4 = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/view4", null, context.getPackageName()));
        View mView5 = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/view5", null, context.getPackageName()));
                
        holder.itemView.setBackgroundColor(blurEnabled || clearEnabled || blurEnabled && clearEnabled ? Color.TRANSPARENT :  colorSurface);
        mView1.setBackground(gd);
        mView2.setBackground(gd);
        mView3.setBackground(gd);
        mView4.setBackground(gd);
        mView5.setVisibility(blurEnabled || clearEnabled || blurEnabled && clearEnabled ? View.GONE : View.VISIBLE);
        
        TextView chipsetInfo = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/cpu", null, context.getPackageName()));
        chipsetInfo.setSelected(true);
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

        LinearLayout chipsetIn = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_cpu", null, context.getPackageName()));
        chipsetIn.setClickable(true);
        chipsetIn.setOnClickListener(view -> {
            
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
                    getIdentifier("layout/cpu_layout", null, context.getPackageName()));
         customDialog.setCancelable(true);
        
         TextView deviceInfo  = customDialog.findViewById(context.getResources().
                    getIdentifier("id/device_codename", null, context.getPackageName()));
         TextView memoryInfo  = customDialog.findViewById(context.getResources().
                    getIdentifier("id/memory_info", null, context.getPackageName()));
         TextView cpuInfo   = customDialog.findViewById(context.getResources().
                    getIdentifier("id/cpu_info", null, context.getPackageName()));
         TextView procInfo   = customDialog.findViewById(context.getResources().
                    getIdentifier("id/proc_info", null, context.getPackageName()));
         TextView screenInfo   = customDialog.findViewById(context.getResources().
                    getIdentifier("id/screen_info", null, context.getPackageName()));
         TextView batteryInfo  = customDialog.findViewById(context.getResources().
                    getIdentifier("id/batt_info", null, context.getPackageName()));
            
            
            procInfo.setText(getCpuInfoMap().get("Processor"));
            screenInfo.setText(getScreenRes(context) + " pxls / " + getDisplaySize(context) + " inch");
            cpuInfo.setText(getCpuInfoMap().get("Hardware"));
            memoryInfo.setText(getTotalRAM() + " RAM / " + getTotalROM() + "GB ROM");
            batteryInfo.setText(getBatteryCapacity(context) + " mAh");
            setInfo("ro.product.device", deviceInfo);
                    
         customDialog.show();
        
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

            /*
            *ImageView mGithub = customDialog.findViewById(context.getResources().
             *      getIdentifier("id/goGithub", null, context.getPackageName()));
            *ImageView mTwitter = customDialog.findViewById(context.getResources().
             *      getIdentifier("id/goTwitter", null, context.getPackageName()));
            *ImageView mTelegram = customDialog.findViewById(context.getResources().
             *      getIdentifier("id/goTelegram", null, context.getPackageName()));
            *ImageView mInstagram = customDialog.findViewById(context.getResources().
             *      getIdentifier("id/goInstagram", null, context.getPackageName()));
            */
            ImageView mNusantara = customDialog.findViewById(context.getResources().
                    getIdentifier("id/goNad", null, context.getPackageName()));

            customDialog.show();
            /*
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
            */

            mNusantara.setOnClickListener(v1 -> {
                ValueAnimator anim = new ValueAnimator();
                anim.setIntValues(Color.LTGRAY, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED);
                anim.setEvaluator(new ArgbEvaluator());
                anim.addUpdateListener(valueAnimator -> mNusantara.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                /*
                anim.addUpdateListener(valueAnimator -> mInstagram.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.addUpdateListener(valueAnimator -> mGithub.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.addUpdateListener(valueAnimator -> mTelegram.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                anim.addUpdateListener(valueAnimator -> mTwitter.setColorFilter((Integer) valueAnimator.getAnimatedValue()));
                */
                anim.setDuration(4000);
                anim.start();
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://linktr.ee/nusantararom/"));
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                        anim.cancel();
                        /*
                        mGithub.setColorFilter(null);
                        mInstagram.setColorFilter(null);
                        mTwitter.setColorFilter(null);
                        mTelegram.setColorFilter(null);
                        */
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
            TextView git = customDialog.findViewById(context.getResources().
                    getIdentifier("id/devGithub", null, context.getPackageName()));
            TextView tele = customDialog.findViewById(context.getResources().
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

        chipsetInfo.setText(getCpuInfoMap().get("Hardware"));
        setInfoNad("ro.nad.build.version", "ro.nad.build_codename", "ro.nad.build.type", nadVersion);
        setInfo("ro.system.build.id", buildNumber);
    }

}

