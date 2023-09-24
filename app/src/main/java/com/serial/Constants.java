package com.serial;

import com.hoverdroid.BuildConfig;

public class Constants {

    // values have to be globally unique
    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    public static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    public static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    public static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    // values have to be unique within each app
    public  static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    public  Constants() {}
}
