package fr.toh.bike_tour_coordsgps;

// Help to the implementation of the methods in the other classes
class Constants {

    static final int LOCATION_SERVICE_ID = 175;

    //start / Stop the position detection
    static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationSservice";

    // The key of the user
    // The same for the accelerometer part and the location one
    //id_key: is entered by the user when logs in
    static String KEY_LOGIN = StartActivity.id_key;
}
