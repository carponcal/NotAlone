package com.carponcal.notalone;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by carlos on 20/12/2017.
 * Extrae a través de JSON los datos que devuelve la web
 */
public class JsonActivityParser {

    public List<Activity_user> leerFlujoJson(InputStream in) throws IOException, JSONException {
        //Convertir a JsonObjext el inputStream
        // Lista temporal
        ArrayList<Activity_user> activities = new ArrayList<>();
        /*
        JSONParser jsonParser = new JSONParser();
        org.json.simple.JSONObject jsonObject = new org.json.simple.JSONObject();

        try {
            jsonObject = (org.json.simple.JSONObject) jsonParser.parse(new InputStreamReader(in, "UTF-8"));
            //JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        }
        catch (org.json.simple.parser.ParseException e){
            e.printStackTrace();
        }

        */

        BufferedReader bR = new BufferedReader(  new InputStreamReader(in));
        String line = "";

        StringBuilder responseStrBuilder = new StringBuilder();
        while((line =  bR.readLine()) != null){

            responseStrBuilder.append(line);
        }
        in.close();

        JSONObject result= new JSONObject(responseStrBuilder.toString());
        try {
            //Leer ObjetoActividades
            activities =  leerObjetoActividades(result);
            // Leer Array
            //return leerArrayActividades(reader);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            //reader.close();
        }
        return activities;
    }

    public ArrayList<Activity_user> leerObjetoActividades(JSONObject object) throws JSONException {
        // Lista temporal
        ArrayList<Activity_user> activities = new ArrayList<>();
        int id = 0;
        String name = null;
        double longi = 0;
        double lat = 0;
        int type = 0;
        String description = null;
        String planification = null;
        int userProp = 0;
        int numUsers = 0;
        String propName = "";
        JSONArray activity_array = object.getJSONArray("activities");
        for(int i = 0 ; i < activity_array.length(); i++){
            JSONObject elemento = activity_array.getJSONObject(i);
            JSONObject activity = elemento.getJSONObject("activity");
            id = activity.getInt("act_id");
            name = activity.getString("act_name");
            description = activity.getString("act_description");
            planification = activity.getString("act_planification");
            type = activity.getInt("act_type");
            longi = activity.getDouble("act_position_long");
            lat = activity.getDouble("act_position_lat");
            userProp = activity.getInt("ua_user_prop");
            numUsers = activity.getInt("num_users");
            propName = activity.getString("us_username");
            Location location = new Location("GPS_PROVIDER");
            location.setLatitude(lat);
            location.setLongitude(longi);
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.ENGLISH);
            try {
                date = format.parse(planification);
            }
            catch (ParseException e)
            {}
            Activity_user act_user = new Activity_user(id,name,description,location, date,type,userProp,propName, numUsers);
            activities.add(act_user);
        }



        return activities;
    }





}

