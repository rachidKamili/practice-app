package med.kamili.rachid.practiceapp;

import android.app.Application;

import med.kamili.rachid.practiceapp.utils.CrashReportingTree;
import timber.log.Timber;

public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }
}
