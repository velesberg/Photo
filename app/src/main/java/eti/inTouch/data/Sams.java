package eti.inTouch.data;

import android.app.Application;
import android.content.Context;

public class Sams extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        Sams.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Sams.context;
    }

}