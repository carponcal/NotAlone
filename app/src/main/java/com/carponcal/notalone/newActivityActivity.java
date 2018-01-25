package com.carponcal.notalone;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by carlos on 20/12/2017.
 * Activity para crear una actividad
 */
public class newActivityActivity extends AppCompatActivity {
    private int typeImagen, type;
    private Date planification;
    private  int dia,mes,ano,hora,minutos;
    HttpURLConnection con;
    private ProgressDialog progressDialog;
    private Location localizacion;
    private SharedPreferences preferences;
    private static final int OK_RESULT_CODE = 1;
    private static final int MAP_PICKER_ACTIVITY = 0;

    private String direction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_activity);
        typeImagen = getIntent().getExtras().getInt("typeImage");
        type = getIntent().getExtras().getInt("type");
        ImageView image = (ImageView) findViewById(R.id.image);
        image.setImageResource(typeImagen);


        TextView typeActivity = (TextView)findViewById(R.id.typeActivity);
        selectType(typeActivity);


    }
    public void cancelCreation(View view){
        Intent data = new Intent();
        data.putExtra("result","ko");
        setResult(OK_RESULT_CODE,data);
        finish();
    }

    public void createActivity(View view){
        TareaCreateActivity createActivity = new TareaCreateActivity(view);
        createActivity.execute();
    }

    private void selectType(TextView view){
        switch(type){
            case 1:
                view.setText(R.string.running);
                break;
            case 2:
                view.setText(R.string.walking);
                break;
            case 3:
                view.setText(R.string.rollerblading);
                break;
            case 4:
                view.setText(R.string.cycling);
                break;
            case 5:
                view.setText(R.string.tennis);
                break;
            case 6:
                view.setText(R.string.trekking);
                break;
            case 7:
                view.setText(R.string.football);
                break;
            case 8:
                view.setText(R.string.basketball);
                break;
        }
    }
    public void setPlanification(View view){
        final TextView et = (TextView) view;
        final Calendar c = Calendar.getInstance();
        dia=c.get(Calendar.DAY_OF_MONTH);
        mes=c.get(Calendar.MONTH);
        ano=c.get(Calendar.YEAR);
        hora=c.get(Calendar.HOUR_OF_DAY);
        minutos=c.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                et.setText("" +year + "/" + (((monthOfYear+1)<10)?"0":"") + (monthOfYear+1) + "/" +  ((dayOfMonth<10)?"0":"") + dayOfMonth);
                TimePickerDialog timePickerDialog = new TimePickerDialog(newActivityActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        et.setText(et.getText() + " " + hourOfDay+":"+minute);
                        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                        try {
                            planification = format.parse(et.getText().toString());
                        }
                        catch (ParseException e)
                        {}
                    }
                },hora,minutos,true);
                timePickerDialog.show();
            }
        },ano,mes,dia);
        datePickerDialog.show();





    }

    public void setPosition (View view){
        Intent intent = new Intent(newActivityActivity.this, MapPickerActivity.class);
        startActivityForResult(intent,MAP_PICKER_ACTIVITY);
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

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == MAP_PICKER_ACTIVITY && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                localizacion = bundle.getParcelable("Location");
                Log.e("TEST: ", "onActivityResult: "+ localizacion.toString() );
                TextView textLocation = (TextView) findViewById(R.id.whereActivity);
                textLocation.setText("Long:" + String.format("%.3f", localizacion.getLongitude()) +
                        " - Lat: " + String.format("%.3f", localizacion.getLatitude()));
            }
        }
    }

    //AsynkTask to create User
    private class TareaCreateActivity extends AsyncTask<Void, Void, Boolean> {
        private View view;

        public TareaCreateActivity(View view){
            this.view = view;

        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(newActivityActivity.this);
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
                Snackbar.make(view, R.string.DialogCreateActivityCorrect, Snackbar.LENGTH_LONG)
                        .setAction("Error", null).show();
                Intent data = new Intent();
                data.putExtra("result","ok");
                setResult(OK_RESULT_CODE,data);
                finish();
            }
            else{
                /*AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(R.string.DialogUserIncorrect);
                builder.show();*/

                Snackbar.make(view, R.string.DialogCreateActivityIncorrect, Snackbar.LENGTH_LONG)
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
            EditText name = (EditText)  findViewById(R.id.nameActivity);
            EditText description = (EditText)  findViewById(R.id.descriptionActivity);
            DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.ENGLISH);
            String planificationString = format.format(planification);

            double longi = 0, lat = 0;
            if (localizacion != null){
                longi = localizacion.getLongitude();
                lat  = localizacion.getLatitude();
            }
            boolean ok = false;
            try {
                direction = "http://larctrobat.es/notalone/new_activity.php" +
                        "?id_user=" + String.valueOf(id_user) +
                        "&activity=" + name.getText().toString() +
                        "&description=" + description.getText().toString() +
                        "&planification=" + planificationString +
                        "&long=" + String.valueOf(longi) +
                        "&lat=" + String.valueOf(lat) +
                        "&type=" + String.valueOf(type);
                URL url=new URL(direction);
                con = (HttpURLConnection) url
                        .openConnection();
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String linea = reader.readLine();
                    while (linea !=null && !linea.equals("")) {
                        if (isNumeric(linea)){

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
}
