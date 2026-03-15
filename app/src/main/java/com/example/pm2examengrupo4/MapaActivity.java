package com.example.pm2examengrupo4;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.TextView;

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

        // UI Elements
        map = findViewById(R.id.map);
        TextView txtNombre = findViewById(R.id.txtNombreMapa);
        TextView txtCoords = findViewById(R.id.txtCoordsMapa);
        ImageButton btnBack = findViewById(R.id.btnBack);

        txtNombre.setText(nombre);
        txtCoords.setText(String.format("Lat: %.6f, Lng: %.6f", lat, lng));
        btnBack.setOnClickListener(v -> finish());

        // Configuración del Mapa
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(17.0);
        GeoPoint startPoint = new GeoPoint(lat, lng);
        map.getController().setCenter(startPoint);

        // Agregar marcador personalizado
        Marker marker = new Marker(map);
        marker.setPosition(startPoint);
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
