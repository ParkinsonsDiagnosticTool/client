package com.blackberry.dhalam.pdt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class MotionService extends Service implements SensorEventListener {
    public MotionService() {
    }

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private float last_x;
    private float last_y;
    private float last_z;
    private long lastUpdate;

    private String fileName = "log.txt";
    File file;
    private static final String Separator = System.lineSeparator();
    private static final int SHAKE_THRESHOLD = 800;
    public String data;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_GAME, new Handler());
        file = new File(getApplicationContext().getFilesDir(), fileName);
        last_x = 0;
        last_y = 0;
        last_z = 0;
        lastUpdate = 0;
        data = "";


        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        // only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                Log.d("sensor", "shake detected w/ speed: " + speed);
            }
            last_x = x;
            last_y = y;
            last_z = z;
            data = data + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + "," + String.valueOf(mAccel)  + "," + String.valueOf(speed) + ",";



        }
    }
    @Override
    public void onDestroy(){
        mSensorManager.unregisterListener(this,mAccelerometer);
        last_x = 0;
        last_y = 0;
        last_z = 0;
        lastUpdate = 0;
        data = "";
        super.onDestroy();
        try {
            FileOutputStream outputStream = openFileOutput(file.getName(), MODE_APPEND);
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();

        } catch (Exception ex) {

        }

    }

}
