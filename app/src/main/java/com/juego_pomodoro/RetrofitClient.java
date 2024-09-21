// RetrofitClient.java
package com.juego_pomodoro;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//solicite a ChatGPT como podria hacer un login brindandole los atributos
//necesarios de la clase user, el url y es´pecificando que deberia ser con
//retrofit. Me brindo el ApiService, RequestLogin y RetrofitClient.
public class RetrofitClient {
    private static final String BASE_URL = "https://dummyjson.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
