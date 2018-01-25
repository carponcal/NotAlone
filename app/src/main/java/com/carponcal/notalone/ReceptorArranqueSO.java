package com.carponcal.notalone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by carlos on 20/11/2017.
 * Lanza servicio o scheduler para gestionar el servicio de notificaciones
 */


public class ReceptorArranqueSO extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ReceptorArranque", "onReceive: Se lanza el receptor" );
        /*Si la versión es menor que oreo lanzo el servicio*/
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            context.startService(new Intent(context, WatchService.class));
        }
        /*Si la versión es oreo o superior lanzo el scheduler ya que ahora el sistema cierre los servicios cada 10 segundos*/
        else {
            Util.scheduleJob(context);
        }
    }
}
