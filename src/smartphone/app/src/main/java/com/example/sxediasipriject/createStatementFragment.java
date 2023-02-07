package com.example.sxediasipriject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class createStatementFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private static final String TAG = "HomeActivity";
    EditText name, power;
    Spinner type;
    Button createStatementButton;
    ImageButton pdfButton;
    ProgressBar progressBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float DEFAULT_ZOOM = 10f;
    private GoogleMap mMap;
    private Location currentLocation;
    private String usersSelection;

    public LatLng usersMarkerLocation;
    private double lati;
    private double loni;

    private Marker startMarker;

    public createStatementFragment() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_statement, container, false);
        mAuth = FirebaseAuth.getInstance();
        name = (EditText) rootView.findViewById(R.id.name);
        power = (EditText) rootView.findViewById(R.id.power);
        createStatementButton = (Button)rootView.findViewById(R.id.createButton);
        pdfButton = (ImageButton) rootView.findViewById(R.id.enviromental_pdf);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar2);

        type = (Spinner) rootView.findViewById(R.id.type);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                usersSelection = adapterView.getSelectedItem().toString();
                Log.d(TAG, "------------Choice : "+usersSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getContext(), "Please Choose a Type", Toast.LENGTH_LONG).show();
            }
        });
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(getContext(), R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        getLocationPermission();

        pdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        createStatementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String Sname = name.getText().toString();
                Boolean finall = false;

                int Spower = Integer.valueOf(power.getText().toString());

                Map<Object,Object> data = new HashMap<>();
                data.put("date", Timestamp.now());
                data.put("name" , Sname);
                data.put("type", usersSelection);
                data.put("final", finall);
                data.put("location" , new GeoPoint(usersMarkerLocation.latitude, usersMarkerLocation.longitude));
                data.put("power", Spower);
                data.put("status", 2);
                if(Spower>100){
                    data.put("enviromental_pdf" , "");
                }
                db.collection("statementFolders").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }
                });
                db.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("statementFolders").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Data Added Successfuly", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(getContext(), HomeActivity.class));
                    }
                });

            }
        });


        return rootView;
    }


    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.google_maps, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //getData();
        mMap = googleMap;
        //Initalize MyLocation stuff

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerDragListener(this);
        if(mLocationPermissionGranted){
            getDeviceLocation();
        }

    }


    private void getDeviceLocation(){

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            currentLocation = (Location) task.getResult();

                            if(currentLocation != null){
                                lati = currentLocation.getLatitude();
                                loni = currentLocation.getLongitude();

                                startMarker = mMap.addMarker(
                                        new MarkerOptions()
                                                .position(new LatLng(lati, loni))
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow))
                                                .draggable(true)
                                );
                                moveCamera(new LatLng(lati,loni),DEFAULT_ZOOM);

                            }
                            else{
                                getDeviceLocation();

                            }

                        }
                    }
                });

            }
        }catch(SecurityException e){
            Toast.makeText(getActivity(),"unable to get current location",Toast.LENGTH_SHORT).show();
        }


    }

    private void getLocationPermission(){

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }
            else{
                // When permission is not granted
                // Call method
                requestPermissions(
                        new String[] {
                                Manifest.permission
                                        .ACCESS_FINE_LOCATION,
                                Manifest.permission
                                        .ACCESS_COARSE_LOCATION },
                        100);
            }
        }
        else{
            // When permission is not granted
            // Call method
            requestPermissions(
                    new String[] {
                            Manifest.permission
                                    .ACCESS_FINE_LOCATION,
                            Manifest.permission
                                    .ACCESS_COARSE_LOCATION },
                    100);
        }

    }

    //gia na metatrepsw to drawable se icon gia to map
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void moveCamera(LatLng latLng,float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

    public LatLng getlatlong(){
        return usersMarkerLocation;
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        usersMarkerLocation = marker.getPosition();
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        Toast.makeText(getActivity(), "Changing location", Toast.LENGTH_SHORT).show();
    }
}