package com.example.pm2examengrupo4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

        // Click en el item de la lista solo para SELECCIONAR
        listView.setOnItemClickListener((parent, view, position, id) -> {
            seleccionado = (Contacto) adapter.getItem(position);
            Toast.makeText(this, "Seleccionado: " + seleccionado.getNombre(), Toast.LENGTH_SHORT).show();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            seleccionado = (Contacto) adapter.getItem(position);
            abrirEnMapa(seleccionado);
            return true;
        });

        btnEliminar.setOnClickListener(v -> eliminarContacto());
        btnActualizar.setOnClickListener(v -> mostrarDialogoEdicion());
    }

    private void obtenerContactos() {
        api.getContactos().enqueue(new Callback<List<Contacto>>() {
            @Override
            public void onResponse(Call<List<Contacto>> call, Response<List<Contacto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaContactos = response.body();
                    // Implementamos OnVideoClickListener para que el video solo abra al tocar el icono
                    adapter = new ContactoAdapter(contactos.this, listaContactos, contacto -> {
                        reproducirVideo(contacto.getVideoBase64());
                    });
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
            if (base64 == null || base64.isEmpty()) {
                Toast.makeText(this, "El contacto no tiene un video guardado", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] videoBytes = Base64.decode(base64, Base64.DEFAULT);
            File tempFile = new File(getExternalFilesDir(null), "temp_video.mp4");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(videoBytes);
            fos.close();

            Uri videoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(videoUri, "video/mp4");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void abrirEnMapa(Contacto c) {
        String uri = "geo:" + c.getLatitud() + "," + c.getLongitud() + "?z=16&q=" + c.getLatitud() + "," + c.getLongitud() + "(" + Uri.encode(c.getNombre()) + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        
        try {
            startActivity(intent);
        } catch (Exception e) {
            Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(fallbackIntent);
        }
    }

    private void eliminarContacto() {
        if (seleccionado == null) {
            Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Desea eliminar a " + seleccionado.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    api.deleteContacto(seleccionado.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            obtenerContactos();
                            Toast.makeText(contactos.this, "Eliminado", Toast.LENGTH_SHORT).show();
                            seleccionado = null;
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(contactos.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void mostrarDialogoEdicion() {
        if (seleccionado == null) {
            Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editar_contacto, null);

        EditText etNombre = view.findViewById(R.id.etNombreEdit);
        EditText etTelefono = view.findViewById(R.id.etTelefonoEdit);
        EditText etLatitud = view.findViewById(R.id.etLatitudEdit);
        EditText etLongitud = view.findViewById(R.id.etLongitudEdit);

        etNombre.setText(seleccionado.getNombre());
        etTelefono.setText(seleccionado.getTelefono());
        etLatitud.setText(seleccionado.getLatitud());
        etLongitud.setText(seleccionado.getLongitud());

        // Latitud y Longitud bloqueadas (read-only)
        etLatitud.setEnabled(false);
        etLongitud.setEnabled(false);

        builder.setView(view)
                .setTitle("Editar Contacto")
                .setPositiveButton("Actualizar", (dialog, id) -> {
                    seleccionado.setNombre(etNombre.getText().toString());
                    seleccionado.setTelefono(etTelefono.getText().toString());
                    // Aunque estén bloqueados en la UI, enviamos lo que tienen
                    actualizarContacto();
                })
                .setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void actualizarContacto() {
        api.updateContacto(seleccionado.getId(), seleccionado).enqueue(new Callback<Contacto>() {
            @Override
            public void onResponse(Call<Contacto> call, Response<Contacto> response) {
                obtenerContactos();
                Toast.makeText(contactos.this, "Actualizado con éxito", Toast.LENGTH_SHORT).show();
                seleccionado = null;
            }

            @Override
            public void onFailure(Call<Contacto> call, Throwable t) {
                Toast.makeText(contactos.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
