package com.example.pm2examengrupo4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class video extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CODE = 100;

    private EditText txtNombre, txtTelefono, txtLatitud, txtLongitud;
    private Button btnTomarVideo, btnSalvarContacto, btnContactosSalvados;
    private String videoBase64 = "";
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        txtNombre = findViewById(R.id.txtnombre);
        txtTelefono = findViewById(R.id.txttelefono);
        txtLatitud = findViewById(R.id.txtlatitud);
        txtLongitud = findViewById(R.id.txtlongitud);
        btnTomarVideo = findViewById(R.id.btntomarvideo);
        btnSalvarContacto = findViewById(R.id.btnsalvarcontacto);
        btnContactosSalvados = findViewById(R.id.btncontactossalvados);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnTomarVideo.setOnClickListener(v -> checkPermissionsAndCaptureVideo());
        btnSalvarContacto.setOnClickListener(v -> salvarContacto());
        btnContactosSalvados.setOnClickListener(v -> {
            startActivity(new Intent(this, contactos.class));
        });

        obtenerUbicacion();
    }

    private void checkPermissionsAndCaptureVideo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE);
        } else {
            dispatchTakeVideoIntent();
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            convertVideoToBase64(videoUri);
        }
    }

    private void convertVideoToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            videoBase64 = Base64.encodeToString(byteBuffer.toByteArray(), Base64.DEFAULT);
            Toast.makeText(this, "Video capturado y procesado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar video", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                txtLatitud.setText(String.valueOf(location.getLatitude()));
                txtLongitud.setText(String.valueOf(location.getLongitude()));
            }
        });
    }

    private void salvarContacto() {
        String nombre = txtNombre.getText().toString();
        String telefono = txtTelefono.getText().toString();
        String latitud = txtLatitud.getText().toString();
        String longitud = txtLongitud.getText().toString();

        if (nombre.isEmpty() || telefono.isEmpty() || videoBase64.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos y tome un video", Toast.LENGTH_SHORT).show();
            return;
        }

        Contacto nuevo = new Contacto(null, nombre, telefono, latitud, longitud, videoBase64);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BASE_URL) // Usando configuración global
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ContactosApi api = retrofit.create(ContactosApi.class);
        api.createContacto(nuevo).enqueue(new Callback<Contacto>() {
            @Override
            public void onResponse(Call<Contacto> call, Response<Contacto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(video.this, "Contacto guardado con éxito", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                } else {
                    Toast.makeText(video.this, "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Contacto> call, Throwable t) {
                Toast.makeText(video.this, "Falla de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtTelefono.setText("");
        videoBase64 = "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                Toast.makeText(this, "Permisos necesarios denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
