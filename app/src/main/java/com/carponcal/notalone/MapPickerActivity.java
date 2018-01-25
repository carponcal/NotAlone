package com.carponcal.notalone;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
/*
* Fragment para seleccionar desde el mapa la posicion exacta donde se quiere crear la actividad
* */
public class MapPickerActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener{
    private GoogleMap mapa;
    private static final int SOLICITUD_PERMISO_LOCALIZACION = 0;
    private LatLng ubicActual;
    private Location ubicacion;
    private LocationManager manejador;
    private String proveedor;
    private ImageButton confirmar;
    private int typeMap;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        confirmar = (ImageButton)findViewById(R.id.confirmPosition);
        confirmar.setEnabled(false);
        typeMap = 0;

    }

    public void searchText(View view){
        EditText search_text = (EditText)findViewById(R.id.search_text);
        if (search_text.getVisibility() == View.GONE){
            search_text.setVisibility(View.VISIBLE);
        }
        else{
            String location = search_text.getText().toString();
            List<Address> addressList = null;
            if(location != null && !location.equals(""))
            {
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location , 1);


                } catch (IOException e) {
                    e.printStackTrace();
                }
                mapa.clear();
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude() , address.getLongitude());
                mapa.addMarker(new MarkerOptions().position(latLng).title("Activity Position").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                mapa.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                confirmar.setEnabled(true);
            }
            search_text.setVisibility(View.GONE);
        }

    }



    public static void solicitarPermiso(final String permiso, String
            justificacion, final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad,
                permiso)){
            new AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad,
                                    new String[]{permiso}, requestCode);
                        }})
                    .show();
        } else {
            ActivityCompat.requestPermissions(actividad,
                    new String[]{permiso}, requestCode);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode,
                                                     String[] permissions, int[] grantResults) {
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION) {
            if (grantResults.length== 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapa.setMyLocationEnabled(true);

                mapa.getUiSettings().setZoomControlsEnabled(true);
                mapa.getUiSettings().setCompassEnabled(true);
                manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

                Criteria criterio = new Criteria();
                criterio.setCostAllowed(false);
                criterio.setAltitudeRequired(false);
                criterio.setAccuracy(Criteria.ACCURACY_FINE);
                proveedor = manejador.getBestProvider(criterio, true);
                if (mapa.getMyLocation() != null) {
                    mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mapa.getMyLocation().getLatitude(),
                                    mapa.getMyLocation().getLongitude()), 15));
                    mapa.clear();
                }
                else{
                    ubicacion = manejador.getLastKnownLocation(proveedor);
                    ubicActual = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ubicActual, 17);
                    mapa.animateCamera(cameraUpdate);
                    mapa.addMarker(new MarkerOptions().position(ubicActual).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                /*mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mapa.getMyLocation().getLatitude(),
                                mapa.getMyLocation().getLongitude()), 15));*/
                //animateCamera(findViewById(R.id.irPosicion));
            }
            else {
                /*Button btnMiPos=(Button) findViewById(R.id.button2);
                btnMiPos.setEnabled(false);*/
                Toast.makeText(this, "Sin el permiso, no puedo realizar la " +
                        "acción", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(UPV, 15));
/*        mapa.addMarker(new MarkerOptions()
                .position(UPV)
                .title("UPV")
                .snippet("Universidad Politécnica de Valencia")
                .icon(BitmapDescriptorFactory
                        .fromResource(android.R.drawable.ic_menu_compass))
                .anchor(0.5f, 0.5f));*/
        mapa.setOnMapClickListener(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);

            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
            manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criterio = new Criteria();
            criterio.setCostAllowed(false);
            criterio.setAltitudeRequired(false);
            criterio.setAccuracy(Criteria.ACCURACY_FINE);
            proveedor = manejador.getBestProvider(criterio, true);
            if (mapa.getMyLocation() != null) {
                mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mapa.getMyLocation().getLatitude(),
                                mapa.getMyLocation().getLongitude()), 10));
                mapa.clear();
            }
            else {
                ubicacion = manejador.getLastKnownLocation(proveedor);
                if (ubicacion != null){
                    ubicActual = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ubicActual, 10);
                    mapa.animateCamera(cameraUpdate);
                    mapa.addMarker(new MarkerOptions().position(ubicActual).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }
            /*mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mapa.getMyLocation().getLatitude(),
                            mapa.getMyLocation().getLongitude()), 15));*/
            //animateCamera(findViewById(R.id.irPosicion));
        } else {
            solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION, "Es necesario el permiso " +
                            "para poder situarnos en el mapa",
                    SOLICITUD_PERMISO_LOCALIZACION, this);

        }
    }
  /*  public void moveCamera(View view) {
        mapa.moveCamera(CameraUpdateFactory.newLatLng(UPV));
    }*/

    public void confirmPosition(View view) {
        Intent data = new Intent();
        data.putExtra("Location", ubicacion);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
    public void changeMap (View view){
        typeMap = (typeMap + 1)% 4;
        switch(typeMap){
            case 0:
                mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Toast.makeText(this, "NORMAL",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                Toast.makeText(this, "SATELLITE",Toast.LENGTH_SHORT).show();
                break;
            case 2:
                mapa.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                Toast.makeText(this, "TERRAIN",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                Toast.makeText(this, "HYBRID",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override public void onMapClick(LatLng puntoPulsado) {
        mapa.clear();
        mapa.addMarker(new MarkerOptions().position(puntoPulsado)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        confirmar.setEnabled(true);
        if (ubicacion == null) {//si no encuentra ubicación creo un location

            ubicacion = new Location("");
        }
        ubicacion.setLongitude(puntoPulsado.longitude);
        ubicacion.setLatitude(puntoPulsado.latitude);
        mapa.animateCamera(CameraUpdateFactory.newLatLng(
                puntoPulsado));
    }


}
