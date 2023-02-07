package com.example.sxediasipriject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static final String TAG = "HomeActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float DEFAULT_ZOOM = 10f;

    private Location currentLocation;
    List<Map<String,Object>> statements;

    private double lati;
    private double loni;



    public MapFragment() {
        // Required empty public constructor

        statements = new ArrayList<>();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        getLocationPermission();
        return view;
    }


    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        getData();
        mMap = googleMap;
        //Initalize MyLocation stuff

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        if(mLocationPermissionGranted){
            getDeviceLocation();
        }


        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        // Add a marker in Sydney and move the camera

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

    synchronized private  void  createMarkers(){

        if(!statements.isEmpty()){
            for(int i=0; i<statements.size(); i++){
                if(statements.get(i) == null)
                    break;
                GeoPoint location = (GeoPoint)statements.get(i).get("location");
                String name = (String)statements.get(i).get("name");
                long status = (Long)statements.get(i).get("status");
                if(status == 1){

                    //Bitmap icon = Bitmap.createScaledBitmap(, 64, 64, false);
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .title(name)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.green))
                        );
                }
                else if(status == 2){
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow))
                    );
                }else if(status == 3) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red))
                    );
                }
            }

        }


    }

    synchronized private void getData(){


        db.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList;

                if (queryDocumentSnapshots.isEmpty()) {

                    return;
                } else {

                    Log.d(TAG, "DEBUGGGGGGGGGGGGGGGGGGGGGGGG NUMBER 250-------------------------------------------------------");
                    snapshotList = queryDocumentSnapshots.getDocuments();
                    Log.d(TAG, "-----------------List" + snapshotList);
                    for(DocumentSnapshot snapshot : snapshotList){
                        Log.d(TAG, "ID of every loop" + snapshot.getId());
                        db.collection("Users").document(snapshot.getId()).collection("statementFolders").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<DocumentSnapshot> snapshotList2;
                                snapshotList2 = queryDocumentSnapshots.getDocuments();
                                for(DocumentSnapshot snapshot2 : snapshotList2) {
                                    Map<String, Object> document2 = snapshot2.getData();
                                    document2.put("id", snapshot2.getId());
                                    statements.add(document2);
                                    Log.d(TAG, "ADDEDD"+"  --------------------------------------------------------");
                                }
                                Log.d(TAG, "Map : " + statements.size());
                                createMarkers();
                            }
                        });

                    }
                }
            };

        });



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

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
}