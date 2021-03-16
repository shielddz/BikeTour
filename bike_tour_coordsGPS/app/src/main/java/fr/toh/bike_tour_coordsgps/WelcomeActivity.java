package fr.toh.bike_tour_coordsgps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeActivity extends AppCompatActivity {

    public String id;
    EditText id_edt, firstname_edt, lastname_edt;
    Button login_btn;

    String firstname, lastname;
    Intent intent_sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // get the fields where the user can write
        id_edt = (EditText) findViewById(R.id.id_edt);
        login_btn = (Button) findViewById(R.id.login_btn);



        firstname_edt = (EditText) findViewById(R.id.firstname_edt);
        lastname_edt = (EditText) findViewById(R.id.lastname_edt);
        firstname = firstname_edt.getText().toString();
        lastname = lastname_edt.getText().toString();

        // Initialize the intent with the key entered by the user
        intent_sensor = new Intent(WelcomeActivity.this, StartActivity.class);

        // Set actions when the button "Login" is clicked
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the key = id
                id = id_edt.getText().toString();

                // put it in the intent to be transmitted when activity changes
                intent_sensor.putExtra("key", id);

                // Lunch the ne intent
                startActivity(intent_sensor);
            }
        });

    }
}
