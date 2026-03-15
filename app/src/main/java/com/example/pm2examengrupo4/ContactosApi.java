package com.example.pm2examengrupo4;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ContactosApi {
    @GET("contactos")
    Call<List<Contacto>> getContactos();

    @POST("contactos")
    Call<Contacto> createContacto(@Body Contacto contacto);

    @POST("contactos/{id}")
    Call<Contacto> updateContacto(@Path("id") String id, @Body Contacto contacto);

    @DELETE("contactos/{id}")
    Call<Void> deleteContacto(@Path("id") String id);
}
