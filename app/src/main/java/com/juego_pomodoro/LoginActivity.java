// LoginActivity.java
package com.juego_pomodoro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUser, edtPassword;
    private TextInputLayout tilUser, tilPassword;
    private Button btnLogin;

    private static final String SHARED_PREFS_NAME = "user_prefs";
    private static final String KEY_USER = "user";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUser = findViewById(R.id.edt_user);
        edtPassword = findViewById(R.id.edt_password);
        tilUser = findViewById(R.id.til_user);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryLogin();
            }
        });
    }
//Intenté que los inputs dieran un error mas sencillo (cuando es vacio)
    private void tryLogin() {
        String username = edtUser.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty()) {
            tilUser.setError("El usuario no puede estar vacío");
            return;
        } else {
            tilUser.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("La contraseña no puede estar vacía");
            return;
        } else {
            tilPassword.setError(null);
        }
//Solicté que con lo que me brindó en RequestLogin, ApiService y RetroFitClient funcionara el login
        //Adicional con unos mensajes de error, porque las primeras versiones que me dio no funcionaban
        //del todo correcto (logs de ayuda -> mensajes de error en pantalla)
        RequestLogin requestLogin = new RequestLogin();
        requestLogin.username = username;
        requestLogin.password = password;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.login(requestLogin).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    saveUserInMemory(user);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    tilUser.setError("Usuario o contraseña incorrectos");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                tilUser.setError("Error de red, por favor intente de nuevo.");
            }
        });
    }

    private void saveUserInMemory(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            JSONObject jsonUser = new JSONObject();
            jsonUser.put("firstName", user.firstName);
            jsonUser.put("lastName", user.lastName);
            jsonUser.put("gender", user.gender);
            jsonUser.put("email", user.email);
            jsonUser.put("username", user.username);
            jsonUser.put("id", user.id);

            editor.putString(KEY_USER, jsonUser.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
