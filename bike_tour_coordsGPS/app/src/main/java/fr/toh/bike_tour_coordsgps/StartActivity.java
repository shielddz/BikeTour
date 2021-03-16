package fr.toh.bike_tour_coordsgps;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.hardware.SensorManager.SENSOR_DELAY_UI;

public class StartActivity extends AppCompatActivity implements SensorEventListener {
    // Class to collect and send the accelerometer data

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    int DELAY = 500000; // = 0.5 sec = 500ms
    boolean mustReadSensor;

    private SensorManager sensorManager;
    Sensor accelerometer;
    String X, Y, Z;

    // Connect to the API
    private Retrofit retrofit_acc;
    private RetrofitInterface retrofitInterface_acc;
    private String URL_Acc = "https://bicycleinfo0803.herokuapp.com/accelerometers/";

    static String id_key; // User's ID
    Intent intent ;

    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(getApplicationContext(), LocationService.class);

        // SensorManager to access to all the device's sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Specify the sensor needed = Accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // To get the user's Id
        Bundle bundle = getIntent().getExtras();
        id_key = bundle.getString("key");

        // Set actions when the button Start is clicked
        findViewById(R.id.buttonStartLocation).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                // Permissions requirements
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            StartActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );

                } else { // Start location and sensor
                    // Set a timer to when the phone collects a lot of data, so he needs an interval to adjust
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            mustReadSensor = true;
                        }
                    }, 0, 500);

                    // Start location Service
                    startLocationService();

                    //Start the sensor
                    sensorManager.registerListener((SensorEventListener) StartActivity.this
                            , accelerometer, DELAY);
                }
            }
        });

        // Set actions when the button Stop is clicked
        findViewById(R.id.buttonStopLocation).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //Stop location service
                stopLocationService();

                // Stop the sensor
                sensorManager.unregisterListener(StartActivity.this, accelerometer);
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Where the accelerometer's data is collected

        //Check if the sensor is available
        if (!mustReadSensor) {
            return;
        }
        mustReadSensor = false;

        // Get the values collected
        X = String.valueOf(event.values[0]);
        Y = String.valueOf(event.values[1]);
        Z = String.valueOf(event.values[2]);


        //Add the X Y Z to the API

        // Add the accelerometer's coords to the API
        // Create a new object from HttpLoggingInterceptor
        // To have a trace of the requests and the responses
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add Interceptor to HttpClient
        // HttpClient to send a request
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build();

        // Build a retrofit object
        retrofit_acc = new Retrofit.Builder()
                .baseUrl(URL_Acc)
                .addConverterFactory(GsonConverterFactory.create()) // Supervise the parsing of teh data that is sent and/or received
                .client(client) // the OkHttpClient created
                .build();

        // Create a Retrofit interface with our request
        retrofitInterface_acc = retrofit_acc.create(RetrofitInterface.class);

        // Create a hashmap with all the information we need
        HashMap<String, String> map_acc = new HashMap<>();
        map_acc.put("key", id_key);
        map_acc.put("X", String.valueOf(X));
        map_acc.put("Y", String.valueOf(Y));
        map_acc.put("Z", String.valueOf(Z));

        // Send the request

        // Execute a call to the API with a request in the shape of Accelerometer_sensor
        // with the hashmap full with the collected information
        Call<Accelerometer_sensor> call = retrofitInterface_acc.executeAcc(map_acc);
        call.enqueue(new Callback<Accelerometer_sensor>() {
            // If it succesfully connected to the server
            @Override
            public void onResponse(Call<Accelerometer_sensor> call, Response<Accelerometer_sensor> response) {
                Log.d("GPS", "onResponse_sent");
            }

            // If the connection to the server failed
            @Override
            public void onFailure(Call<Accelerometer_sensor> call, Throwable t) {
                Log.d("GPS", "onFailure");
                if (t instanceof IOException) {
                    Toast.makeText(getApplicationContext(), "this is an actual network failure :( inform the user and possibly retry", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "conversion issue! big problems :(", Toast.LENGTH_SHORT).show();
                    // todo log to some central bug tracking service
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Start the location service from this intent
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startLocationService() {
     //   if (isLocationServiceRunning()) {
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startForegroundService(intent);
            Toast.makeText(this, "Location service started ", Toast.LENGTH_SHORT).show();
        //}
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopLocationService() {
     //   if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            //startService(intent);
            this.getApplicationContext().startForegroundService(intent);
            Toast.makeText(this, "Location service stopped ", Toast.LENGTH_SHORT).show();

        //}
    }

}
