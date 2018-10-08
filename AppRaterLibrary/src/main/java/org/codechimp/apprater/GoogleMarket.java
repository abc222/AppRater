package org.codechimp.apprater;

import android.content.Context;
import android.net.Uri;

public class GoogleMarket {

    private static String marketLink = "market://details?id=";

    protected static String packageName;

    public Uri getMarketURI(Context context) {
        return Uri.parse(marketLink + GoogleMarket.getPackageName(context));
    }

    public void overridePackageName(String packageName) {
        GoogleMarket.packageName = packageName;
    }

    protected static String getPackageName(Context context) {
        if (GoogleMarket.packageName != null) {
            return GoogleMarket.packageName;
        }
        return context.getPackageName();
    }
}
