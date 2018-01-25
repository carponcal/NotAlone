package com.carponcal.notalone;

import android.location.Location;

import java.util.Date;

/**
 * Created by carlos on 21/12/2017.
 * Clase Actividad: Clase base de las actividades con las que trabaja la app
 */

public class Activity_user {
    private int id;
    private String name;
    private String description;
    private Location location;
    private Date planification;
    private int type;
    private int propietary;
    private int image;
    private int typeName;
    private int numUsers;
    private String propietaryName;
    private boolean news;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    private float distance;

    public Activity_user(int id, String name, String description, Location location, Date planification, int type, int propietary, String propietaryName, int numUsers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.planification = planification;
        this.type = type;
        this.propietary = propietary;
        this.propietaryName = propietaryName;
        this.numUsers = numUsers;
        this.news = false;
        selectTypeActivity();
    }

    public int getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(int numUsers) {
        this.numUsers = numUsers;
    }

    public String getPropietaryName() {
        return propietaryName;
    }

    public void setPropietaryName(String propietaryName) {
        this.propietaryName = propietaryName;
    }

    public void selectTypeActivity(){
        switch (type){
            case 4:
                image = R.drawable.ic_bike_black_48dp;
                typeName = R.string.cycling;
                break;
            case 6:
                image  = R.drawable.ic_image_filter_hdr_black_48dp;
                typeName =R.string.trekking;
                break;
            case 2:
                image = R.drawable.ic_walk_black_48dp;
                typeName = R.string.walking;
                break;
            case 1:
                image = R.drawable.ic_run_black_48dp;
                typeName = R.string.running;
                break;
            case 7:
                image = R.drawable.ic_soccer_black_48dp;
                typeName = R.string.football;
                break;
            case 8:
                image = R.drawable.ic_basketball_black_48dp;
                typeName = R.string.basketball;
                break;
            case 5:
                image = R.drawable.ic_tennis_black_48dp;
                typeName = R.string.tennis;
                break;
            case 3:
                image = R.drawable.rollerblade;
                typeName = R.string.rollerblading;
                break;
            default:
                image = 0;
                break;

        }

    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getTypeName() {
        return typeName;
    }

    public void setTypeName(int typeName) {
        this.typeName = typeName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getPlanification() {
        return planification;
    }

    public void setPlanification(Date planification) {
        this.planification = planification;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPropietary() {
        return propietary;
    }

    public void setPropietary(int propietary) {
        this.propietary = propietary;
    }

    public int getResourceType(){
        int result = 0;
        switch (this.type) {
            case 1:
                result = R.drawable.ic_run_black_48dp;
                break;
            case 2:
                result = R.drawable.ic_walk_black_48dp;
                break;
            case 3:
                result = R.drawable.rollerblade;
                break;
            case 4:
                result = R.drawable.ic_bike_black_48dp;
                break;
            case 5:
                result = R.drawable.ic_tennis_black_48dp;
                break;
            case 6:
                result = R.drawable.ic_image_filter_hdr_black_48dp;
                break;
            case 7:
                result = R.drawable.ic_soccer_black_48dp;
                break;
            case 8:
                result = R.drawable.ic_basketball_black_48dp;
                break;
        }
        return result;
    }





}
