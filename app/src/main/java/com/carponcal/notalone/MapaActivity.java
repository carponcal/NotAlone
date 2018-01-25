package com.carponcal.notalone;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MapaActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mapa;
    private boolean myactivities;
    private int typeMap = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        myactivities = getIntent().getExtras().getBoolean("myactivities",false);
    }
    /*Permite cambiar el tipo de mapa*/
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
    /*Recarga los datos del mapa. Muestra en el mapa las actividades posicionadas. */
    public void reloadData(){
        mapa.clear();
        if (myactivities)
        {
            if (MainActivity.my_activities.size()>0) {
                Location loc = MainActivity.my_activities.get(0).getLocation();
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(loc.getLatitude(), loc.getLongitude()), 12));
            }
            for (int n = 0; n < MainActivity.my_activities.size(); n++) {
                Activity_user activity = MainActivity.my_activities.get(n);
                Location loc = activity.getLocation();
                if (loc != null && loc.getLatitude() != 0) {
                    BitmapDrawable iconoDrawable = (BitmapDrawable)
                            getResources().getDrawable(activity.getResourceType());
                    Bitmap iGrande = iconoDrawable.getBitmap();
                    Bitmap icono = Bitmap.createScaledBitmap(iGrande,
                            iGrande.getWidth() / 2, iGrande.getHeight() / 2, false);
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
                    String datos = format.format(activity.getPlanification());
                    mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(loc.getLatitude(),loc.getLongitude()))
                            .title(activity.getName())
                            .snippet(datos)
                            .icon(BitmapDescriptorFactory.fromBitmap(icono)));
                }
            }
        }
        else{
            if (MainActivity.activities_search.size()>0) {
                Location loc = MainActivity.activities_search.get(0).getLocation();
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(loc.getLatitude(), loc.getLongitude()), 12));
            }
            for (int n = 0; n < MainActivity.activities_search.size(); n++) {
                Activity_user activity = MainActivity.activities_search.get(n);
                Location loc = activity.getLocation();
                if (loc != null && loc.getLatitude() != 0) {
                    Log.e("TEST", "onMapReady: " + activity.getName() +" Type: " + activity.getType() + " Resource: " + activity.getResourceType() );
                    BitmapDrawable iconoDrawable = (BitmapDrawable) getDrawable(activity.getResourceType());
                    Bitmap iGrande = iconoDrawable.getBitmap();
                    Bitmap icono = Bitmap.createScaledBitmap(iGrande,
                            iGrande.getWidth() / 2, iGrande.getHeight() / 2, false);
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
                    String datos = format.format(activity.getPlanification());
                    mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(loc.getLatitude(),loc.getLongitude()))
                            .title(activity.getName())
                            .snippet(datos)

                            .icon(BitmapDescriptorFactory.fromBitmap(icono)));
                }
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
        }



        reloadData();
        mapa.setOnInfoWindowClickListener(this);
    }

    /*Permite lanzarse la activity donde se muestra toda la información de la actividad seleccionada*/
    @Override
    public void onInfoWindowClick(Marker marker) {
        if(myactivities) {
            for (int id=0; id<MainActivity.my_activities.size(); id++){
                if (MainActivity.my_activities.get(id).getName()
                        .equals(marker.getTitle())){
                    Intent intent = new Intent(this, ViewerActivity.class);
                    intent.putExtra("activity", id);
                    intent.putExtra("myactivities",myactivities);
                    startActivityForResult(intent,1);
                    break;
                }
            }
        }
        else{
            for (int id=0; id< MainActivity.activities_search.size(); id++){
                if (MainActivity.activities_search.get(id).getName()
                        .equals(marker.getTitle())){
                    Intent intent = new Intent(this, ViewerActivity.class);
                    intent.putExtra("activity", id);
                    intent.putExtra("myactivities",myactivities);
                    startActivityForResult(intent,1);
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ((requestCode == 1)){

                reloadData();
        }
    }
}
