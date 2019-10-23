package ca.kevinjarocki.mapseventrecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.List;

import ca.kevinjarocki.mapseventrecorder.Adapters.MarkerRecyclerAdapter;
import io.opencensus.resource.Resource;

public class MapFragment extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener{
    // Tag for Logcat logs.
    private static final String TAG = "MapFragment";

    // Fragment Views
    private RecyclerView RecycleView;
    private GoogleMap GMap;

    // Google Map API Properties
    private static final boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient googleApiClient;

    // LatLng Data for different google maps camera functions
    LatLng mCurrentLocation;
    LatLng mCameraPosition;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private LatLngBounds MapDimensions;
    // Other Values
    private LatLng loc; // Default location

    //Lists and AbstractDataTypes
    private MarkerRecyclerAdapter MarkerRecyclerAdapter;
    private ArrayList<MarkerOptions> CMarkers = new ArrayList<MarkerOptions>();

    // Alert Box Values for creating a new marker.
    private AlertDialog.Builder builder;
    private MarkerOptions mark;
    private EditText commentInput;
    private LatLng position;
    private LayoutInflater inflater;
    //Strings and values for a new Marker
    private String Scomment;
    private int TotalMarkers;
    // Shared Preferences & Bundles
    private String Map_View_Bundle_Key = "MAPVIEW_BUNDLE_KEY";
    private String New_Marker_Name = "NEW_MARKER_NAME";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Bundle mapViewBundle;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.fragment_marker_list);
        Bundle bundle = null;
        if (savedInstanceState != null){
            bundle = savedInstanceState.getBundle(Map_View_Bundle_Key);
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);
        // Checks if ACCESS_FINE_LOCATION permission is granted
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            RecycleView = findViewById(R.id.marker_list_recycler_view);
            CreateMarkerListRecyclerView();
        }
        // Initialize the Google API Client
        googleApiClient = new GoogleApiClient.Builder
                (this, this, this)
                .addApi(LocationServices.API).build();
        // Initialize the fusedLocationClient to get current loc
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Initialize Layout Inflator for the New Marker AlertBox
        inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

/*    public void LogMarkersDatePersistance(LatLng position, String name , int image ){
        TotalMarkers++;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lat" + Integer.toString((TotalMarkers -1)), Double.toString(position.latitude));
        editor.putString("lng" + Integer.toString((TotalMarkers -1)), Double.toString(position.longitude));
        editor.putString("title" + Integer.toString((TotalMarkers-1)), name);
        //editor.putInt("icon" + Integer.toString((TotalMarkers-1)), BitmapDescriptorFactory.fromResource(I));
    }*/

    public Dialog CreateDialog(final LatLng pos) {
        builder = new AlertDialog.Builder(MapFragment.this);
        builder.setTitle(R.string.new_marker);
        commentInput = new EditText(MapFragment.this);
        builder.setView(commentInput).setPositiveButton("Next Destination", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                position = new LatLng(0,0);
                position = pos;
                Scomment = "";
                Scomment = commentInput.getText().toString();
                mark = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_explore_black_24)).position(pos).title(Scomment).snippet(Scomment);
                CMarkers.add(mark);
                GMap.addMarker(mark);
                Toast.makeText(getApplicationContext(), "Adding New Marker at (Lat,Long): " + position.latitude + "," + position.longitude, Toast.LENGTH_SHORT);
                GMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                GMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,14));
            }
        }).setNeutralButton("I've Been here!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                position = new LatLng(0,0);
                position = pos;
                Scomment = "";
                Scomment = commentInput.getText().toString();
                mark = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_explore_black_24)).position(pos).title(Scomment).snippet(Scomment);
                CMarkers.add(mark);
                GMap.addMarker(mark);
                Toast.makeText(getApplicationContext(), "Adding New Marker at (Lat,Long): " + position.latitude + "," + position.longitude, Toast.LENGTH_SHORT);
                GMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                GMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,14));
            }
        }).setPositiveButton("I want to go here!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                position = new LatLng(0,0);
                position = pos;
                Scomment = "";
                Scomment = commentInput.getText().toString();
                mark = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_explore_off_black_24)).position(pos).title(Scomment).snippet(Scomment);
                CMarkers.add(mark);
                GMap.addMarker(mark);
                Toast.makeText(getApplicationContext(), "Adding New Marker at (Lat,Long): " + position.latitude + "," + position.longitude, Toast.LENGTH_SHORT);
                GMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                GMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,14));
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "New Marker Creation Cancelled.", Toast.LENGTH_SHORT);
                        dialogInterface.cancel();
                    }
                }).show();
        return builder.create();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();}
    }
    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location lastLocation) {
                        // Got last known location. In some rare situations this can be null.
                        if (lastLocation != null) {
                            // Logic to handle location object
                            double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
                            loc = new LatLng(lat, lon);
                            Log.i(TAG, loc.toString());
                            GMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                            // animate camera allows zoom
                            GMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
                        }
                    }
                });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        GMap = googleMap;

        GMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "Google Map click at Long: " + latLng.longitude + " and Lat:" + latLng.latitude);
                CreateDialog(latLng);
            }
        });
        //addMarkers();
    }

    private void CreateMarkerListRecyclerView() {
        MarkerRecyclerAdapter = new MarkerRecyclerAdapter(getApplicationContext(),CMarkers);
        RecycleView.setAdapter(MarkerRecyclerAdapter);
        RecycleView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapViewBundle = outState.getBundle(Map_View_Bundle_Key);
        if (mapViewBundle == null){
            mapViewBundle = new Bundle();
            outState.putBundle(Map_View_Bundle_Key,mapViewBundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,
                "Connection suspended to Google Play Services!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences(Map_View_Bundle_Key, Context.MODE_PRIVATE);

    }

    public void onAboutClick(View view){
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        startActivity(aboutIntent);
    }
    public void onResetClick(View view){
    GMap.clear();
    CMarkers.clear();
    MarkerRecyclerAdapter.ResetMarkers();
    this.CreateMarkerListRecyclerView();
    }

    @Override
    protected void onPause() {
        sharedPreferences = getSharedPreferences(Map_View_Bundle_Key, Context.MODE_PRIVATE);

        super.onPause();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG,
                "Can't connect to Google Play Services!");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


}
