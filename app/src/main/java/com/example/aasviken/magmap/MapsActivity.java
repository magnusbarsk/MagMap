package com.example.aasviken.magmap;

import android.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import static com.example.aasviken.magmap.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 888;
    private GoogleMap mMap;
    private String frafield;
    private String tilfield;
    double fromlatitude;
    double tolatitude;
    double fromlongitude;
    double tolongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        final EditText fra = (EditText) findViewById(R.id.fra);
        final EditText til = (EditText) findViewById(R.id.til);
        final Button sokeknapp = (Button) findViewById(R.id.searchbutton);
        final Context context = MapsActivity.this;
        final Geocoder geocoder = new Geocoder(this);
        final TextView avstandd = (TextView)findViewById(R.id.avstand);

        sokeknapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fra.getText().toString().equals("")|| til.getText().toString().equals("")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("Lukk", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.setMessage("Feltet kan ikke være tomt");
                    dialog.create().show();
                }
                else {
                    try {
                        fromlatitude = geocoder.getFromLocationName(fra.getText().toString(), 1).get(0).getLatitude();
                        tolatitude = geocoder.getFromLocationName(til.getText().toString(), 1).get(0).getLatitude();
                        fromlongitude = geocoder.getFromLocationName(fra.getText().toString(), 1).get(0).getLongitude();
                        tolongitude = geocoder.getFromLocationName(til.getText().toString(), 1).get(0).getLongitude();

                        System.out.println(tolatitude + " " + tolongitude);
                        System.out.println(fromlatitude+ " "+ fromlongitude);
                    } catch (Exception e) {
                        System.out.println("qwdawdawdawdaaaaaaaaaaaaaaaaaaaaaaaaa");
                    }
                    LatLng fra = new LatLng(fromlatitude, fromlongitude);
                    LatLng til = new LatLng(tolatitude, tolongitude);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(fra).title("Din posisjon"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(fra));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fra, 7));
                    mMap.addMarker(new MarkerOptions().position(til).title("Din destinasjon"));


                    GoogleDirection.withServerKey("AIzaSyDgMuCPcbDVRdkvY6_W9VNkij4RDRZPYOE")
                            .from(new LatLng(fromlatitude, fromlongitude))
                            .to(new LatLng(tolatitude, tolongitude))
                            .avoid(AvoidType.HIGHWAYS)
                            .transportMode(TransportMode.DRIVING)
                            .execute(new DirectionCallback() {
                                @Override
                                public void onDirectionSuccess(Direction direction, String rawBody) {
                                    System.out.println("''''''''''''''''''''''''''''''''''");
                                    if (direction.isOK()) {
                                        System.out.println("********************************************************************");

                                        Route route = direction.getRouteList().get(0);
                                        Leg leg = route.getLegList().get(0);
                                        String avstand = String.valueOf(leg.getDistance().getText());
                                        avstandd.setText(avstand);
                                        List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();


                                        ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getApplicationContext(), stepList, 5, Color.YELLOW, 3, Color.BLACK);
                                        PolylineOptions pol = new PolylineOptions();
                                        pol.color(Color.parseColor("#0000FF"));
                                        for (int i=0;i<stepList.size();i++) {
                                            Step step = stepList.get(i);
                                            double startLat = step.getStartLocation().getLatitude();
                                            double startLng = step.getStartLocation().getLongitude();
                                            pol.add(new LatLng(startLat,startLng));

                                            double sluttLat = step.getEndLocation().getLatitude();
                                            double sluttLng = step.getEndLocation().getLongitude();

                                            pol.add(new LatLng(sluttLat, sluttLng));

                                        }
                                        mMap.addPolyline(pol);


                                    }

                                }
                                @Override
                                public void onDirectionFailure (Throwable t) {
                                    System.out.println("DET SKJEDD FEILAA");
                                }
                            });

                }
            }
        });
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
    public void onMapReady(GoogleMap map) {
        mMap = map;


        //--------------------------------------------------------------------------------------------------

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
        else {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

            android.location.Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double latitude = 0.0;
            double longitude = 0.0;
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                LatLng kords = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(kords).title("You"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(kords));
            }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }



    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}