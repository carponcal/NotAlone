package com.carponcal.notalone;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;


/**Created by carlos on 20/11/2017.
 * Lanza Servicio en segundo plano o no en funcion de si la app está en segundo plano o no
 */
public class JobService extends android.app.job.JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Intent service = new Intent(getApplicationContext(), WatchService.class);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            if (isAppOnForeground(getApplicationContext()))
                getApplicationContext().startService(service);
            else
                getApplicationContext().startForegroundService(service);//whitout notification => only 5 seconds alive
        else
            getApplicationContext().startService(service);
        Util.scheduleJob(getApplicationContext()); // reschedule the job performed in the main thread,
        //if you start asynchronous processing in this method, return true otherwise false.
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //if something wrong => true to restart
        return true;
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}
