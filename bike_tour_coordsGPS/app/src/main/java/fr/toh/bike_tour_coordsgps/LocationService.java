package fr.toh.bike_tour_coordsgps;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationService extends Service {

    // Connect to the API
    private Retrofit retrofit_coord;
    private RetrofitInterface retrofitInterface_coords;
    private String URL_Coords = "https://bicycleinfo0803.herokuapp.com/coordinates/";

    // flag for GPS status
    public boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    double latitude; // latitude
    double longitude; // longitude
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 10000; // 10 secondes

    //To convert the latitude/longitude to an address
    ArrayList<String> addressFragments;
    String strAdresse;
    Geocoder geocoder;
    List<Address> adresses;


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        // onLocationResult() easier to deal with when receiving multiple locations simultaneously
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                // The current location --------------------------
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                latitude =  locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();

                //get the address from latitude and longitude
                try
                {
                    adresses = geocoder.getFromLocation(latitude, longitude, 1);

                }
                catch (IOException ioException)
                {
                    Log.e("GPS", "erreur IOException", ioException);
                } catch (IllegalStateException illegalStateException)
                {
                    Log.e("GPS", "erreur IllegalArgumentExc" , illegalStateException);
                }

                if (adresses == null || adresses.size()  == 0)
                {
                    Log.e("GPS", "erreur aucune adresse !");

                } else {
                    Address adresse = adresses.get(0);
                    addressFragments = new ArrayList<String>();

                    // Final address
                    strAdresse = adresse.getAddressLine(0) + ", " + adresse.getLocality();

                    for(int i = 0; i <= adresse.getMaxAddressLineIndex(); i++)
                    {
                        addressFragments.add(adresse.getAddressLine(i));
                    }
                }
                // At this point the data is collected successfully


                // Add the coords to the API
                // Create a new object from HttpLoggingInterceptor
                // To have a trace of the requests and the responses
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                // Add Interceptor to HttpClient
                // HttpClient to demand the connection
                OkHttpClient client = new OkHttpClient.Builder()
                        .readTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .addInterceptor(interceptor).build();

                // Build a retrofit object
                // Retrofit is used to upload or retreive JSON files
                // Here it is the URL_Coords
                retrofit_coord = new Retrofit.Builder()
                        .baseUrl(URL_Coords)
                        .addConverterFactory(GsonConverterFactory.create()) // Supervise the parsing of teh data that is sent and/or received
                        .client(client) // the OkHttpClient created
                        .build();

                // Create a Retrofit interface with our request
                retrofitInterface_coords = retrofit_coord.create(RetrofitInterface.class);

                // Create a hashmap with all the information we need
                HashMap<String, String> map_coords = new HashMap<>();
                map_coords.put("key", Constants.KEY_LOGIN);
                map_coords.put("lat", String.valueOf(latitude));
                map_coords.put("lon", String.valueOf(longitude));
                map_coords.put("adress", strAdresse);


                // Send the request

                // Execute a call to the API with a request in the shape of Coordinate
                // with the hashmap full with the collected information
                Call<Coordinate> call_coords = retrofitInterface_coords.executeCoords(map_coords);
                call_coords.enqueue(new Callback<Coordinate>() {
                    @Override
                    public void onResponse(Call<Coordinate> call, Response<Coordinate> response) {
                        Log.d("Coords", "onResponse_sent");
                    }
                    @Override
                    public void onFailure(Call<Coordinate> call, Throwable t) {
                        Log.d("Coords", "onFailure");
                        if (t instanceof IOException) {
                            Toast.makeText(getApplicationContext(), " Network Failure ", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "conversion issue! big problems :(", Toast.LENGTH_SHORT).show();
                            // todo log to some central bug tracking service
                        }
                    }
                });
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Start the location service
    private void startLocationservice() {

        // Set up a notification as sson as the location service is activated
        String channelId = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Shape of that notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service ");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running ");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        //  How the location will be collected
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                  channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }

            // LocationRequest objects are used to provide a quality location service
            // New Locationrequest object
            LocationRequest locationRequest = new LocationRequest();
            // the location will be collected every 10 seconds
            locationRequest.setInterval(MIN_TIME_BW_UPDATES); // 10 secondes
            // with high accuracy location
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // fusedLocationProviderClient will make the connection to Play Services in the background,
            // and we’ll start receiving location updates once that connection has been established,
            // and we’ll start receiving callbacks to onLocationResult() once that has all been done
            LocationServices.getFusedLocationProviderClient(this).
                    requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
        }
    }


    //stop the location service
    public void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback); // Removes location updates for this intent
        stopForeground(true); // Remove the notification
        stopSelf();
    }

    // Called every time a client starts the service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationservice();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
