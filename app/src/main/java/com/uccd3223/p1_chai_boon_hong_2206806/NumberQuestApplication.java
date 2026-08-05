package com.uccd3223.p1_chai_boon_hong_2206806;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class NumberQuestApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private int startedActivities;
    private boolean changingConfiguration;

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        GameAudioManager.get(this);
    }

    @Override public void onActivityStarted(Activity activity) {
        if (startedActivities++ == 0 && !changingConfiguration) GameAudioManager.get(this).setAppForeground(true);
        changingConfiguration = false;
    }

    @Override public void onActivityStopped(Activity activity) {
        changingConfiguration = activity.isChangingConfigurations();
        if (--startedActivities == 0 && !changingConfiguration) GameAudioManager.get(this).setAppForeground(false);
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) { }
    @Override public void onActivityResumed(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
    @Override public void onActivityDestroyed(Activity activity) { }
}
