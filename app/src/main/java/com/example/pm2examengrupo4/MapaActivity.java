package com.example.pm2examengrupo4;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaActivity extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración obligatoria de OSMDroid
        Configuration.getInstance().load(this,
            PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_mapa);

        double lat = Double.parseDouble(getIntent().getStringExtra("latitud"));
        double lng = Double.parseDouble(getIntent().getStringExtra("longitud"));
        String nombre = getIntent().getStringExtra("nombre");

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK); // OpenStreetMap
        map.setMultiTouchControls(true);

        // Centrar mapa en la ubicación
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(lat, lng));

        // Agregar marcador
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(lat, lng));
        marker.setTitle(nombre);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
