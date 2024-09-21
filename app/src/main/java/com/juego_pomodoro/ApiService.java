// ApiService.java
package com.juego_pomodoro;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<User> login(@Body RequestLogin requestLogin);
}
