package com.carponcal.notalone;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by carlos on 20/12/2017.
 *
 * Adaptador para el recycler de las actividades del usuario
 *
 */
public class MyActivitiesAdapter extends   RecyclerView.Adapter <MyActivitiesAdapter.ListaViewHolder> {
    protected List<Activity_user> activities;
    protected LayoutInflater inflador;   //Crea Layouts a partir del XML
    protected Context contexto;          //Lo necesitamos para el inflador
    protected View.OnClickListener onClickListener;

    public MyActivitiesAdapter(Context context, List<Activity_user>  objects) {
        this.activities = objects;
        this.contexto = context;
        inflador = (LayoutInflater) contexto
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateData(List<Activity_user> viewModels) {
        if (activities == null)
            activities =  new ArrayList<Activity_user>(viewModels);
        else{
            activities.clear();
            activities.addAll(viewModels);
        }
        
        notifyDataSetChanged();
    }
    public void addItem(int position, Activity_user viewModel) {
        activities.add(position, viewModel);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        activities.remove(position);
        notifyItemRemoved(position);
    }


    public static class ListaViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item

        public ImageView type;
        public TextView name;
        public TextView planification;
        public TextView distance;

        public ListaViewHolder(View v) {
            super(v);
            type = (ImageView) v.findViewById(R.id.foto);
            name = (TextView) v.findViewById(R.id.nombre);
            planification = (TextView) v.findViewById(R.id.planificacion);
            distance = (TextView) v.findViewById(R.id.distancia);
        }
    }

    @Override public int getItemCount() {
        if (activities == null){
            return 0;
        }
        else {
            return activities.size();
        }
    }


    @Override public ListaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.elemento_actividad, viewGroup, false);
        return new ListaViewHolder(v);
    }
    /*Actualizo datos del recycler*/
    @Override public void onBindViewHolder(ListaViewHolder viewHolder, int i) {
        Resources res = contexto.getResources();
        viewHolder.type.setImageResource(activities.get(i).getImage());
        viewHolder.name.setText(activities.get(i).getName());
        Date ahora = new Date();
        Date fecha_actividad = activities.get(i).getPlanification();
        long diferenciaTiempo =  (fecha_actividad.getTime() - ahora.getTime());
        long dias, horas, minutos;

        if (diferenciaTiempo < 0){
            //Si ya ha pasado una hora
            if (diferenciaTiempo < -3600000){
                viewHolder.planification.setText("ACTIVIDAD YA PASADA");
            }
            else{
                viewHolder.planification.setText("ACTIVIDAD EN CURSO");
            }
        }
        else {

            dias = diferenciaTiempo / 86400000;
            horas = (diferenciaTiempo - dias * 86400000) / 3600000;
            minutos = (diferenciaTiempo - dias * 86400000 - horas * 3600000) / 60000;
            if (dias > 0) {
                viewHolder.planification.setText("Quedan " + dias + " días y " + horas + ":" + minutos + " horas");
            } else {
                if (horas > 0) {
                    viewHolder.planification.setText("Quedan solo : " + horas + ":" + minutos + " horas");
                } else {
                    viewHolder.planification.setText("SOLO QUEDAN " + minutos + " MINUTOS");
                }
            }
        }
        float distancia = activities.get(i).getDistance();
        if (distancia < 1000){
            String distanciaString = String.format("%.3f",distancia);
            viewHolder.distance.setText("Está a " + distanciaString + " Mtrs");
        }
        else {
            distancia = distancia/1000;
            String distanciaString = String.format("%.3f",distancia);
            viewHolder.distance.setText("Está a " + distanciaString + " Km");
        }
    }
}