package com.android.settings.deviceinfo.aboutphone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

import com.android.settingslib.DeviceInfoUtils;
import android.os.SELinux;
import android.os.SystemClock;

public class About extends Preference {

    private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
    private static final String FILENAME_PROC_VERSION = "/proc/version";
    static String aproxStorage;
    private long lastTouchTime = 0;
    private long currentTouchTime = 0;

    public About(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(context.getResources().
                getIdentifier("layout/nusantara_firmware_version", null, context.getPackageName()));

    }

    // device name
    public static String getDeviceModel() {
        FutureTask<String> msvSuffixTask = new FutureTask<>(() -> DeviceInfoUtils.getMsvSuffix());

        msvSuffixTask.run();
        try {
            // Wait for msv suffix value.
            final String msvSuffix = msvSuffixTask.get();
            return Build.MODEL + msvSuffix;
        } catch (ExecutionException e) {
            Log.e(TAG, "Execution error, so we only show model name");
        } catch (InterruptedException e) {
            Log.e(TAG, "Interruption error, so we only show model name");
        }
        // If we can't get an msv suffix value successfully,
        // it's better to return model name.
        return Build.MODEL;
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

    // total RAM
    public static String getTotalRAM() {
        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam = 0;
        String lastValue = "";
        try {
            try {
                reader = new RandomAccessFile("/proc/meminfo", "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
                // System.out.println("Ram : " + value);
            }
            reader.close();

            totRam = Double.parseDouble(value);
            // totRam = totRam / 1024;

            double mb = (totRam / 1024) + 0.1f;
            double gb = (totRam / 1048576) + 0.1f;
            double tb = (totRam / 1073741824) + 0.1f;
            int MB = (int) Math.round(mb);
            int GB = (int) Math.round(gb);
            int TB = (int) Math.round(tb);

            if (tb > 1) {
                lastValue = twoDecimalForm.format(TB).concat("TB");
            } else if (gb > 1) {
                lastValue = twoDecimalForm.format(GB).concat("GB");
            } else if (mb > 1) {
                lastValue = twoDecimalForm.format(MB).concat("MB");
            } else {
                lastValue = twoDecimalForm.format(totRam).concat("KB");
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }

        return lastValue;
    }

    // total Internal
    public static String getTotalInternalMemorySize() {
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

    /**
     * Reads a line from the specified file.
     *
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
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

    // build prop
    private static void setInfo(String prop, TextView textview) {
        if (TextUtils.isEmpty(getSystemProperty(prop))) {
            textview.setText("Unknown");
        } else {
            textview.setText(getSystemProperty(prop));
        }

    }

    // wifi address
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // kernel
    private String getFullKernelVersion() {
        String procVersionStr;
        try {
            procVersionStr = readLine(FILENAME_PROC_VERSION);
            return procVersionStr;
        } catch (IOException e) {
            Log.e(TAG,
                    "IO Exception when getting kernel version for Device Info screen", e);
            return "Unavailable";
        }
    }

    private String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan1")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = false;
        final Context context = getContext();

        TextView deviceName = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/device_name", null, context.getPackageName()));
        TextView androidVersion = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/android_version", null, context.getPackageName()));
        TextView nadVersion = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/nad_version", null, context.getPackageName()));
        final TextView buildNumber = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/build_number", null, context.getPackageName()));
        final TextView baseBand = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/base_band", null, context.getPackageName()));
        TextView selinux = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/selinux_status", null, context.getPackageName()));
        TextView security = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/security_update", null, context.getPackageName()));
        final TextView kernel = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/kernel_info", null, context.getPackageName()));
        final TextView strogeInfo = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/storage_info", null, context.getPackageName()));
        final TextView chipsetInfo = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/chipset_info", null, context.getPackageName()));
        final TextView processorInfo = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/processor_info", null, context.getPackageName()));
        final TextView screenInfo = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/screen_info", null, context.getPackageName()));
        final TextView mIpAddress = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/ip_address", null, context.getPackageName()));
        final TextView mMacAddress = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/mac_address", null, context.getPackageName()));
        final TextView upTime = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/uptime", null, context.getPackageName()));


        final GridLayout cL = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/info_device", null, context.getPackageName()));

        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

        LinearLayout namaHP = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_nama_hape", null, context.getPackageName()));
        namaHP.setClickable(true);
        namaHP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$HardwareInfoActivity"));
                context.startActivity(intent);
            }
        });

        LinearLayout versiAN = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_versi_android", null, context.getPackageName()));
        versiAN.setClickable(true);
        versiAN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();

                if (currentTouchTime - lastTouchTime < 250) {
                    Log.d("Duble", "Click");
                    lastTouchTime = 0;
                    currentTouchTime = 0;
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$FirmwareVersionActivity"));
                    context.startActivity(intent);
                } else {

                    String[] randomStrings = new String[]{"and again", "Are U crazy ??", "Yes U bad.", "LOL!", "Faster!", "So bad", "Again!", "Press again"};
                    Toast.makeText(context.getApplicationContext(), randomStrings[new Random().nextInt(randomStrings.length)], Toast.LENGTH_SHORT).show();
                }

            }
        });

        LinearLayout lnadVersion = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_nad_version", null, context.getPackageName()));
        lnadVersion.setClickable(true);
        lnadVersion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Dialog customDialog = new Dialog(getContext());
                customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                customDialog.setContentView(context.getResources().
                        getIdentifier("layout/dialog_nad", null, context.getPackageName()));
                TextView dialogBuildDate = customDialog.findViewById(context.getResources().
                        getIdentifier("id/dialog_build_date", null, context.getPackageName()));
                setInfo("ro.nad.build.date", dialogBuildDate);
                TextView dialogNadCode = customDialog.findViewById(context.getResources().
                        getIdentifier("id/dialog_nad_codename", null, context.getPackageName()));
                setInfo("ro.nad.build_codename", dialogNadCode);
                TextView dialogNadVer = customDialog.findViewById(context.getResources().
                        getIdentifier("id/dialog_nad_version", null, context.getPackageName()));
                dialogNadVer.setText(String.format("v%s", getSystemProperty("ro.nad.build.version")));
                TextView dialogBuildType = customDialog.findViewById(context.getResources().
                        getIdentifier("id/dialog_build_type", null, context.getPackageName()));
                setInfo("ro.nad.build.type", dialogBuildType);
                customDialog.setCancelable(true);
                customDialog.show();
            }
        });

        LinearLayout lbuildNumber = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_build_number", null, context.getPackageName()));
        lbuildNumber.setClickable(true);
        lbuildNumber.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder customDialog = new AlertDialog.Builder(getContext());
                customDialog.setTitle("Build Number");
                customDialog.setMessage(String.format(getSystemProperty("ro.system.build.id")));
                customDialog.setIcon(context.getResources().
                        getIdentifier("drawable/ic_a_build_number", null, context.getPackageName()));
                AlertDialog alertDialog = customDialog.create();
                alertDialog.show();
                buildNumber.setSelected(true);
            }
        });

        final LinearLayout lbaseBand = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_base_band", null, context.getPackageName()));
        lbaseBand.setClickable(true);
        lbaseBand.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder customDialog = new AlertDialog.Builder(getContext());
                customDialog.setTitle("Base Band");
                customDialog.setMessage(String.format(getSystemProperty("gsm.version.baseband")));
                customDialog.setIcon(context.getResources().
                        getIdentifier("drawable/ic_a_baseband", null, context.getPackageName()));
                AlertDialog alertDialog = customDialog.create();
                alertDialog.show();
                baseBand.setSelected(true);
            }
        });

        LinearLayout lSelinux = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_selinux", null, context.getPackageName()));
        lSelinux.setClickable(false);

        LinearLayout lSecure = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_secure", null, context.getPackageName()));
        lSecure.setClickable(true);
        lSecure.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://source.android.com/security/bulletin/"));
                context.startActivity(intent);
            }
        });

        LinearLayout lKernel = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_kernel", null, context.getPackageName()));
        lKernel.setClickable(true);
        lKernel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                kernel.setSelected(true);
                AlertDialog.Builder customDialog = new AlertDialog.Builder(getContext());
                customDialog.setTitle("Kernel");
                customDialog.setMessage(getFullKernelVersion());
                customDialog.setIcon(context.getResources().
                        getIdentifier("drawable/ic_a_kernel", null, context.getPackageName()));
                AlertDialog alertDialog = customDialog.create();
                alertDialog.show();
            }
        });

        LinearLayout moreInfo = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_more_info", null, context.getPackageName()));
        moreInfo.setClickable(true);
        moreInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$MyDeviceInfoActivity"));
                context.startActivity(intent);
            }
        });

        LinearLayout mac = holder.itemView.findViewById(context.getResources().
                getIdentifier("id/l_mac_address", null, context.getPackageName()));
        mac.setClickable(true);
        mac.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$SavedAccessPointsSettingsActivity"));
                context.startActivity(intent);
            }
        });

        /*
        imgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cL.getVisibility() == View.INVISIBLE) {//In transition: (alpha from 0 to 0.5)
                    lN.setAlpha(1f);
                    lN.setVisibility(View.VISIBLE);
                    lN.animate()
                            .alpha(0f)
                            .setDuration(2000)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {

                                    lN.setVisibility(View.INVISIBLE);
                                    cL.setVisibility(View.VISIBLE);
                                }
                            });
                    cL.setAlpha(0f);
                    cL.animate()
                            .alpha(1f)
                            .setDuration(2000)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    cL.setVisibility(View.VISIBLE);
                                    lN.setVisibility(View.INVISIBLE);
                                }
                            });

                } else if (cL.getVisibility() == View.VISIBLE) {//In transition: (alpha from 0 to 0.5)
                    cL.setAlpha(1f);
                    cL.setVisibility(View.VISIBLE);
                    cL.animate()
                            .alpha(0f)
                            .setDuration(2000)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    cL.setVisibility(View.INVISIBLE);
                                    lN.setVisibility(View.VISIBLE);
                                }
                            });

                    lN.setAlpha(0f);
                    lN.animate()
                            .alpha(1f)
                            .setDuration(2000)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    lN.setVisibility(View.VISIBLE);
                                    cL.setVisibility(View.INVISIBLE);
                                }
                            });

                } else {
                    return;
                }

            }
        });*/

         // selinux status
         if (SELinux.isSELinuxEnforced()) {
         selinux.setText(context.getResources().
         getIdentifier("string/selinux_status_enforcing", null, context.getPackageName()));
         } else if (!SELinux.isSELinuxEnforced()) {
         selinux.setText(context.getResources().
         getIdentifier("string/selinux_status_permissive", null, context.getPackageName()));
         } else {
         selinux.setText(context.getResources().
         getIdentifier("string/selinux_status_enforcing", null, context.getPackageName()));
         }

        // up time
        long ut = Math.max((SystemClock.elapsedRealtime() / 1000), 1);
        final StringBuilder summary = new StringBuilder();
        summary.append(DateUtils.formatElapsedTime(SystemClock.elapsedRealtime() / 1000));

        upTime.setText(summary.toString());
        upTime.setSelected(true);
        mMacAddress.setText(getMacAddr());
        mIpAddress.setText(getLocalIpAddress());
        mMacAddress.setSelected(true);
        mIpAddress.setSelected(true);
        nadVersion.setSelected(true);
        setInfo("ro.product.model", deviceName);
        deviceName.setText(getDeviceModel());
        setInfo("ro.build.version.release", androidVersion);
        setInfo("gsm.version.baseband", baseBand);
        setInfo("ro.system.build.id", buildNumber);
        setInfo("ro.build.version.security_patch", security);
        kernel.setText(DeviceInfoUtils.getFormattedKernelVersion(context));
        processorInfo.setText(getCpuInfoMap().get("Processor"));
        screenInfo.setText(getScreenRes(context) + " pixels / " + getDisplaySize(context) + " inch Display");
        chipsetInfo.setText(getCpuInfoMap().get("Hardware"));
        strogeInfo.setText(getBatteryCapacity(context) + "mAh + " + getTotalRAM() + " RAM / " + getTotalInternalMemorySize() + "GB ROM");
        setInfoNad("ro.nad.build.version", "ro.nad.build_codename", "ro.nad.build.type", nadVersion);

    }

}
