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

import android.content.Intent;
import android.os.Bundle;
import com.android.settings.R;
import androidx.fragment.app.FragmentActivity;

public class SettingsHomepageActivity extends FragmentActivity {
    
    /* access modifiers changed from: protected */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_start);

        String launchinfo = getSharedPreferences("PrefsFile", MODE_PRIVATE).getString("tema", "TEMA_DEFAULT");

        if (launchinfo.equals("TEMA_SATU")) {

            Thread splashTread = new Thread() {
                @Override
                public void run() {
                    try {

                        Thread.sleep(400);
                    } catch (Exception e) {
                    } finally {

                        startActivity(new Intent(SettingsHomepageActivity.this,
                                ThemeA.class));
                        finish();
                    }
                }

            };
            splashTread.start();

        } else if (launchinfo.equals("TEMA_DUA")) {

            Thread splashTread = new Thread() {
                @Override
                public void run() {
                    try {

                        Thread.sleep(400);
                    } catch (Exception e) {
                    } finally {

                        startActivity(new Intent(SettingsHomepageActivity.this,
                                ThemeB.class));
                        finish();
                    }
                }

            };
            splashTread.start();
        } else if (launchinfo.equals("TEMA_DEFAULT")) {
        
                    Thread splashTread = new Thread() {
                        @Override
                        public void run() {
                            try {
        
                                Thread.sleep(400);
                            } catch (Exception e) {
                            } finally {
        
                                startActivity(new Intent(SettingsHomepageActivity.this,
                                        ThemeDefault.class));
                                finish();
                            }
                        }
        
                    };
                    splashTread.start();
                }

    }
    
}
