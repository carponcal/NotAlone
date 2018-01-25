package com.carponcal.notalone;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements LocationListener {
    private HttpURLConnection conexion;
    private ProgressDialog progressDialog;
    private int id;
    /*Variables para localización*/
    private LocationManager manejador;
    private String proveedor;
    private static final long TIEMPO_MIN = 10 * 1000 ; // 10 segundos
    private static final long DISTANCIA_MIN = 5; // 5 metros
    Location localizacion;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION  = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        //Listener para comprobar cuando se ha modificado el campo usuario, así comprobamos si existe ya o no
        final EditText username = (EditText) findViewById(R.id.username);
        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
        /* When focus is lost check that the text field
        * has valid values.
        */
                if (!hasFocus) {
                    TareaCheckUser tareaCheckUser = new TareaCheckUser(username);
                    tareaCheckUser.execute();
                }
            }
        });

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder information = new AlertDialog.Builder(this);
                information.setMessage(R.string.location_request);
                information.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ActivityCompat.requestPermissions(RegisterActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    }});
                information.show();
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criterio = new Criteria();
            criterio.setCostAllowed(false);
            criterio.setAltitudeRequired(false);
            criterio.setAccuracy(Criteria.ACCURACY_FINE);
            proveedor = manejador.getBestProvider(criterio, true);
            localizacion = getLastKnownLocation();
            TextView loc = (TextView)findViewById(R.id.location);
            String texto;
            if (localizacion != null) {
                texto = "Long: " + String.format("%.3f", localizacion.getLongitude()) +
                        " -- Lat: " + String.format("%.3f", localizacion.getLatitude());
                loc.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else {
                texto = getString(R.string.waiting_location);
            }
            loc.setText(texto);
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

    public void requestLocationPermission(View view){
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder information = new AlertDialog.Builder(this);
                information.setMessage(R.string.location_request);
                information.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ActivityCompat.requestPermissions(RegisterActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    }});
                information.show();
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criterio = new Criteria();
            criterio.setCostAllowed(false);
            criterio.setAltitudeRequired(false);
            criterio.setAccuracy(Criteria.ACCURACY_FINE);
            proveedor = manejador.getBestProvider(criterio, true);
            localizacion = getLastKnownLocation();
            TextView loc = (TextView)findViewById(R.id.location);
            String texto;
            if (localizacion != null) {
                texto = "Long: " + String.format("%.3f", localizacion.getLongitude()) +
                        " -- Lat: " + String.format("%.3f", localizacion.getLatitude());
                loc.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else {
                texto = getString(R.string.waiting_location);
            }
            loc.setText(texto);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
                    manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);
                    localizacion = getLastKnownLocation();
                    String texto= "Long: " + String.format("%.3f",localizacion.getLongitude()) +
                            " -- Lat: " + String.format("%.3f",localizacion.getLatitude());
                    TextView loc = (TextView)findViewById(R.id.location);
                    loc.setText(texto);
                    loc.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AlertDialog.Builder information = new AlertDialog.Builder(this);
                    information.setMessage(R.string.location_request_denied);
                    information.show();
                    TextView loc = (TextView)findViewById(R.id.location);
                    loc.setText(R.string.errorLocation);
                    loc.setTextColor(getResources().getColor(R.color.colorDisabled));
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
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);
        }
    }
    @Override protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            manejador.removeUpdates(this);
        }
    }
    // Métodos de la interfaz LocationListener
    public void onLocationChanged(Location location) {
        localizacion = location;
        String texto= "Long: " + String.format("%.3f",localizacion.getLongitude()) +
                " -- Lat: " + String.format("%.3f",localizacion.getLatitude());
        TextView loc = (TextView)findViewById(R.id.location);
        loc.setText(texto);
        loc.setTextColor(getResources().getColor(R.color.colorPrimary));
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

    public void loginAsGuest(View view){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.DialogUserGuest)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences preferences;
                        preferences = getApplicationContext().getSharedPreferences(
                                "username", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("username", "");
                        editor.putString("password", "");
                        editor.putBoolean("guest", true);
                        editor.putInt("id", -1);
                        editor.apply();
                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(i);
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public void CreateUserThread(View view){
        EditText username = (EditText)  findViewById(R.id.username);
        EditText password = (EditText)  findViewById(R.id.password);
        if (username.getText().equals("") || password.getText().equals("")){
            Toast.makeText(this, R.string.user_password_empty,Toast.LENGTH_LONG).show();
        }
        else {
            TareaCreateUser login = new TareaCreateUser(view);
            login.execute();
        }


    }

    //Metodo para comprobar si un valor es numérico
    private static boolean isNumeric(String cadena){
        try {
            Integer.parseInt(cadena);
            return true;
        } catch (NumberFormatException nfe){
            return false;
        }
    }

    //AsynkTask to check User
    private class TareaCheckUser extends AsyncTask<Void, Void, Integer> {
        private View view;

        public TareaCheckUser(View view){
            this.view = view;

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer aInteger) {
            super.onPostExecute(aInteger);
            AppCompatButton createUser = (AppCompatButton) findViewById(R.id.createAccount);
            switch (aInteger){
                case 0://Error de conexion
                    AlertDialog.Builder information = new AlertDialog.Builder(RegisterActivity.this);
                    information.setMessage(R.string.connection_error);
                    information.show();
                    break;
                case 1://Nombre de usuario no existe
                    createUser.setText(R.string.createAccount);
                    createUser.setEnabled(true);
                    createUser.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case 2://Nombre de usuario ya existe
                    createUser.setText(R.string.createAccountUserExists);
                    createUser.setEnabled(false);
                    createUser.setBackgroundColor(getResources().getColor(R.color.colorDisabled));
                    Snackbar.make(view, R.string.DialogUserExists, Snackbar.LENGTH_LONG)
                            .setAction("Error", null).show();
                    break;

                default:
                    break;
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            EditText username = (EditText)  findViewById(R.id.username);
            int ok = 0;
            try {
                URL url=new URL("http://larctrobat.es/notalone/check_user.php" + "?username=" + username.getText().toString());
                conexion = (HttpURLConnection) url
                        .openConnection();
                conexion.setConnectTimeout(3000);
                if (conexion.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                    String linea = reader.readLine();
                    ok = 2;
                    while (linea !=null && !linea.equals("")) {
                        if (linea.equals("OK")){
                            ok = 1;
                            break;
                        }
                        linea = reader.readLine();
                    }
                    reader.close();
                } else {
                    Log.e("NotAlone", conexion.getResponseMessage());
                }
            } catch (Exception e) {
                Log.e("NotAlone", e.getMessage(), e);
                return ok;
            } finally {
                if (conexion!=null) conexion.disconnect();
                return ok;
            }

        }
    }


    //AsynkTask to create User
    private class TareaCreateUser extends AsyncTask<Void, Void, Boolean> {
        private View view;

        public TareaCreateUser(View view){
            this.view = view;

        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage(getString(R.string.connecting));
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            EditText username = (EditText)  findViewById(R.id.username);
            EditText password = (EditText)  findViewById(R.id.password);
            progressDialog.dismiss();
            if (aBoolean){
                SharedPreferences preferences;
                preferences = getApplicationContext().getSharedPreferences(
                        "username", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", username.getText().toString());
                editor.putString("password", password.getText().toString());
                editor.putInt("id", id);
                editor.putBoolean("guest", false);
                editor.apply();
                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
            else{
                /*AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(R.string.DialogUserIncorrect);
                builder.show();*/

                Snackbar.make(view, R.string.DialogCreateUserIncorrect, Snackbar.LENGTH_LONG)
                        .setAction("Error", null).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            EditText username = (EditText)  findViewById(R.id.username);
            EditText password = (EditText)  findViewById(R.id.password);
            EditText email = (EditText)  findViewById(R.id.mail);
            EditText phone = (EditText)  findViewById(R.id.phone);
            double longi = 0, lat = 0;
            if (localizacion != null){
                longi = localizacion.getLongitude();
                lat  = localizacion.getLatitude();
            }
            boolean ok = false;
            try {
                URL url=new URL("http://larctrobat.es/notalone/new_user.php" +
                        "?username=" + username.getText().toString() +
                        "&pass=" + password.getText().toString() +
                        "&phone=" + phone.getText().toString() +
                        "&mail=" + email.getText().toString() +
                        "&long=" + String.valueOf(longi) +
                        "&lat=" + String.valueOf(lat));
                conexion = (HttpURLConnection) url
                        .openConnection();
                if (conexion.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                    String linea = reader.readLine();
                    while (linea !=null && !linea.equals("")) {
                        if (isNumeric(linea)){
                            ok = true;
                            id = Integer.parseInt(linea);
                            break;
                        }
                        linea = reader.readLine();
                    }
                    reader.close();
                } else {
                    Log.e("NotAlone1", conexion.getResponseMessage());
                }
            } catch (Exception e) {
                Log.e("NotAlone2", e.getMessage(), e);
            } finally {
                if (conexion!=null) conexion.disconnect();
                return ok;
            }

        }
    }
}
