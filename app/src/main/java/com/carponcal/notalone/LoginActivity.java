package com.carponcal.notalone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private HttpURLConnection conexion;
    private ProgressDialog progressDialog;
    private int id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences preferences;
        preferences = getApplicationContext().getSharedPreferences(
                "username", Context.MODE_PRIVATE);
        boolean guest = preferences.getBoolean("guest",true);
        if(!guest){
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    // Métodos del ciclo de vida de la actividad
    @Override protected void onResume() {
        super.onResume();
        SharedPreferences preferences;
        preferences = getApplicationContext().getSharedPreferences(
                "username", Context.MODE_PRIVATE);
        boolean guest = preferences.getBoolean("guest",true);
        if(!guest){

            finish();
        }
    }

    public void LoginUserThread(View view){
        EditText username = (EditText)  findViewById(R.id.username);
        EditText password = (EditText)  findViewById(R.id.password);
        if (username.getText().equals("") || password.getText().equals("")){
            Toast.makeText(this,R.string.user_password_empty,Toast.LENGTH_LONG).show();
        }
        else {
            TareaLoginUser login = new TareaLoginUser(view);
            login.execute();
        }
    }

    public void launchRegisterActivity(View view){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void loginAsGuest(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
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

    //AsynkTask para realizar login
    private class TareaLoginUser extends AsyncTask<Void, Void, Boolean> {
        private View view;

        public TareaLoginUser(View view){
            this.view = view;

        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
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
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
            else{
                /*AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(R.string.DialogUserIncorrect);
                builder.show();*/

                Snackbar.make(view, R.string.DialogUserIncorrect, Snackbar.LENGTH_LONG)
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
            boolean ok = false;
            try {
                URL url=new URL("http://larctrobat.es/notalone/login_user.php" + "?username=" + username.getText().toString() + "&pass=" + password.getText().toString());
                conexion = (HttpURLConnection) url
                        .openConnection();
                if (conexion.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                    String linea = reader.readLine();
                    while (linea !=null && !linea.equals("")) {
                        if (isNumeric(linea)){
                            id = Integer.parseInt(linea);
                            ok = true;

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
            } finally {
                if (conexion!=null) conexion.disconnect();
                return ok;
            }

        }
    }
}
