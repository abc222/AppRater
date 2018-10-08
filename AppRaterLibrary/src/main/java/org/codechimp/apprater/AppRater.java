package org.codechimp.apprater;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class AppRater {

    public static final String TAG = "AppRater";

    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREF_REMIND_LATER = "remindmelater";
    private final static String PREF_APP_VERSION_NAME = "app_version_name";
    private final static String PREF_APP_VERSION_CODE = "app_version_code";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;
    private static int DAYS_UNTIL_PROMPT_FOR_REMIND_LATER = 3;
    private static int LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER = 7;
    private static boolean isDark;
    private static boolean themeSet;
    private static boolean isVersionNameCheckEnabled;
    private static boolean isVersionCodeCheckEnabled;
    private static boolean isCancelable = true;

    /**
     * 策略调度 总展示次数 展示时间间隔
     */
    private static int TOTAL_SHOW_NUM = 10;
    private static long SHOW_TIME_INTERVAL = 24 * 60 * 60 * 1000;

    private static GoogleMarket googleMarket = new GoogleMarket();

    /**
     * Decides if the version name check is active or not
     *
     * @param versionNameCheck
     */
    public static void setVersionNameCheckEnabled(boolean versionNameCheck) {
        isVersionNameCheckEnabled = versionNameCheck;
    }

    /**
     * Decides if the version code check is active or not
     *
     * @param versionCodeCheck
     */
    public static void setVersionCodeCheckEnabled(boolean versionCodeCheck) {
        isVersionCodeCheckEnabled = versionCodeCheck;
    }

    /**
     * sets number of day until rating dialog pops up for next time when remind
     * me later option is chosen
     *
     * @param daysUntilPromt
     */
    public static void setNumDaysForRemindLater(int daysUntilPromt) {
        DAYS_UNTIL_PROMPT_FOR_REMIND_LATER = daysUntilPromt;
    }

    /**
     * sets the number of launches until the rating dialog pops up for next time
     * when remind me later option is chosen
     *
     * @param launchesUntilPrompt
     */
    public static void setNumLaunchesForRemindLater(int launchesUntilPrompt) {

        LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER = launchesUntilPrompt;
    }

    /**
     * sets whether the rating dialog is cancelable or not, default is true.
     *
     * @param cancelable
     */
    public static void setCancelable(boolean cancelable) {
        isCancelable = cancelable;
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default day, launch count
     * values and checking if the version is changed or not
     *
     * @param context
     */
    public static void app_launched(Context context) {
        app_launched(context, DAYS_UNTIL_PROMPT, LAUNCHES_UNTIL_PROMPT);
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default day, launch count
     * values with additional day and launch parameter for remind me later option
     * and checking if the version is changed or not
     *
     * @param context
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     * @param daysForRemind
     * @param launchesForRemind
     */
    public static void app_launched(Context context, int daysUntilPrompt, int launchesUntilPrompt, int daysForRemind, int launchesForRemind) {
        setNumDaysForRemindLater(daysForRemind);
        setNumLaunchesForRemindLater(launchesForRemind);
        app_launched(context, daysUntilPrompt, launchesUntilPrompt);
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     *
     * @param context
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     */
    public static void app_launched(Context context, int daysUntilPrompt, int launchesUntilPrompt) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        int days;
        int launches;
        if (isVersionNameCheckEnabled) {
            if (!ratingInfo.getApplicationVersionName().equals(prefs.getString(PREF_APP_VERSION_NAME, "none"))) {
                editor.putString(PREF_APP_VERSION_NAME, ratingInfo.getApplicationVersionName());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (isVersionCodeCheckEnabled) {
            if (ratingInfo.getApplicationVersionCode() != (prefs.getInt(PREF_APP_VERSION_CODE, -1))) {
                editor.putInt(PREF_APP_VERSION_CODE, ratingInfo.getApplicationVersionCode());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
            return;
        } else if (prefs.getBoolean(PREF_REMIND_LATER, false)) {
            days = DAYS_UNTIL_PROMPT_FOR_REMIND_LATER;
            launches = LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER;
        } else {
            days = daysUntilPrompt;
            launches = launchesUntilPrompt;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);
        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREF_FIRST_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        }
        // Wait for at least the number of launches or the number of days used
        // until prompt
//        if (launch_count >= launches || (System.currentTimeMillis() >= date_firstLaunch + (days * 24 * 60 * 60 * 1000))) {
//            showRateAlertDialog(context, editor);
//        }
        if (launch_count >= launches || (System.currentTimeMillis() >= date_firstLaunch + SHOW_TIME_INTERVAL)) {
            showRateAlertDialog(context, editor);
        }
        commitOrApply(editor);
    }

    /**
     * Call this method directly if you want to force a rate prompt, useful for
     * testing purposes
     *
     * @param context
     */
    public static void showRateDialog(final Context context) {
        showRateAlertDialog(context, null);
    }

    /**
     * Call this method directly to go straight to play store listing for rating
     *
     * @param context
     */
    public static void rateNow(final Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, googleMarket.getMarketURI(context)));
        } catch (ActivityNotFoundException activityNotFoundException1) {
            Log.e(AppRater.class.getSimpleName(), "GoogleMarket Intent not found");
        }
    }

    public static void setPackageName(String packageName) {
        AppRater.googleMarket.overridePackageName(packageName);
    }


    /**
     * Sets dialog theme to dark
     */
    @TargetApi(11)
    public static void setDarkTheme() {
        isDark = true;
        themeSet = true;
    }

    /**
     * Sets dialog theme to light
     */
    @TargetApi(11)
    public static void setLightTheme() {
        isDark = false;
        themeSet = true;
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    @SuppressLint("NewApi")
    private static void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);

        final RatingDialog ratingDialog = new RatingDialog(context);

        ratingDialog.setOnDialogClickListener(new RatingDialog.OnDialogClickListener() {
            @Override
            public void onEncourageButtonClickListener() {
                Log.d(TAG, "stars: " + ratingDialog.stars);
                if (ratingDialog.stars < 3) {
                    if (editor != null) {
//                      editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
//                      editor.putBoolean(PREF_REMIND_LATER, false);
                        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
                        editor.putBoolean(PREF_REMIND_LATER, true);
                        long date_firstLaunch = System.currentTimeMillis();
                        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                        editor.putLong(PREF_LAUNCH_COUNT, 0);
                        commitOrApply(editor);
                    }
                    ratingDialog.dismiss();
                } else {
                    rateNow(context);
                    if (editor != null) {
                        editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                        commitOrApply(editor);
                    }
                    ratingDialog.dismiss();
                }
            }
        });
        ratingDialog.show();
    }


    @SuppressLint("NewApi")
    private static void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static void resetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
        editor.putBoolean(PREF_REMIND_LATER, false);
        editor.putLong(PREF_LAUNCH_COUNT, 0);
        long date_firstLaunch = System.currentTimeMillis();
        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        commitOrApply(editor);
    }
}
