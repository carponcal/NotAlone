package com.carponcal.notalone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewerActivity extends AppCompatActivity {
    private int id_activity;
    private boolean myactivities;
    private int id_user;
    private int typeImage;
    private String typeName;
    private SharedPreferences preferences;
    private Activity_user activity;
    HttpURLConnection con;
    private ProgressDialog progressDialog;
    private boolean guest;

    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        Intent i = getIntent();
        id_activity = i.getIntExtra("activity",0);

        myactivities = i.getBooleanExtra("myactivities",false);

        preferences = this.getSharedPreferences(
                "username", Context.MODE_PRIVATE);
        id_user = preferences.getInt("id", 0);
        guest = preferences.getBoolean("guest", false);
        //Inicializar datos en los campos de la actividad

        ImageView image = (ImageView) findViewById(R.id.image);
        TextView typeActivity = (TextView) findViewById(R.id.typeActivity);
        TextView title = (TextView) findViewById(R.id.nameActivity);
        TextView planificationText = (TextView) findViewById(R.id.whenActivity);
        TextView locationLat = (TextView) findViewById(R.id.whereActivity);
        TextView locationLong = (TextView) findViewById(R.id.whereActivity2);
        TextView description = (TextView) findViewById(R.id.descriptionActivity);
        TextView owner = (TextView) findViewById(R.id.owner);
        TextView users = (TextView) findViewById(R.id.numusers);

        if (myactivities) {
             activity= MainActivity.my_activities.get(id_activity);
        }
        else{
            activity= MainActivity.activities_search.get(id_activity);
        }
        selectTypeActivity();
        title.setText(activity.getName());
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        String planificationString = format.format(activity.getPlanification());
        planificationText.setText(planificationString);
        locationLat.setText("Lat: "+ activity.getLocation().getLatitude());
        locationLong.setText("Long: " + activity.getLocation().getLongitude());
        description.setText(activity.getDescription());
        owner.setText(activity.getPropietaryName());
        users.setText(String.valueOf(activity.getNumUsers()));
        image.setImageResource(typeImage);
        typeActivity.setText(typeName);
        stileButton();
    }

    public void stileButton(){
        button = (Button) findViewById(R.id.btnAccept);
        if(myactivities){
            button.setBackground(getDrawable(R.drawable.corners_button_cancel));
            button.setText(R.string.leave);
        }
        else{
            button.setBackground(getDrawable(R.drawable.corners_button));
            button.setText(R.string.join);
        }
    }

    public void selectTypeActivity(){
        int type = activity.getType();
        switch (type){
            case 4:
                typeImage = R.drawable.ic_bike_black_48dp;
                typeName = getString(R.string.cycling);
                break;
            case 6:
                typeImage = R.drawable.ic_image_filter_hdr_black_48dp;
                typeName = getString(R.string.trekking);
                break;
            case 2:
                typeImage = R.drawable.ic_walk_black_48dp;
                typeName = getString(R.string.walking);
                break;
            case 1:
                typeImage = R.drawable.ic_run_black_48dp;
                typeName = getString(R.string.running);
                break;
            case 7:
                typeImage = R.drawable.ic_soccer_black_48dp;
                typeName = getString(R.string.football);
                break;
            case 8:
                typeImage = R.drawable.ic_basketball_black_48dp;
                typeName = getString(R.string.basketball);
                break;
            case 5:
                typeImage = R.drawable.ic_tennis_black_48dp;
                typeName = getString(R.string.tennis);
                break;
            case 3:
                typeImage = R.drawable.rollerblade;
                typeName = getString(R.string.rollerblading);
                break;
            default:
                typeImage = 0;
                break;

        }

    }

    public void intentShare(View view){

        String text = "Actividad: " + activity.getName() +
                "\nPlanificación: " + activity.getPlanification().toString() +
                "\nUbicación: (Long:" + String.format("%.4f",activity.getLocation().getLongitude()) +
                ", Lat:" +String.format("%.4f",activity.getLocation().getLongitude()) + ")" +
                "\nDescripción: " + activity.getDescription() +
                "\n--------------------------------------------------" +
        "\n¿No tienes con quien hacer algo? Hazlo con alguien con NotAlone: http://play.google.com/store/apps/details?id=" +
                this.getPackageName();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/pla in");
        i.putExtra(Intent.EXTRA_TEXT, text);
        this.startActivity(Intent.createChooser(i, getString(R.string.select_app)));
    }

    public void intentLocation(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri geo = Uri.parse("geo:0,0?q=" + String.valueOf(activity.getLocation().getLatitude()) + ","
                + String.valueOf(activity.getLocation().getLongitude())+
                "("+ activity.getName() +")");
        i.setData(geo);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);

        }
    }

    public void joinActivity(View view){
        if (guest){
            Snackbar.make(view, R.string.no_join_guest, Snackbar.LENGTH_LONG)
                    .setAction("Error", null).show();
        }
        else {
            TareaJoinActivity joinActivity = new TareaJoinActivity(view);
            joinActivity.execute();
        }
    }

    //AsynkTask to create User
    private class TareaJoinActivity extends AsyncTask<Void, Void, Boolean> {
        private View view;

        public TareaJoinActivity(View view){
            this.view = view;

        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(ViewerActivity.this);
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
            progressDialog.dismiss();
            if (aBoolean){
                if (myactivities)
                    myactivities = false;
                else
                    myactivities = true;
                stileButton();
            }
            else{
                /*AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(R.string.DialogUserIncorrect);
                builder.show();*/

                Snackbar.make(view, R.string.DialogJoinActivityIncorrect, Snackbar.LENGTH_LONG)
                        .setAction("Error", null).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            preferences = view.getContext().getSharedPreferences(
                    "username", Context.MODE_PRIVATE);
            int id_user = preferences.getInt("id", 0);
            int join = 1;
            if(myactivities)
                join = 0;

            boolean ok = false;
            try {
                String direction = "http://larctrobat.es/notalone/join_activity.php" +
                        "?user_id=" + String.valueOf(id_user) +
                        "&activity_id=" + String.valueOf(activity.getId()) +
                        "&propietary=" + String.valueOf(activity.getPropietary()) +
                        "&join=" + String.valueOf(join) ;
                URL url=new URL(direction);
                con = (HttpURLConnection) url
                        .openConnection();
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String linea = reader.readLine();
                    while (linea !=null && !linea.equals("")) {
                        if (linea.equals("OK")){
                            ok = true;
                            break;
                        }
                        linea = reader.readLine();
                    }
                    reader.close();
                } else {
                    Log.e("NotAlone1", con.getResponseMessage());
                }
            } catch (Exception e) {
                Log.e("NotAlone2", e.getMessage(), e);
            } finally {
                if (con!=null) con.disconnect();
                return ok;
            }

        }
    }

    @Override
    public void onBackPressed() {

        onActivityResult(1,1,null);
        super.onBackPressed();
    }
}
