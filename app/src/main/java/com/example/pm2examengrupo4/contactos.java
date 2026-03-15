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

        // Usando configuración global para la IP
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BASE_URL) 
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
                Toast.makeText(contactos.this, "Error al obtener datos: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        // Abrir Google Maps directamente
        String uri = "geo:" + c.getLatitud() + "," + c.getLongitud() + "?z=16&q=" + c.getLatitud() + "," + c.getLongitud() + "(" + Uri.encode(c.getNombre()) + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        
        try {
            startActivity(intent);
        } catch (Exception e) {
            // Si Google Maps no está instalado, usar el navegador u otra app de mapas
            Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(fallbackIntent);
        }
    }

    private void eliminarContacto() {
        if (seleccionado == null) {
            Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            return;
        }
        api.deleteContacto(seleccionado.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                obtenerContactos();
                Toast.makeText(contactos.this, "Eliminado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(contactos.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarContacto() {
        if (seleccionado == null) {
            Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            return;
        }
        seleccionado.setNombre(seleccionado.getNombre() + " (Mod)");
        api.updateContacto(seleccionado.getId(), seleccionado).enqueue(new Callback<Contacto>() {
            @Override
            public void onResponse(Call<Contacto> call, Response<Contacto> response) {
                obtenerContactos();
                Toast.makeText(contactos.this, "Actualizado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Contacto> call, Throwable t) {
                Toast.makeText(contactos.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
