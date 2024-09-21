package com.juego_pomodoro;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Timer;

public class TimeService extends Service {
    private static final long WORK_TIME = 5 * 60 * 1000;
    private static final long REST_TIME = 1 * 60 * 1000;
    private long timeLeftInMillisWork = WORK_TIME;
    private long timeLeftInMillisRest = REST_TIME;
    private CountDownTimer countDownTimer;
    private boolean isWorking = true;
    private boolean isShowDialogRest = false;
    private boolean isShowDialogWork = false;
    public static final String TIMER_UPDATED = "TIMER_UPDATED";
    public static final String TIME_LEFT_WORK = "timeLeftWork";
    public static final String TIME_LEFT_REST = "timeLeftRest";
    public static final String TIMER_RESTART = "timerRestart";
    public static final String TIMER_PLAY = "timerPlay";
    public static final String TIMER_ACTION = "timerAction";
    public static final String TIMER_STATUS_RUNNING = "timerStatusRunning";
    public static final String TIMER_CHECK_STATUS= "timerCheckStatus";
    public static final String TIMER_IS_WORKING = "timerCheckStatus";
    public static final String TIMER_DIALOG_REST_SHOWING = "timer_dialog_rest_showing";
    public static final String TIMER_DIALOG_REST_CLOSE = "timer_dialog_rest_close";
    public static final String TIMER_DIALOG_WORK_SHOWING = "timer_dialog_work_showing";
    public static final String TIMER_DIALOG_WORK_CLOSE = "timer_dialog_work_close";
    public boolean isRunningTimer = false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getStringExtra(TIMER_ACTION);
            if(action.equals(TIMER_RESTART)) {
                restartTimer();
            } else if(action.equals(TIMER_PLAY)) {
                startTimer();
            } else if(action.equals(TIMER_CHECK_STATUS)) {
                sendTimeUpdate();
            } else if(action.equals(TIMER_DIALOG_REST_CLOSE)) {
                isShowDialogRest = false;
            } else if(action.equals(TIMER_DIALOG_WORK_CLOSE)) {
                isShowDialogWork = false;
            }
        }
        return START_STICKY;
    }

    private void restartTimer() {
        Log.e("pomodoro","pause");
        isRunningTimer = true;
        isWorking = true;
        timeLeftInMillisWork = WORK_TIME;
        timeLeftInMillisRest = REST_TIME;
        countDownTimer.cancel();
        startTimer();

    }

    private void startTimer() {
        Log.e("pomodoro","play");

        isRunningTimer = true;
        Long timeLeftInMillis;
        if(isWorking) {
            timeLeftInMillis = timeLeftInMillisWork;
        } else {
            timeLeftInMillis = timeLeftInMillisRest;
        }
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(isWorking) {
                    timeLeftInMillisWork = millisUntilFinished;
                } else {
                    timeLeftInMillisRest = millisUntilFinished;
                }
                sendTimeUpdate();
            }

            @Override
            public void onFinish() {

                if (isWorking) {
                    isWorking = false;
                    isShowDialogRest = true;
                    startTimer();
                } else {
                    isShowDialogWork = true;
                    sendTimeUpdate();
                }

            }
        }.start();
    }

    private void sendTimeUpdate() {
        Intent intent = new Intent(TIMER_UPDATED);
        intent.putExtra(TIME_LEFT_WORK, timeLeftInMillisWork);
        intent.putExtra(TIME_LEFT_REST, timeLeftInMillisRest);
        intent.putExtra(TIMER_STATUS_RUNNING, isRunningTimer);
        intent.putExtra(TIMER_IS_WORKING,isWorking);
        intent.putExtra(TIMER_DIALOG_REST_SHOWING,isShowDialogRest);
        intent.putExtra(TIMER_DIALOG_WORK_SHOWING,isShowDialogWork);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}