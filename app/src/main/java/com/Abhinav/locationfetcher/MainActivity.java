package com.Abhinav.locationfetcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView latitude, longitude, address;
    private MaterialButton getLastLocationBtn, retrieveLocationBtn;
    private final static int REQUEST_CODE = 100;


    private FusedLocationProviderClient fusedLocationClient;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
            }
        });

        retrieveLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveLocationFromDatabase();
            }
        });

    }


    private void getLastLocation() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null){
                                Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                     addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude() , 1);

                                    Log.e("Latitude : " , String.valueOf(location.getLatitude()));
                                    Log.e("Longitude : " , String.valueOf(location.getLongitude()));

                                    latitude.setText("Latitude : " + addresses.get(0).getLatitude());
                                    longitude.setText("Longitude : " + addresses.get(0).getLongitude());
                                    address.setText("Address : " + addresses.get(0).getAddressLine(0));

                                    locationHelper = new LocationHelper(String.valueOf(location.getLatitude()),
                                            String.valueOf(location.getLongitude()),
                                            addresses.get(0).getAddressLine(0));

                                    FirebaseDatabase.getInstance().getReference("Current Location")
                                            .setValue(locationHelper).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(MainActivity.this, "Location Saved on Database", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Toast.makeText(MainActivity.this, "Error occured while storing location", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
        else{
            askLocationPermission();
        }
    }

    private void retrieveLocationFromDatabase() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Current Location");

        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String latitudeDB = snapshot.child("latitude").getValue(String.class);
                String longitudeDB = snapshot.child("longitude").getValue(String.class);
                String addressDB = snapshot.child("address").getValue(String.class);

                latitude.setText("Latitude : " + latitudeDB);
                longitude.setText("LONGITUDE : " + longitudeDB);
                address.setText("ADDRESS : " + addressDB);

                Toast.makeText(MainActivity.this, "Location Retrieved from Database Successully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void askLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void init(){
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        address = findViewById(R.id.address);
        getLastLocationBtn = findViewById(R.id.getLastLocationBtn);
        retrieveLocationBtn = findViewById(R.id.retrieveLastLocationBtn);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
        }
        else{
            Toast.makeText(this, "Location Permission required to access location", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}