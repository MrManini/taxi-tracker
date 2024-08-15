//Package is used to declare the folder of the main activity inside the project
package com.example.sendmygeo;
//Necessary imports to android apps development
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//Imports related to google mobile services implementation (user to obtain position through GPS)
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
//Imports related to date and time conversion
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //Default numbers to specify permissions request, each permission has it's own integer code
    private static final int PERMISSION_REQUEST_SEND_SMS = 123;
    private static final int PERMISSION_REQUEST_LOCATION = 124;
    //expressions to cast variables in its corresponding type
    private EditText phoneNumberEditText;
    private Button updateAndSendButton;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView altitudeTextView;
    private TextView timeTextView;
    //Google service (public class) to access current location
    private FusedLocationProviderClient fusedLocationClient;
    private String locationMessage;
    //Callback and request
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Default lines to create the main page and specify the corresponding xml
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Obtain front-end elements using id
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        updateAndSendButton = findViewById(R.id.updateAndSendButton);
        latitudeTextView = findViewById(R.id.latitudeTextView);
        longitudeTextView = findViewById(R.id.longitudeTextView);
        altitudeTextView = findViewById(R.id.altitudeTextView);
        timeTextView = findViewById(R.id.timeTextView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();

        //Function to define procedure when the user presses the update and send button
        updateAndSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocationAndSend();
            }
        });

        //Starts from here the location services
        startLocationUpdates();
    }

    private void createLocationRequest() {
        //It is specified to ask for a location request every 10 seconds to update it or even every 5 seconds if the user asks to
        //Also, High accuracy is specified to obtain better results, there are other modes as battery saving but for now, precision ir prioritized
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // 10 segundos
        locationRequest.setFastestInterval(500); // 5 segundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //low power reduces accuracy, as less battery power is invested in calculating the exact position
    }

    private void createLocationCallback() {
        //Method exportes from documentation to create a callback and define current location as the las location obtained from all
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastLocation = location;
                }
            }
        };
    }

    private void startLocationUpdates() {
        //Method called instantly in the activity creation method
        //Check if permission is granted, if not, asks for it using the location request code
        //Package manager is a class used to retrieve information about packages installed or permissions used
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            //use the written method, as argument it takes the request, the callback and a looper
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateLocationAndSend() {
        if (lastLocation != null) {
            updateLocationUI();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        PERMISSION_REQUEST_SEND_SMS);
            } else {
                sendSMS();
            }
        } else {
            Toast.makeText(MainActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocationUI() {
        //Using Locale.getDefault to display numbers according to the region
        //from location specify the desired value, whether is latitude, altitude, etc
        String latitude = String.format(Locale.getDefault(), "%.6f", lastLocation.getLatitude());
        String longitude = String.format(Locale.getDefault(), "%.6f", lastLocation.getLongitude());
        String altitude = String.format(Locale.getDefault(), "%.2f", lastLocation.getAltitude());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(lastLocation.getTime()));
        //Update objects attributes to display latest known position
        latitudeTextView.setText("Latitud: " + latitude);
        longitudeTextView.setText("Longitud: " + longitude);
        altitudeTextView.setText("Altitud: " + altitude + " metros");
        timeTextView.setText("Tiempo: " + formattedTime);
        //Formatted and concatenated message to be sent through SMS
        locationMessage = "La latitud es " + latitude + ". La longitud es " + longitude +
                ". La altitud es " + altitude + " metros. El tiempo fue " + formattedTime + ".";
    }

    private void sendSMS() {
        //Phone number is converted to a trimmed string to avoid blank spaces
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        //If there is, indeed, a phone number and a location, try sending the message, else print an error message
        //asking for the phone number and location
        if (!phoneNumber.isEmpty() && locationMessage != null) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                //scAdress refers to the service center to be used, null means default
                //sentIntent and deliveryIntent are used to send alerts in case the message is sent correctly
                smsManager.sendTextMessage(phoneNumber, null, locationMessage, null, null);
                Toast.makeText(this, "SMS enviado exitosamente", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //if an errors occurs of any type, print an alert and the error trace: line and cause
                Toast.makeText(this, "Error al enviar SMS", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Por favor, ingrese un número y obtenga los datos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //CallBack executed everytime requestPermissions is calles (in upper methods, it is used to request permissions)
        //  grantResults contains the status or response code
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS();
            } else {
                Toast.makeText(this, "Permiso denegado para enviar SMS", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permiso denegado para acceder a la ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // onPause is used to stop location services when the app isn't actively used
    //Override to use the onPause function defined by the user and not by the superclass: AppCompatActivity
    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    //onResume used to restart system operation
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }
}