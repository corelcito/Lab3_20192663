package com.juego_pomodoro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TodoListActivity extends AppCompatActivity {

    private Gson gson = new Gson();
    private ArrayList<TodoModel> todoList;
    private Spinner spinner;
    private TodoModel selectedTodo;
    private TextView textName;
    private ImageView btnBack;
    private User user;
    private Button btnChangeState;
    private static final String SHARED_PREFS_NAME = "user_prefs";
    private static final String KEY_USER = "user";
    private ImageView btnLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        user = getUserFromMemory(this);

        String todoListJson = getIntent().getStringExtra("todoList");

        Type todoListType = new com.google.gson.reflect.TypeToken<ArrayList<TodoModel>>() {}.getType();
        todoList = gson.fromJson(todoListJson, todoListType);

        spinner = findViewById(R.id.spinner);
        btnBack = findViewById(R.id.btn_back);
        btnChangeState = findViewById(R.id.button);
        textName = findViewById(R.id.text_name);
        btnLogout = findViewById(R.id.btn_logout);
        textName.setText(user.firstName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getTodoStrings());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TodoListActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnChangeState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = spinner.getSelectedItemPosition();
                selectedTodo = todoList.get(selectedPosition);

                boolean newCompletedStatus = !selectedTodo.completed;
                new ChangeTodoStateTask().execute(selectedTodo.id, newCompletedStatus);
            }
        });
    }

    private ArrayList<String> getTodoStrings() {
        ArrayList<String> todoStrings = new ArrayList<>();
        for (TodoModel todo : todoList) {
            todoStrings.add(todo.todo + "-" + (todo.completed ? "Completado" : "No Completado"));
        }
        return todoStrings;
    }

    private class ChangeTodoStateTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            int todoId = (int) params[0];
            boolean newCompletedStatus = (boolean) params[1];
            HttpURLConnection urlConnection = null;

            try {

                URL url = new URL("https://dummyjson.com/todos/" + todoId);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);


                String requestBody = "{ \"completed\": " + newCompletedStatus + " }";
                OutputStream os = urlConnection.getOutputStream();
                os.write(requestBody.getBytes());
                os.flush();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(TodoListActivity.this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show();

                selectedTodo.completed = !selectedTodo.completed;

                ((ArrayAdapter) spinner.getAdapter()).notifyDataSetChanged();
                finish();
            } else {
                Toast.makeText(TodoListActivity.this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static User getUserFromMemory(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userJsonString = sharedPreferences.getString(KEY_USER, null);

        if (userJsonString != null) {
            try {
                JSONObject jsonUser = new JSONObject(userJsonString);
                User user = new User();
                user.firstName = jsonUser.getString("firstName");
                user.lastName = jsonUser.getString("lastName");
                user.gender = jsonUser.getString("gender");
                user.email = jsonUser.getString("email");
                user.username = jsonUser.getString("username");
                user.id = jsonUser.getInt("id");
                return user;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
