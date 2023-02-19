package com.example.gps_tracking_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    public static final int PERMISSIONS_FINE_LOCATION = 99;
    String x;
    String y;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_sensor, tv_updates, tv_address, tv_wayPointCounts;
    Button firebaseBtn, firebase1Btn, map, help, btn_map;
    Time time_new;
    String timestep;
    DatabaseReference reference;
    int mCounter = 0;

    Switch sw_locationupdates, sw_gps;
    String currentData = "";
    //button initialization
    Button button;
    Button nStartButton;

    public  static ArrayList<String> arraylist2 = new ArrayList<String>();
    ArrayList<ArrayList<String>> arraylist3 = new ArrayList<>();
    /*Map<String, List> mapOfLists = new HashMap<String, List>();
    ArrayList<String> listOfStrings = new ArrayList<String>();
    ArrayList<Integer> listOfIntegers = new ArrayList<Integer>();
    mapOfLists.("integers", listOfIntegers);*/
    // variable to remember if we are tracking
    boolean updateOn = false;

    // current location
    Location currentLocation;
    // list of saved location
    List<Location> savedLocations;



    // Location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;


    LocationCallback locationCallBack;


    // Google's API location service
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button OnCreate
        button = findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                startActivity(intent);


            }
        });
        nStartButton = findViewById(R.id.startButton);
        nStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentQuiz = new Intent(MainActivity.this,QuizActivity.class);
                startActivity(intentQuiz);
            }
        });




        //app return to home screen


        //give each UI variable a value

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        map = findViewById(R.id.map);
        firebaseBtn = findViewById(R.id.firebase_test);
        firebase1Btn = findViewById(R.id.firebase_test_1);
        help = findViewById(R.id.help);
        //btn_map= findViewById(R.id.btn_map);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);



        // set all properties of LocationRequest
        locationRequest = LocationRequest.create();

        locationRequest.setInterval(100 * DEFAULT_UPDATE_INTERVAL);

        locationRequest.setFastestInterval(100 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);

            }
        };



        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    // most accurate - use GPS
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using GPS sensor");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");
                }
            }
        });

        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()) {
                    // turn on location tracking
                    startLocationUpdates();
                } else {
                    // turn off location tracking
                    stopLocationUpdates();
                }
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference myRef1 = database.getReference("UserUID");
        reference = FirebaseDatabase.getInstance().getReference().child("Users");



        for(int i=0;i<20;i++) {
            arraylist2.add(String.valueOf("null"));
        }

        myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (int i=0;i<20;i++){
                    String uidcheck = (String) dataSnapshot.child("UID"+i).getValue();
                    arraylist2.set(i, uidcheck);
                }
                for (int ii=0;ii<20;ii++) {
                    arraylist2.remove(null);
                }
                int length = arraylist2.size();
                int num=0;

                for (int iii=0; iii<length;iii++) {
                    String string;
                    string = arraylist2.get(iii);
                    if(uid.equals(string)){
                        num++;
                    }
                }
                if(num==0) {
                    myRef1.child("UID" + length).setValue(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


        firebaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication myApplication = (MyApplication)getApplicationContext();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                long yourmilliseconds = System.currentTimeMillis();

                Date resultdate = new Date(yourmilliseconds);
                System.out.println(sdf.format(resultdate));
                savedLocations=myApplication.getMylocations();
                savedLocations.add(currentLocation);

                Toast.makeText(getBaseContext(), "Thanks for your support. Your location was saved", Toast.LENGTH_LONG).show();
                //reference.child(uid).setValue(uid);
                reference.child(uid).child("Status").setValue("Infected");
            }
        });

        firebase1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Thanks for your support. Your location was erased", Toast.LENGTH_LONG).show();
                reference.child(uid).child("Status").setValue("Healthy");
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(MainActivity.this, Information.class );
                startActivity(i);
            }
        });



        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
                int num = MapsActivity.j;
                int number = MapsActivity.j;
                
                if(num>0) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My notification");

                    builder.setContentTitle("GPS Tracking App");

                    builder.setContentText("You may get infected. We recommend taking a PCR test.");

                    builder.setSmallIcon(R.drawable.ic_virus);

                    builder.setAutoCancel(true);


                    PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(contentIntent);

                    NotificationManager managerCompat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    managerCompat.notify(0, builder.build());
                }
            }
        });

        updateGPS();
    }// end onCreat method

    public void logout1(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
    }

    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location is NOT being tracked");
        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_address.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS() {
        // get permissions from the user to track GPS
        // get current location from the fused client
        // update the UI -i.e. set all properties in their associated text view items
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            // user provided permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // we got permission. Put values of location. XXX into the UI components
                    updateUIValues(location);
                    currentLocation = location;

                }
            });
        }
        else{
            // permission not provided yet

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    private void updateUIValues(Location location) {
        if (location == null) return;
        DecimalFormat df = new DecimalFormat("#.#");
        // update all of the text view objects with the new location
        tv_lat.setText(df.format(location.getLatitude()));

        x = df.format(location.getLatitude());
        tv_lon.setText(df.format(location.getLongitude()));
        y = df.format(location.getLongitude());
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        timestep=((new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(new Date()))+","+df.format(location.getLatitude())+","+df.format(location.getLongitude()));

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("timestep"+mCounter,timestep);
        reference.child(uid).updateChildren(hashMap);

        if (mCounter<20) {
            mCounter++;
        } else {
            mCounter=0;
        }


        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else{
            tv_altitude.setText("Not available");
        }


        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> address = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(address.get(0).getAddressLine(0)); // could change to town etc.
        }
        catch (Exception e){
            tv_address.setText("Unable to get street address");
        }


        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMylocations();

        // show the number of waypoints saved
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}