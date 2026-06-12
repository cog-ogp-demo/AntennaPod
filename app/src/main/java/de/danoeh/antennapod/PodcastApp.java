package de.danoeh.antennapod;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.DynamicColors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

import de.danoeh.antennapod.storage.preferences.UserPreferences;

/** Main application class. */
public class PodcastApp extends Application {
    private static final String TAG = "PodcastApp";
    private int lastAccentColor;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashReportExceptionHandler());
        RxJavaErrorHandlerSetup.setupRxJavaErrorHandler();

        try {
            // Robolectric calls onCreate for every test, which causes problems with static members
            EventBus.builder()
                    .logNoSubscriberMessages(false)
                    .sendNoSubscriberEvent(false)
                    .installDefaultEventBus();
        } catch (EventBusException e) {
            Log.d(TAG, e.getMessage());
        }

        DynamicColors.applyToActivitiesIfAvailable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lastAccentColor = getColor(android.R.color.system_accent1_500);
            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        lastAccentColor = activity.getColor(android.R.color.system_accent1_500);
                    }
                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && UserPreferences.getIsThemeColorTinted()) {
                        int currentColor = activity.getColor(android.R.color.system_accent1_500);
                        if (currentColor != lastAccentColor) {
                            lastAccentColor = currentColor;
                            activity.recreate();
                        }
                    }
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                }
            });
        }

        ClientConfigurator.initialize(this);
        PreferenceUpgrader.checkUpgrades(this);
    }
}
