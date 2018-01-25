package com.carponcal.notalone;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    public static List<Activity_user> activities_search;
    public static List<Activity_user> my_activities;
    public static ArrayList<Integer> notif_hora, notif_dia;
    public static boolean servicio_activo;

    public static ConnectivityManager  connMgr;
    //This is our tablayout
    private TabLayout tabLayout;

    //This is our viewPager
    private ViewPager viewPager;



    private SharedPreferences preferences;

    //Fragments
    ActivitiesFragment activitiesFragment;
    SearchFragment searchFragment;

    private static HttpURLConnection con;


    int[] tabTitle={R.string.tab1,R.string.tab2};

    int[] unreadCount={0,0};

    /*Variables para localización*/
    private LocationManager manejador;
    private String proveedor;
    private static final long TIEMPO_MIN = 20 * 1000 ; // 20 segundos
    private static final long DISTANCIA_MIN = 1000; // 1 km
    public static Location localizacion;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION  = 0;
    private boolean myactivities = true;
    private boolean guest = false;
    public static int distancia;

private WatchService watchService;


    static final int ACTIV_PREFERENCES = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_without_icon);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        try {
            setupTabIcons();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        //Selecciono por defecto mis actividades o buscar si soy invitado
        preferences = this.getSharedPreferences(
                "username", Context.MODE_PRIVATE);
        guest = preferences.getBoolean("guest", false);
        if (guest) {
            tabLayout.getTabAt(0).select();
            myactivities = false;
        }
        else {
            tabLayout.getTabAt(1).select();
            myactivities = true;
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        distancia = Integer.parseInt(preferences.getString("distancia","0"));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position,false);
                switch (position){
                    case 0:
                        myactivities = false;
                        if  (SearchFragment.adaptador != null) {
                            updateDistances(0);
                            SearchFragment.adaptador.updateData(activities_search);
                        }
                        break;
                    case 1:
                        myactivities = true;
                        if  (ActivitiesFragment.adaptador!= null) {
                            updateDistances(1);
                            ActivitiesFragment.adaptador.updateData(my_activities);
                        }
                        break;
                    case 2:
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        /*Compruebo si tengo acceso a la ubicacion del dispositivo, en caso negativo solicito permiso*/
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder information = new AlertDialog.Builder(this);
                information.setMessage(R.string.location_request);
                information.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    }});
                information.show();
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            }
            else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

            //muestraProveedores();
            Criteria criterio = new Criteria();
            criterio.setCostAllowed(false);
            criterio.setAltitudeRequired(false);
            criterio.setAccuracy(Criteria.ACCURACY_FINE);
            proveedor = manejador.getBestProvider(criterio, true);
            localizacion = getLastKnownLocation();

        }

        if (!servicio_activo) {
            watchService = new WatchService();
            Intent service = new Intent(getApplicationContext(), WatchService.class);
            startService(service);
        }
        else
            Log.e("TEST", "onCreate: Servicio ya activo" );
    }

    public void lanzarPreferencias(View view){
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivityForResult(i, ACTIV_PREFERENCES);

    }

    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIV_PREFERENCES) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            distancia = Integer.parseInt(pref.getString("distancia","0"));
            updateDistances(2);
        }
    }

    private Location getLastKnownLocation() {
        manejador = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = manejador.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = manejador.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Criteria criterio = new Criteria();
                    criterio.setCostAllowed(false);
                    criterio.setAltitudeRequired(false);
                    criterio.setAccuracy(Criteria.ACCURACY_FINE);
                    proveedor = manejador.getBestProvider(criterio, true);
                    localizacion = getLastKnownLocation();
                }
                else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AlertDialog.Builder information = new AlertDialog.Builder(this);
                    information.setMessage(R.string.location_request_denied);
                    information.show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    // Métodos del ciclo de vida de la actividad
    @Override protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);
        }
    }
    @Override protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            manejador.removeUpdates(this);
        }
    }

    // Métodos de la interfaz LocationListener
    public void onLocationChanged(Location location) {
        localizacion = location;
        try {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                preferences = this.getSharedPreferences(
                        "username", Context.MODE_PRIVATE);
                int id_user = preferences.getInt("id", 0);
                new JsonTaskActivitySearch(this).execute( new URL("http://larctrobat.es/notalone/list_activities.php?id_user=" + id_user +"&my_activities=0"));

            } else {
                Toast.makeText(this, R.string.not_network, Toast.LENGTH_LONG).show();
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        updateDistances(2);

    }

    public void updateDistances(int tab){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        float distance;
        if ((tab == 0 || tab == 2) && activities_search != null ) {
            if (localizacion == null){
                localizacion  = manejador.getLastKnownLocation(proveedor);
            }
            if (localizacion != null) {
                for (int i = 0; i < activities_search.size(); i++) {


                    distance = localizacion.distanceTo(activities_search.get(i).getLocation());

                    activities_search.get(i).setDistance(distance);
                    if (distancia > 0 && distancia < distance / 1000) {
                        activities_search.remove(i);
                        i--;
                    }
                }
            }
            if (SearchFragment.adaptador != null)
                SearchFragment.adaptador.updateData(activities_search);


        }

        if((tab == 1 || tab == 2) && my_activities != null ) {
            if (localizacion == null){
                localizacion  = manejador.getLastKnownLocation(proveedor);
            }
            if (localizacion != null) {
                for (int i = 0; i < my_activities.size(); i++) {
                    distance = localizacion.distanceTo(my_activities.get(i).getLocation());
                    my_activities.get(i).setDistance(distance);
                    //Log.e("TEST", "updateDistances: MyAct Act: " + my_activities.get(i).getName() + " distancia: " + distance);
                    if (distancia > 0 && distancia < distance / 1000) {
                        my_activities.remove(i);
                        i--;
                    }
                }
            }
            if (ActivitiesFragment.adaptador != null)
                ActivitiesFragment.adaptador.updateData(my_activities);
        }
    }
    public void onProviderDisabled(String proveedor) {
        //log("Proveedor deshabilitado: " + proveedor + "\n");
    }
    public void onProviderEnabled(String proveedor) {
        //log("Proveedor habilitado: " + proveedor + "\n");
    }
    public void onStatusChanged(String proveedor, int estado, Bundle extras) {
        //log("Cambia estado proveedor: " + proveedor + ", estado="
        //        + E[Math.max(0, estado)] + ", extras=" + extras + "\n");
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        // Associate searchable configuration with the SearchView
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_places:

                Intent intent = new Intent(this, MapaActivity.class);

                intent.putExtra("myactivities",myactivities);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                lanzarPreferencias(null);
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_aboutus:
                lanzarAcercaDe(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void lanzarAcercaDe(View view){

        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }

    void logout(){
        SharedPreferences preferences;
        preferences = getApplicationContext().getSharedPreferences(
                "username", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        distancia = 0;
        editor.putString("username", "");
        editor.putString("password", "");
        editor.putBoolean("guest", true);
        editor.putInt("id", -1);
        editor.apply();
        editor.commit();
        editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("distancia","0");
        editor.apply();
        editor.commit();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        searchFragment=new SearchFragment();
        activitiesFragment=new ActivitiesFragment();
        adapter.addFragment(searchFragment, getString(R.string.tab1));
        adapter.addFragment(activitiesFragment,getString(R.string.tab2));
        viewPager.setAdapter(adapter);
    }

    private View prepareTabView(int pos) {
        View view = getLayoutInflater().inflate(R.layout.custom_tab,null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_title.setText(getString(tabTitle[pos]));
        if(unreadCount[pos]>0)
        {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText(""+unreadCount[pos]);
        }
        else
            tv_count.setVisibility(View.GONE);


        return view;
    }

    private void setupTabIcons() {

        for(int i=0;i<tabTitle.length;i++)
        {
            /*TabLayout.Tab tabitem = tabLayout.newTab();
            tabitem.setCustomView(prepareTabView(i));
            tabLayout.addTab(tabitem);*/

            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }


    }
    /*AsyncTask para cargar datos de actividades*/
    public class JsonTaskActivitySearch extends AsyncTask<URL, Void, List<Activity_user>> {
        private Context context;
        public JsonTaskActivitySearch(Context context) {
            this.context = context;
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

                activities_search = activities_user;
                if  (SearchFragment.adaptador != null)
                    updateDistances(0);
                //SearchFragment.adaptador.updateData(activities_user);
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
    /*AsyncTask para cargar datos de actividades del usuario*/
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
                my_activities = activities_user;
                if  (ActivitiesFragment.adaptador!= null)
                    updateDistances(1);
                //adaptador = new ActivitiesSearchAdapter(getBaseContext(), activities_user);
                //recycler.setAdapter(adaptador);
            }else{
                Toast.makeText(
                        getBaseContext(),
                        "Ocurrió un error de Parsing Json",
                        Toast.LENGTH_SHORT)
                        .show();
            }

        }
    }

}

