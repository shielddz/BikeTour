package fr.toh.bike_tour_coordsgps;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {
    // To implement the fields of the server

    @POST("/coordinates")
    Call<Coordinate> executeCoords(@Body HashMap<String, String> map);

    @POST("/accelerometers")
    Call<Accelerometer_sensor> executeAcc(@Body HashMap<String, String> map);

    @POST("/users")
    Call<User> executeUser(@Body HashMap<String, String> map);

}
