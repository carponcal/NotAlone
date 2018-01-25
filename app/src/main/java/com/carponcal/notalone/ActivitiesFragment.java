package com.carponcal.notalone;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ActivitiesFragment extends Fragment {
    private RecyclerView recycler;
    private SwipeRefreshLayout refreshLayout;
    public static MyActivitiesAdapter adaptador;
    private Context context;
    private int id_user;
    private boolean guest;
    HttpURLConnection con;
    private SharedPreferences preferences;

    static final int CREATE_ACTIVITY = 1;  // The request code
    public ActivitiesFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.context = getContext();
        preferences = context.getSharedPreferences(
                "username", Context.MODE_PRIVATE);
        id_user = preferences.getInt("id", 0);
        guest = preferences.getBoolean("guest", true);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ((requestCode == CREATE_ACTIVITY)){
            if (data != null) {
                String ok = data.getStringExtra("result");
                if (ok.equals("ok")) {
                    launchTask();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        context = view.getContext();
        recycler = (RecyclerView) view.findViewById(R.id.recyclerMyActivities);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        adaptador =  new MyActivitiesAdapter(context,MainActivity.my_activities );
        recycler.setAdapter(adaptador);

        // Creación escuchador para la seleccion de los elementos del RecyclerView
        recycler.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View v, int position) {

                        /*StringBuilder tmp = new StringBuilder();
                        tmp.append("");
                        tmp.append(position);
                        String msg = "Se ha pulsado el elemento " + tmp.toString();
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();*/

                        Intent intent = new Intent(context, ViewerActivity.class);
                        intent.putExtra("activity", position);
                        intent.putExtra("myactivities", true);

                        startActivity(intent);
                    }
                })
        );

        // Obtener el refreshLayout
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);

        // Iniciar la tarea asíncrona al revelar el indicador
        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        boolean ok =  launchTask();
                        if (!ok){
                            // Parar la animación del indicador
                            refreshLayout.setRefreshing(false);
                        }
                    }
                }
        );
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.new_activity) ;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (guest){
                      Snackbar.make(view,  R.string.nocreateUserGuest, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                }
                else {
                    Intent i = new Intent(context, SelectTypeActivity.class);
                    startActivityForResult(i, CREATE_ACTIVITY);
                }
            }
        });

        return view;
    }
    /*Se comprueba conectividad y en caso positivo lanza el thread para descargar los datos actualizados desde el servidor*/
    public boolean launchTask(){
        try {
            ConnectivityManager connMgr = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                String URL = "http://larctrobat.es/notalone/list_activities.php?id_user=" + id_user + "&my_activities=1";
                new JsonTaskMyActivities(context).execute( new URL(URL));
                return true;
            } else {
                Toast.makeText(context, R.string.not_network, Toast.LENGTH_LONG).show();
                return false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        launchTask();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            //inflater.inflate(R.menu.menu_home, menu);
            super.onCreateOptionsMenu(menu, inflater);
    }
    /*Clase interna Que lanza Thread asincrono para descargar datos por red*/
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
                /*Si hay error al capturar los datos se crea actividad que indique error al usuario*/
                if(statusCode!=200) {
                    activities = new ArrayList<>();
                    activities.add(new Activity_user(0,"Error",null,null, null,0,0,"",0));

                } else {

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
                MainActivity.my_activities = activities_user;
                /*DEBUG*/
                /*
                for (int i= 0; i<activities_user.size();i++){
                    Log.e("activities_user", "Elemento: " + i + " -> " + activities_user.get(i).getName() );
                }
                for (int i= 0; i<MainActivity.my_activities.size();i++){
                    Log.e("Main.my_activities", "Elemento: " + i + " -> " + MainActivity.my_activities.get(i).getName() );
                }

                for (int i= 0; i<MainActivity.my_activities.size();i++){
                    Log.e("NEW Main.my_act", "Elemento: " + i + " -> " + MainActivity.my_activities.get(i).getName() +
                    "; Location: " + MainActivity.my_activities.get(i).getLocation().getLatitude() + "/" +
                            MainActivity.my_activities.get(i).getLocation().toString());
                }*/

                if  (ActivitiesFragment.adaptador!= null)
                    updateDistances();
            }
            else{
                Toast.makeText(
                        context,
                        "Ocurrió un error de Parsing Json",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            // Parar la animación del indicador
            refreshLayout.setRefreshing(false);

        }
    }
    /*Recalculo de distancias en pantalla*/
    public void updateDistances() {
        float distance;
        if (MainActivity.my_activities != null) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && MainActivity.localizacion != null) {
                for (int i = 0; i < MainActivity.my_activities.size(); i++) {
                    distance = MainActivity.localizacion.distanceTo(MainActivity.my_activities.get(i).getLocation());
                    MainActivity.my_activities.get(i).setDistance(distance);
                    if (MainActivity.distancia > 0 && MainActivity.distancia < distance/1000){
                        MainActivity.my_activities.remove(i);
                        i--;
                    }
                }
            }
            /*Si el adaptador tiene datos*/
            if (adaptador != null)
                adaptador.updateData(MainActivity.my_activities);


        }
    }
}
