package com.carponcal.notalone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by carlos on 28/12/2017.
 * Servicio para comprobar si las actividades en las que está apuntado el usuario están
 * cercanas a ejecutarse
 */

public class WatchService extends Service {
    private BroadcastReceiver tick_tack;
    HttpURLConnection con;
    SharedPreferences preferences;
    private String CHANNEL_ID = "canal_actividades";
    private int mNotificationIdDay = 2;
    private int mNotificationIdHour = 3;
    private int id_user;
    public WatchService() {
        super();
        if (!MainActivity.servicio_activo){
            MainActivity.servicio_activo = true;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TEST", "Start WatchService...");
        preferences = this.getSharedPreferences(
                "username", Context.MODE_PRIVATE);
        id_user = preferences.getInt("id", -1);
        tick_tack = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    if (MainActivity.my_activities == null){//Así solo se llama una vez en el arranque
                        try {
                            ConnectivityManager connMgr = (ConnectivityManager)
                                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                            if (networkInfo != null && networkInfo.isConnected()) {
                                String URL = "http://larctrobat.es/notalone/list_activities.php?id_user=" + id_user + "&my_activities=1";
                                new JsonTaskMyActivities(context).execute(new URL(URL));
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();

                        }
                    }
                    if (MainActivity.my_activities != null) {
                        checkActivities();
                    }
                }
            }
        };
        this.registerReceiver(tick_tack, new IntentFilter(Intent.ACTION_TIME_TICK));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(tick_tack);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class JsonTaskMyActivities extends AsyncTask<URL, Void, List<Activity_user>> {
        private Context context;
        public JsonTaskMyActivities(Context context) {
            this.context  = context;
        }

        @Override
        protected List<Activity_user> doInBackground(URL... urls) {
            List<Activity_user> activities = null;

            try {

                // Establecer la conexión
                con = (HttpURLConnection)urls[0].openConnection();
                con.setConnectTimeout(15000);
                con.setReadTimeout(10000);

                // Obtener el estado del recurso
                int statusCode = con.getResponseCode();

                if(statusCode!=200) {
                    activities = new ArrayList<>();
                    //activities.add(new Activity_user(0,"Error",null,null, null,0,0,"",0));

                }
                else {

                    // Parsear el flujo con formato JSON
                    InputStream in = new BufferedInputStream(con.getInputStream());

                    // JsonActivityParser parser = new JsonActivityParser();
                    JsonActivityParser parser = new JsonActivityParser();

                    activities = parser.leerFlujoJson(in);


                }

            } catch (Exception e) {
                e.printStackTrace();

            }finally {
                con.disconnect();
            }
            return activities;
        }

        @Override
        protected void onPostExecute(List<Activity_user> activities_user) {
            /*
            Asignar los objetos de Json parseados al adaptador
             */
            if(activities_user!=null) {
                /*DEBUG*//*
                for (int i= 0; i<activities_user.size();i++){
                    Log.e("activities_user", "Elemento: " + i + " -> " + activities_user.get(i).getName() );
                }
                for (int i= 0; i<MainActivity.my_activities.size();i++){
                    Log.e("Main.my_activities", "Elemento: " + i + " -> " + MainActivity.my_activities.get(i).getName() );
                }*/
                MainActivity.my_activities = activities_user;

                /*for (int i= 0; i<MainActivity.my_activities.size();i++){
                    Log.e("NEW Main.my_act", "Elemento: " + i + " -> " + MainActivity.my_activities.get(i).getName() +
                    "; Location: " + MainActivity.my_activities.get(i).getLocation().getLatitude() + "/" +
                            MainActivity.my_activities.get(i).getLocation().toString());
                }*/


                //adaptador = new ActivitiesSearchAdapter(getBaseContext(), activities_user);
                //recycler.setAdapter(adaptador);
            }else{
                Toast.makeText(
                        context,
                        "Ocurrió un error de Parsing Json",
                        Toast.LENGTH_SHORT)
                        .show();
            }


        }
    }

    void checkActivities(){
        Date date  = new Date();
        if(MainActivity.notif_hora == null){
            MainActivity.notif_hora = new ArrayList<>();
        }
        if(MainActivity.notif_dia == null){
            MainActivity.notif_dia = new ArrayList<>();
        }
        int minutes;
        boolean notific = true;
        for (int i = 0 ; i < MainActivity.my_activities.size(); i++){
            long diferencia = MainActivity.my_activities.get(i).getPlanification().getTime() - date.getTime();
            int id = MainActivity.my_activities.get(i).getId();
            if (diferencia <= 86400000 && diferencia > 3600000){ // Falta menos de 1 día
                notific = true;
                for (int j = 0; j < MainActivity.notif_dia.size() ; j++) {
                    if (MainActivity.notif_dia.get(j) == id) {
                        notific = false;
                        break;
                    }
                }
                if(notific){
                    //minutes = (int) diferencia / (1000 * 60 * 60);
                    MainActivity.notif_dia.add(id);
                    launchNotification(i, true);
                    break;
                }
            }
            else {
                if (diferencia <= 3600000 && diferencia > 0) { // Falta menos de 1 hora
                    for (int j = 0; j < MainActivity.notif_hora.size() ; j++) {
                        notific = true;
                        if (MainActivity.notif_hora.get(j) == id) {
                            notific = false;
                            break;
                        }
                    }
                    if(notific){
                        //minutes = (int) diferencia / (1000 * 60);
                        MainActivity.notif_hora.add(id);
                        launchNotification(i, false);
                        break;
                    }
                }
            }
        }
    }

    /*Lanza notificacion cuando quedan menos de 24 horas y menos de 1 hora en las actividades que
    * el usuario está apuntado
    * - Se lanzan las notificaciones de forma diferente en funcion de la versión de android se lanza
    * de una forma u otra*/
    void launchNotification(int i, boolean notif_dia){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {


            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0, intent, 0);

            mBuilder.setContentIntent(intencionPendiente);


        }
        else{
            CharSequence name;
            if(notif_dia) {
                name = "QUEDAN MENOS DE 24 HORAS!";
            }
            else{
                name = "QUEDA MENOS DE 1 HORA!";
            }
            String description = MainActivity.my_activities.get(i).getName();
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,name,importance);

            //Configuracion del canal
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);

            mNotificationManager.createNotificationChannel(mChannel);

            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);


            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MainActivity.class);
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your app to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);


            // mNotificationId is a unique integer your app uses to identify the
            // notification. For example, to cancel the notification, you can pass its ID
            // number to NotificationManager.cancel().

        }
        String title;
        if(notif_dia) {
            title = "QUEDAN MENOS DE 24 HORAS!";
        }
        else{
            title = "QUEDA MENOS DE 1 HORA!";
        }
        mBuilder.setSmallIcon( MainActivity.my_activities.get(i).getResourceType())
                .setContentTitle(title)
                .setContentText(MainActivity.my_activities.get(i).getName())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_notalone))
                .setWhen(System.currentTimeMillis())
                .setContentInfo(MainActivity.my_activities.get(i).getDescription())
                .setTicker("Mas info!")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0, 100, 200, 300, 400, 500, 600, 700})
                //.setDefaults(Notification.DEFAULT_LIGHTS)
                .setLights(Color.RED, 3000, 1000)
                .setAutoCancel(true);
        //if(notif_dia)
            mNotificationManager.notify(i, mBuilder.build());
        //else
        //    mNotificationManager.notify(mNotificationIdHour, mBuilder.build());
    }
}
