package com.juego_pomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {

    private TextView textTimerWork;
    private TextView textTimerRest;
    private TextView textName;
    private TextView textEmail;
    private CardView containerController;
    private boolean isWorking = true;
    private boolean isTimerRunning = false;
    private ImageView imgController, imgPerson;
    private ImageView btnLogout;
    private ImageView btnTodos;
    private User user;
    private static final String SHARED_PREFS_NAME = "user_prefs";
    private static final String KEY_USER = "user";
    private AlertDialog dialogRest;
    private AlertDialog dialogWork;
    private ArrayList<TodoModel> todoList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTimerWork = findViewById(R.id.text_timer_work);
        textTimerRest = findViewById(R.id.text_timer_rest);
        imgController = findViewById(R.id.img_controller);
        imgPerson = findViewById(R.id.img_person);
        textName = findViewById(R.id.text_name);
        textEmail = findViewById(R.id.text_email);
        btnLogout = findViewById(R.id.btn_logout);
        btnTodos = findViewById(R.id.btn_todos);
        containerController = findViewById(R.id.container_controller);
        user = getUserFromMemory(this);
        createDialogRest();
        createDialogWork();
        if (user.gender.equals("female")) {
            imgPerson.setImageResource(R.drawable.baseline_woman_24);
        } else {
            imgPerson.setImageResource(R.drawable.baseline_man_24);
        }

        textName.setText(user.lastName + " " + user.firstName);
        textEmail.setText(user.email);

        Intent intent = new Intent(this, TimeService.class);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();

            }
        });
        containerController.setOnClickListener(v -> {
            if (!isTimerRunning) {
                intent.putExtra(TimeService.TIMER_ACTION, TimeService.TIMER_PLAY);
                startService(intent);
            } else {
                if(todoList == null || todoList.isEmpty()) {
                    showDialogRest();
                } else {
                    intent.putExtra(TimeService.TIMER_ACTION, TimeService.TIMER_RESTART);
                    startService(intent);
                }

            }
        });

        btnTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTodoListActivity();
            }
        });

        registerReceiver(timerReceiver, new IntentFilter(TimeService.TIMER_UPDATED));
        intent.putExtra(TimeService.TIMER_ACTION, TimeService.TIMER_CHECK_STATUS);
        startService(intent);
        new GetTodosTask().execute(user.id);

    }


    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long timeLeftWork = intent.getLongExtra(TimeService.TIME_LEFT_WORK, 0);
            long timeLeftRest = intent.getLongExtra(TimeService.TIME_LEFT_REST, 0);
            boolean isRunningRimer = intent.getBooleanExtra(TimeService.TIMER_STATUS_RUNNING, false);
            boolean isWorking = intent.getBooleanExtra(TimeService.TIMER_IS_WORKING, true);
            boolean restInitDialog = intent.getBooleanExtra(TimeService.TIMER_DIALOG_REST_SHOWING, false);
            boolean workInitDialog = intent.getBooleanExtra(TimeService.TIMER_DIALOG_WORK_SHOWING, false);
            updateTimerText(timeLeftWork, timeLeftRest, isRunningRimer, isWorking, restInitDialog,workInitDialog);
        }
    };

    private void showDialogRest() {
        if (!dialogRest.isShowing()) {
            dialogRest.show();
        }
    }

    private void showDialogWork() {
        if (!dialogWork.isShowing()) {
            dialogWork.show();
        }
    }
        //timer brindadio por chatGPT, de tal modo que este continue en funcionamiento pese a saltar o regresar en pantallas.
    private void updateTimerText(long timeLeftWork, long timeLeftRest, boolean isTimerRunning, boolean isWorking, boolean restInitDialog, boolean workInitDialog) {
        this.isTimerRunning = isTimerRunning;
        this.isWorking = isWorking;
        if (restInitDialog) {
            showDialogRest();
        }

        if(workInitDialog) {
            if(dialogRest.isShowing()) {
                dialogRest.dismiss();
            }
            showDialogWork();
        }

        if (isTimerRunning) {
            imgController.setImageResource(R.drawable.baseline_restore_24);

        } else {
            imgController.setImageResource(R.drawable.baseline_play_arrow_24);

        }
        int minutesWork = (int) (timeLeftWork / 1000) / 60;
        int secondsWork = (int) (timeLeftWork / 1000) % 60;

        int minutesRest = (int) (timeLeftRest / 1000) / 60;
        int secondsRest = (int) (timeLeftRest / 1000) % 60;

        String timeFormattedWork = String.format(Locale.getDefault(), "%02d:%02d", minutesWork, secondsWork);
        String timeFormattedRest = String.format(Locale.getDefault(), "%02d:%02d", minutesRest, secondsRest);

        textTimerWork.setText(timeFormattedWork);
        textTimerRest.setText(timeFormattedRest);

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

    private void createDialogRest() {
        MaterialAlertDialogBuilder builder  = new MaterialAlertDialogBuilder(this);
        builder.setTitle("¡Felicidades!");
        builder.setMessage("Empezó el tiempo de descanso!");
        builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, TimeService.class);

                intent.putExtra(TimeService.TIMER_ACTION, TimeService.TIMER_DIALOG_REST_CLOSE);
                startService(intent);
            }
        });

        dialogRest = builder.create();
    }

    private void createDialogWork() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("¡Atención!")
                .setMessage("Terminó el tiempo de descanso. Dale al\nbotón de reinicio para empezar otro ciclo.")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, TimeService.class);
                        intent.putExtra(TimeService.TIMER_ACTION, TimeService.TIMER_DIALOG_WORK_CLOSE);
                        startService(intent);
                    }
                });

        dialogWork = builder.create();
    }

    private void checkTotalTodos(ArrayList<TodoModel> todos) {
        if(todos.size() >= 1) {
            goToTodoListActivity();
        }
    }


    private void goToTodoListActivity() {
        Intent intent = new Intent(MainActivity.this, TodoListActivity.class);

        Gson gson = new Gson();
        String todoListJson = gson.toJson(todoList);
        intent.putExtra("todoList", todoListJson);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(user != null)  {
            new GetTodosTaskFromBack().execute(user.id);
        }
    }

    private class GetTodosTask extends AsyncTask<Integer, Void, TodoListResponse> {
        @Override
        protected TodoListResponse doInBackground(Integer... params) {
            int idUser = params[0];
            TodoListResponse todoListResponse = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                URL url = new URL("https://dummyjson.com/todos/user/" + idUser);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                reader = new BufferedReader(inputStream);

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Gson gson = new Gson();

                todoListResponse = gson.fromJson(response.toString(), TodoListResponse.class);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return todoListResponse;
        }

        @Override
        protected void onPostExecute(TodoListResponse todoListResponse) {
            if (todoListResponse != null && todoListResponse.todos != null) {

                todoList = todoListResponse.todos;
                checkTotalTodos(todoListResponse.todos);
            }
        }
    }

    private class GetTodosTaskFromBack extends AsyncTask<Integer, Void, TodoListResponse> {
        @Override
        protected TodoListResponse doInBackground(Integer... params) {
            int idUser = params[0];
            TodoListResponse todoListResponse = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                URL url = new URL("https://dummyjson.com/todos/user/" + idUser);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                reader = new BufferedReader(inputStream);

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Gson gson = new Gson();

                todoListResponse = gson.fromJson(response.toString(), TodoListResponse.class);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return todoListResponse;
        }

        @Override
        protected void onPostExecute(TodoListResponse todoListResponse) {
            if (todoListResponse != null && todoListResponse.todos != null) {

                todoList = todoListResponse.todos;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timerReceiver);
    }
}
