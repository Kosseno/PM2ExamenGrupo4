package com.example.pm2examengrupo4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class contactos extends AppCompatActivity {

    private ListView listView;
    private SearchView searchView;
    private Button btnEliminar, btnActualizar;
    private ContactoAdapter adapter;
    private List<Contacto> listaContactos = new ArrayList<>();
    private Contacto seleccionado;
    private ContactosApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);

        listView = findViewById(R.id.listViewContactos);
        searchView = findViewById(R.id.searchView);
        btnEliminar = findViewById(R.id.btneliminarcontacto);
        btnActualizar = findViewById(R.id.btnactualizarcontacto);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://tu-api-aqui.com/") // REEMPLAZAR
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ContactosApi.class);

        obtenerContactos();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            seleccionado = (Contacto) adapter.getItem(position);
            reproducirVideo(seleccionado.getVideoBase64());
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            seleccionado = (Contacto) adapter.getItem(position);
            abrirEnMapa(seleccionado);
            return true;
        });

        btnEliminar.setOnClickListener(v -> eliminarContacto());
        btnActualizar.setOnClickListener(v -> actualizarContacto());
    }

    private void obtenerContactos() {
        api.getContactos().enqueue(new Callback<List<Contacto>>() {
            @Override
            public void onResponse(Call<List<Contacto>> call, Response<List<Contacto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaContactos = response.body();
                    adapter = new ContactoAdapter(contactos.this, listaContactos);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Contacto>> call, Throwable t) {
                Toast.makeText(contactos.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reproducirVideo(String base64) {
        try {
            byte[] videoBytes = Base64.decode(base64, Base64.DEFAULT);
            File tempFile = File.createTempFile("temp_video", ".mp4", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(videoBytes);
            fos.close();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(tempFile), "video/mp4");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error al reproducir video", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirEnMapa(Contacto c) {
        // Opción 1: Mapa Interno
        Intent intent = new Intent(this, MapaActivity.class);
        intent.putExtra("latitud", c.getLatitud());
        intent.putExtra("longitud", c.getLongitud());
        intent.putExtra("nombre", c.getNombre());
        startActivity(intent);

        // Opción 2: Google Maps External (Extra)
        /*
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + c.getLatitud() + "," + c.getLongitud() + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
        */
    }

    private void eliminarContacto() {
        if (seleccionado == null) return;
        api.deleteContacto(seleccionado.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                obtenerContactos();
                Toast.makeText(contactos.this, "Eliminado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void actualizarContacto() {
        if (seleccionado == null) return;
        // Aquí se podría abrir un diálogo o actividad para editar
        // Por simplicidad, mandamos el mismo con nombre modificado
        seleccionado.setNombre(seleccionado.getNombre() + " (Mod)");
        api.updateContacto(seleccionado.getId(), seleccionado).enqueue(new Callback<Contacto>() {
            @Override
            public void onResponse(Call<Contacto> call, Response<Contacto> response) {
                obtenerContactos();
                Toast.makeText(contactos.this, "Actualizado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Contacto> call, Throwable t) {}
        });
    }
}
