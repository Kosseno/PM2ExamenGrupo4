package com.example.pm2examengrupo4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactoAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<Contacto> originalList;
    private List<Contacto> filteredList;
    private OnVideoClickListener videoClickListener;

    public interface OnVideoClickListener {
        void onVideoClick(Contacto contacto);
    }

    public ContactoAdapter(Context context, List<Contacto> list, OnVideoClickListener listener) {
        this.context = context;
        this.originalList = list;
        this.filteredList = list;
        this.videoClickListener = listener;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        }

        Contacto contacto = filteredList.get(position);
        TextView nombre = convertView.findViewById(R.id.itemNombre);
        TextView telefono = convertView.findViewById(R.id.itemTelefono);
        TextView ubicacion = convertView.findViewById(R.id.itemUbicacion);
        ImageView imgPlay = convertView.findViewById(R.id.imgPlay);

        nombre.setText(contacto.getNombre());
        telefono.setText(contacto.getTelefono());
        ubicacion.setText("Lat: " + contacto.getLatitud() + ", Lng: " + contacto.getLongitud());

        imgPlay.setOnClickListener(v -> {
            if (videoClickListener != null) {
                videoClickListener.onVideoClick(contacto);
            }
        });

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredList = originalList;
                } else {
                    List<Contacto> filtered = new ArrayList<>();
                    for (Contacto row : originalList) {
                        if (row.getNombre().toLowerCase().contains(charString.toLowerCase()) || 
                            row.getTelefono().contains(charString)) {
                            filtered.add(row);
                        }
                    }
                    filteredList = filtered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<Contacto>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
