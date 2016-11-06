package com.blackberry.dhalam.pdt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MotionActivity extends AppCompatActivity implements SensorEventListener{
    Button mStop;
    Button mStart;
    TextView mTextView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private float last_x;
    private float last_y;
    private float last_z;
    private long lastUpdate;

    private static final String Separator = System.lineSeparator();
    private static final int SHAKE_THRESHOLD = 800;
    public String data;


    String contents;

    private Handler handler;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);
        //hide the action bar
        getSupportActionBar().hide();

        mStop = (Button) findViewById(R.id.stopMotionButton);
        mStart = (Button) findViewById(R.id.startMotionButton);
        mTextView = (TextView) findViewById(R.id.textView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        last_x = 0;
        last_y = 0;
        last_z = 0;
        lastUpdate = 0;
        data = "";

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStart.setEnabled(false);
                mSensorManager.registerListener(MotionActivity.this, mAccelerometer,
                        SensorManager.SENSOR_DELAY_GAME);
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                        setupOkhttp();
                        mStart.setEnabled(true);
                    }
                }, 20000);

            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
                data = data + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z) + "," + String.valueOf(mAccel) + "," + String.valueOf(speed) + ",";


            }
        }
    }

    public void stop(){
        mSensorManager.unregisterListener(this,mAccelerometer);

        /*
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (Exception ex){
        }
        */
        contents = new String(data);

        String[] s = contents.split(",");
        String cont = "";
        for (int i = 0; i< 500; i++) {
            if (i == 499){
                cont = cont + s[i];
            } else {
                cont = cont + s[i] + ",";
            }
        }
        contents = cont;
        mTextView.setText("can restart");

        last_x = 0;
        last_y = 0;
        last_z = 0;
        lastUpdate = 0;
        data = "";
    }
    public void setupOkhttp(){
        // should be a singleton
        OkHttpClient client = new OkHttpClient();
        JSONObject output = new JSONObject();

        try{
            output.put("data",contents);
        } catch (JSONException ex){

        }

        Request request = new Request.Builder()
                .url("http://ec2-35-162-139-63.us-west-2.compute.amazonaws.com/api/v1.0/dataset")
                .post(RequestBody.create(JSON,output.toString()))
                .build();

        // Get a handler that can be used to post to the main thread
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    final String responseData = response.body().string();
                }
            }
        });
        Log.d("test", "setupOkhttp");
    }

}
