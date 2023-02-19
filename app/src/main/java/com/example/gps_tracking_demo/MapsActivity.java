package com.example.gps_tracking_demo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private DatabaseReference reference;
    private LocationManager manager;
    private final int MIN_TIME=1000;// 1 sec
    private final int MIN_DISTANCE=1;// 1 meter
    List<Location> savedLocations;
    Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        manager=(LocationManager) getSystemService(LOCATION_SERVICE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference= FirebaseDatabase.getInstance().getReference().child("UsersInfo").child(uid);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MyApplication myApplication=(MyApplication) getApplicationContext();
        savedLocations=myApplication.getMylocations();

        getLocationUpdates();

        readchanges();

    }

    private void readchanges(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    try{
                        Location location = snapshot.getValue(Location.class);
                        if (location!=null) {
                            myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
                        }
                    }catch (Exception e){
                       // Toast.makeText(MapsActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                        int number = MapsActivity.j;
                        if(number>0) {
                            Toast.makeText(MapsActivity.this, "You may get infected =( We recommend taking a PCR test.", Toast.LENGTH_LONG).show();

                        }else{
                            Toast.makeText(MapsActivity.this, "No coincidences with infected users locations detected. You are save =)", Toast.LENGTH_LONG).show();
                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLocationUpdates(){
        if(manager!=null){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DISTANCE,this);
                }else if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DISTANCE,this);
                }else{
                    Toast.makeText(this, "No Provider Enabled", Toast.LENGTH_SHORT).show();
                }


            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocationUpdates();
            }else{
                Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        for(Location location: savedLocations){

            LatLng latLng= new LatLng(location.getLatitude(), location.getLongitude());
            long days = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - new Date().getTime());
            if (days > 14) continue;

            String daysAgo = days + " days ago";
            MarkerOptions markerOptions=new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(daysAgo + "");
            myMarker=mMap.addMarker(markerOptions);


        }


    }

    @Override
    public void onLocationChanged(Location location){
        if(location!=null){
            saveLocation(location);
        }else{
            Toast.makeText(this, "No location", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveLocation(Location location){
        reference.setValue(location);
    }


    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users");
    private ValueEventListener listener;
    public static final String TAG = "YOUR-TAG-NAME";
    final DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference().child("UserUID");
    int z = 0;
    public static int j = 0;
    public MapsActivity(){
        ArrayList<String> arraylist = new ArrayList<String>();
        ArrayList<String> arraylist2 = MainActivity.arraylist2;
        ArrayList<String> arraylist3 = new ArrayList<String>();
        for(int i=0;i<20;i++) {
            arraylist.add(String.valueOf(i));
        }
        for(int i=0;i<20;i++) {
            arraylist3.add(String.valueOf(i));
        }

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot: snapshot.getChildren()){
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};

                    for(int i=0;i<20;i++){
                        String value = (String) snapshot.child(uid).child("timestep"+i).getValue();
                        arraylist.set(i, value);
                    }

                    for (int i=0; i<arraylist2.size(); i++){

                        String valueofAL2 = arraylist2.get(i);

                        if (!uid.equals(valueofAL2)){
                            String statusCheck = (String) snapshot.child(arraylist2.get(i)).child("Status").getValue();
                            if (statusCheck!=null){
                            if (statusCheck.equals("Infected")) {
                                for(int k=0;k<20;k++){
                                    String value3 = (String) snapshot.child(arraylist2.get(i)).child("timestep"+k).getValue();
                                    arraylist3.set(k, value3);

                                }
                                for (int m=0; m<20; m++){
                                    String myString = arraylist.get(m);

                                    for(int n=0; n<20; n++){
                                        String otherString = arraylist3.get(n);

                                        if (myString.equals(otherString)) {
                                            j++;
                                           // mRef.child(uid).child("whoEQL"+i).setValue(arraylist2.get(i));
                                        }
                                        else{
                                            z++;
                                        }
                                    }
                                }
                                if(j>0){
                                    mRef.child(uid).child("notification").setValue("You may get infected. We recommend taking a PCR test.");

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this, "My notification");

                                    builder.setContentTitle("GPS Tracking App");

                                    builder.setContentText("You may get infected. We recommend taking a PCR test.");

                                    builder.setSmallIcon(R.drawable.ic_virus);

                                    builder.setAutoCancel(true);

                                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MapsActivity.this);

                                    managerCompat.notify(1,builder.build());

                                }
                                else {
                                    mRef.child(uid).child("notification").setValue("No coincidences with infected users locations detected. You are save =)");
                                }
                            }}
                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        };
        mRef.addValueEventListener(listener);

    }
}
