package com.example.location_busdeteceting;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;        //created by a line in gradle module app
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private GoogleMap mMap;
    private LocationManager locationManager;
    Circle circle;
    Marker marker,blindMarker;
    TextToSpeech txtSpeech;
    public static  double BlindLatitude,BlindLongitude;

    LatLng BlindCoor;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    FusedLocationProviderClient mFusedLocationClient;
    LocationListener locationListener;
    private GoogleApiClient googleApiClient;
    private boolean mLocationPermissionsGranted = false;
    private Timer timer;
    private Circle BlindCircle;      //circle added when the user clicked on the map
    private Circle FixedZone;       //predefined circle
    Vibrator vibrator;              //a vibrator to vibrate used in onLongClick

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);//////get ready for the map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // drawingCercle(31.5952218,-8.027767);        //coordinates that you need
        //31.6439666 , -8.0203603 for biblio fstg
        //drawingCercle(31.5955014,-8.0309493);         //HOME'S COORDINATES
        drawingCercle(31.6444266,-8.0202964);
        //  drawingCercle(31.6435675,-8.020642);
      /*  bldLoc =new BlindLocation(this);
        Log.i("bldLoc object",bldLoc.toString());
        Toast.makeText(MapsActivity.this, "erreur"+BlindLongitude+" and "+BlindLatitude+" hhhh", Toast.LENGTH_SHORT).show();*/
        /*String address=getAddress(this,31.5952218,-8.027767);
        Log.i("address is ",address);
        Toast.makeText(MapsActivity.this, "address est "+address, Toast.LENGTH_SHORT).show();*/
        initializeTextToSpeech();

        /////to get coordinates every 10secs
        final Handler handler = new Handler();
        timer = new Timer();
        TimerTask doTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            fetchLocation();      //or this fct :getDeviceLocation();
                        } catch (Exception e) {
                            Toast.makeText(MapsActivity.this, "erreur", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        timer.schedule(doTask, 0, 10000);


        /////when we click on the map we create a zone with current coordinates user
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                Toast.makeText(MapsActivity.this, "you clicked on the map", Toast.LENGTH_SHORT).show();
                drawingCercle(BlindCoor.latitude,BlindCoor.longitude);

                ////vibrating while long click on the map
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    vibrator.vibrate(300);
                }
                speake("you have just created a new zone");
                String address = getAddress(BlindCoor.latitude, BlindCoor.longitude);
                Log.i("address is ", address);
                speake(address);
                Toast.makeText(MapsActivity.this, "address est " + address, Toast.LENGTH_SHORT).show();
            }
        });
    }



    //fct to draw a circle on the map
    private void drawingCercle(double lat,double lon){
        // Add a marker in latLng precised and move the camera
        LatLng place = new LatLng(lat,lon);   /////lag and lat
        marker = mMap.addMarker(new MarkerOptions().position(place).title("our zone"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 18));     //zoomed map
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(place)               //just what position
                .zoom(13)                   //that camera should show
                .bearing(90)                  //with zoom resolution
                .build();
        circle = mMap.addCircle(new CircleOptions()              //circle created arround home variable
                .center(place)
                .radius(10)     ///this in meters   10 meters arround home
                .fillColor(Color.BLUE)
                .strokeColor(Color.GREEN));
    }



    ///fct to add a marker in the map
    private void addBlindMarker(double lat,double lon)
    {
        LatLng place = new LatLng(lat,lon);   /////lag and lat
        blindMarker = mMap.addMarker(new MarkerOptions().position(place)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title("you are here"));
    }



    /**
     * Check if a circle contains a point
     *
     * @param circle
     * @param point
     */
    private boolean isCircleContains(Circle circle, LatLng point) {
        double r = circle.getRadius();
        LatLng center = circle.getCenter();
        Log.i("msg hiiiiiiiiiiiiiiiii", String.valueOf(point));
        double cX = center.latitude;
        double cY = center.longitude;
        double pX = point.latitude;
        double pY = point.longitude;

        float[] results = new float[1];

        Location.distanceBetween(cX, cY, pX, pY, results);

        if (results[0] < r) {
            return true;
        } else {
            return false;
        }
    }



    ////////////////////////////////////first fct to get user location /////////////////////////////////////////////
    private void fetchLocation() {


        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new AlertDialog.Builder(this)
                        .setTitle("Required Location Permission")
                        .setMessage("You have to give this permission to acess this feature")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Log.i("msg hh", String.valueOf(mFusedLocationClient));
            if (isLocationEnabled())
            {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                //if (location != null) {
                                // Logic to handle location object
                                double latittude = location.getLatitude();
                                double longitude = location.getLongitude();
                                BlindCoor=new LatLng(latittude,longitude);
                                Toast.makeText(getBaseContext(), "coordinates "+BlindCoor.latitude+" and "+BlindCoor.longitude, Toast.LENGTH_LONG).show();


                                String address=getAddress(31.5952218,-8.027767);
                                Log.i("address is ",address);

                                //the fct that add a marker to our map///////////////////////
                                addBlindMarker(BlindCoor.latitude,BlindCoor.longitude);



                                //////the fct that check ig the blind person is in the zone or not
                                Boolean msg = isCircleContains(circle,BlindCoor);   //BlindCoor
                                if (msg) {
                                    Toast.makeText(getBaseContext(), "Blind person is Inside", Toast.LENGTH_LONG).show();
                                    speake("you are inside the zone");
                                } else {
                                    Toast.makeText(getBaseContext(), "Blind person is Outside", Toast.LENGTH_LONG).show();
                                    speake("you are not closer to our zone ");
                                    //Toast.makeText(getBaseContext(), "b2", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
            }
            else {
                speake("Turn on location first");
                Toast.makeText(this, "جهاز turn ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }

    }


    //initialise txtToSpeech to a language
    private void initializeTextToSpeech() {
        txtSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (txtSpeech.getEngines().size() == 0) {
                    Toast.makeText(MapsActivity.this, "no tts", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    txtSpeech.setLanguage(Locale.US);      //you can choose any language
                    speake("Welcome I am ready");
                }

            }
        });
    }

    /////speake fct to speake a msg
    private void speake(String message) {
        if (Build.VERSION.SDK_INT > 21) {
            txtSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            txtSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);

        }
    }



    /////this the second fct that also give the current coordinates//////////////////////
    private LatLng getDeviceLocation() {
        Log.d("hello 1", "getDeviceLocation: getting the devices current location");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            final Task location = mFusedLocationClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d("hello 2", "onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();
                        Toast.makeText(MapsActivity.this, "coordinates are " + currentLocation.getLatitude() + " and " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                        Log.i("coordinates", "coordinates are " + currentLocation.getLatitude() + " and " + currentLocation.getLongitude());

                        BlindCoor=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());




                        //the fct that add a marker to our map///////////////////////
                        addBlindMarker(BlindCoor.latitude,BlindCoor.longitude);




                        Boolean msg = isCircleContains(circle,BlindCoor);   //BlindCoor
                        if (msg) {
                            Toast.makeText(getBaseContext(), "Blind person is Inside", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getBaseContext(), "Blind person is Outside", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("hello 3", "onComplete: current location is null");
                        Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (SecurityException e) {
            Log.e("hello 4", "getDeviceLocation: SecurityException: " + e.getMessage());
        }


        return BlindCoor;
    }


    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
           /* add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();*/
            //speake(add);
            return add;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    ////GPS system is enable ///////////////////////////////
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }


}