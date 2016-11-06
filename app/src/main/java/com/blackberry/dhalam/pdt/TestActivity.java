package com.blackberry.dhalam.pdt;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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

public class TestActivity extends AppCompatActivity implements SensorEventListener {
    private int numClicks = 0;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    //sensor variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private float last_x;
    private float last_y;
    private float last_z;
    private long lastUpdate;

    //data
    private static final int SHAKE_THRESHOLD = 800;
    private String data;
    private String contents;

    //graphs
    private GraphView graph1; //x,y,z
    private GraphView graph2; //accel
    private GraphView graph3; //accel

    private LineGraphSeries<DataPoint> mSeries1; //x
    private LineGraphSeries<DataPoint> mSeries2;  //y
    private LineGraphSeries<DataPoint> mSeries3;  //z
    private LineGraphSeries<DataPoint> mSeries4;  //accel
    private LineGraphSeries<DataPoint> mSeries5;  //speed



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //set defaults
        last_x = 0;
        last_y = 0;
        last_z = 0;
        lastUpdate = 0;
        data = "";

        mSeries1 = new LineGraphSeries<>();
        mSeries1.setColor(ContextCompat.getColor(TestActivity.this, R.color.line1));

        mSeries2 = new LineGraphSeries<>();
        mSeries2.setColor(ContextCompat.getColor(TestActivity.this, R.color.line2));

        mSeries3 = new LineGraphSeries<>();
        mSeries3.setColor(ContextCompat.getColor(TestActivity.this, R.color.line3));

        mSeries4 = new LineGraphSeries<>();
        mSeries4.setColor(ContextCompat.getColor(TestActivity.this, R.color.line4));

        mSeries5 = new LineGraphSeries<>();
        mSeries5.setColor(ContextCompat.getColor(TestActivity.this, R.color.line5));

        graph1 = (GraphView) findViewById(R.id.graph1);
        graph1.setTitle("X Axis");
        graph1.addSeries(mSeries1);
        StaticLabelsFormatter staticLabelsFormatter1 = new StaticLabelsFormatter(graph1);
        staticLabelsFormatter1.setHorizontalLabels(new String[] {"","","",""});
        graph1.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter1);



        graph2 = (GraphView) findViewById(R.id.graph2);
        graph2.setTitle("Y Axis");
        graph2.addSeries(mSeries2);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph2);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"","","",""});
        graph2.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graph3 = (GraphView) findViewById(R.id.graph3);
        graph3.setTitle("Z Axis");
        graph3.addSeries(mSeries3);
        StaticLabelsFormatter staticLabelsFormatter3 = new StaticLabelsFormatter(graph3);
        staticLabelsFormatter3.setHorizontalLabels(new String[] {"","","",""});
        graph3.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter3);

        graph1.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graph1.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph2.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graph2.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph3.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graph3.getGridLabelRenderer().setGridColor(Color.WHITE);





        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ++numClicks;
                if (numClicks % 2 == 1){
                    fab.setImageResource(R.drawable.ic_stop);
                    Snackbar.make(view, "Test has begun.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mSensorManager.registerListener(TestActivity.this, mAccelerometer,
                            SensorManager.SENSOR_DELAY_GAME);
                } else {
                    fab.setImageResource(R.drawable.ic_play);
                    Snackbar.make(view, "Test has finished.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    stop();
                    setupOkhttp();

                }
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

                //update graphs
                if (Math.abs(x) > 40){
                    mSeries1.appendData(new DataPoint(curTime,50), false, 40);
                } else {
                    mSeries1.appendData(new DataPoint(curTime,x), false, 40);
                }
                if (Math.abs(y) > 40){
                    mSeries2.appendData(new DataPoint(curTime,50),false, 40);
                } else{
                    mSeries2.appendData(new DataPoint(curTime,y),false, 40);
                }
                if (Math.abs(z) > 40){
                    mSeries3.appendData(new DataPoint(curTime,50),false, 40);
                } else{
                    mSeries3.appendData(new DataPoint(curTime,z),false, 40);
                }

            }
        }
    }

    public void stop(){
        mSensorManager.unregisterListener(this,mAccelerometer);
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
                .url("http://ec2-35-162-139-63.us-west-2.compute.amazonaws.com/api/v1.0/predict")
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
