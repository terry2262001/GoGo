package com.example.gogo.Fragment;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.BinderThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gogo.Callback.IFirebaseDriverInfoListener;
import com.example.gogo.Callback.IFirebaseFailedListener;
import com.example.gogo.Common.Common;
import com.example.gogo.Model.AnimationMode;
import com.example.gogo.Model.DriverDeoModel;
import com.example.gogo.Model.DriverInfoModel;
import com.example.gogo.Model.EventBus.SelectPlaceEvent;
import com.example.gogo.Model.GeoQueryModel;
import com.example.gogo.R;
import com.example.gogo.Remote.IGoogleAPI;
import com.example.gogo.Remote.RetrofitClient;
import com.example.gogo.RequestUserActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class MapsFragment extends Fragment implements OnMapReadyCallback, IFirebaseFailedListener, IFirebaseDriverInfoListener {
    //
    //
    // places
    @BindView(R.id.activity_main)
    SlidingUpPanelLayout slidingUpPanelLayout;
    private AutocompleteSupportFragment autocompleteSupportFragment;


    //
    private GoogleMap mMap;

    public static final int PERMISSION_CODE = 100;


    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private LocationManager locationManager;
    //location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    SupportMapFragment mapFragment;
    private boolean isFirstTime = true;

    //Online System
    DatabaseReference onlineRef, currentUserRef, driverLocationRef;
    GeoFire geoFire;
    Marker mCurrent;
    String countryName;
    String roomid;
    ///limit space  user
    private double distance = 1.0; //1km
    private Location previosLocation,currentLocation;
    private static final double LIMIT_RANGE = 10.0;//10km


    IFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;

    boolean firstTime = true;
    String countryName1;
    //
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;
    boolean isGo = true;




    //end user



    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        //  geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);

    }



    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            // && currentUserRef != null
            if (snapshot.exists()) {
                currentUserRef.onDisconnect().removeValue();
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //innit View
        SharedPreferences pref = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        roomid = pref.getString("roomid", "none");

        init();
        initViews(view);


        return view;
    }

    private void initViews(View view) {
        ButterKnife.bind(this,view);

    }


    @SuppressLint("MissingPermission")
    private void init() {
        //
        Places .initialize(getContext(),getString(R.string.MAPS_API_KEY));
        autocompleteSupportFragment = (AutocompleteSupportFragment)getChildFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteSupportFragment.setHint(getString(R.string.where_to));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Snackbar.make(getView(),"" + status.getStatusMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
          //     Snackbar.make(getView(),"" + place.getLatLng(), Snackbar.LENGTH_LONG).show();
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(location1 -> {
                            LatLng origin  = new LatLng(location1.getLatitude(),location1.getLongitude());
                            LatLng destination  = new LatLng(place.getLatLng().latitude,place.getLatLng().longitude);

                            startActivity(new Intent(getContext(), RequestUserActivity.class));
                            EventBus.getDefault().postSticky(new SelectPlaceEvent(origin,destination));
                        });

            }
        });
        //

        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);
        //2 rider
        iFirebaseDriverInfoListener = this;
        iFirebaseFailedListener = this;
        onlineRef = FirebaseDatabase.getInstance().getReference().child("UserLocation");
        driverLocationRef = FirebaseDatabase.getInstance().getReference("UserLocation").child(roomid);
        currentUserRef = driverLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        geoFire = new GeoFire(driverLocationRef);
        //


        registerOnlineSystem();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(50f); //  50f50m
        mLocationRequest.setInterval(15000);//15 sec//15000
        mLocationRequest.setFastestInterval(10000);//10 sec
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LatLng newPotion = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPotion, 18f));
                //  updateLocation

                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                locationResult.getLastLocation().getLongitude()),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                                if (error != null) {
                                    Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_SHORT)
                                            .show();
                                } else {
                                    if (isFirstTime){
                                        Snackbar.make(mapFragment.getView(), "You are online", Snackbar.LENGTH_SHORT)
                                                .show();
                                        isFirstTime = false;
                                        isGo = false;


                                    }

                                }
                            }
                        });




                loadAvailableDrivers();

//
                ////////2: rider : if user has change location , calculate and load driver again
                if (firstTime) {
                    previosLocation = currentLocation = locationResult.getLastLocation();
                    firstTime = false;
                    setRestrictPlacesInContry(locationResult.getLastLocation());

                } else {
                    previosLocation = currentLocation;
                    currentLocation = locationResult.getLastLocation();
                }
                if (previosLocation.distanceTo(currentLocation) / 1000 < 0.5) {//// not over range
                    //  loadAvailableDrivers();



                } else {

                }

                ///////


            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
       // loadAvailableDrivers();
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addressList;
                        try {
                            addressList = geocoder.getFromLocation(location.getLatitude(),
                                    location.getLongitude(), 1);
                            String addressLine = addressList.get(0).getAddressLine(0);
                            if (addressList.size() > 0 ){
                                countryName = addressList.get(0).getCountryName();
                            }
                            if (!TextUtils.isEmpty(countryName)){

                            }else {
                                Snackbar.make(getView(),getString(R.string.city_name_emplty),Snackbar.LENGTH_LONG).show();
                            }




                        } catch (IOException e) {
                            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void setRestrictPlacesInContry(Location location) {
        try {
            Geocoder geocoder  = new Geocoder(getContext(),Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if (addressList.size() > 0){
                autocompleteSupportFragment.setCountry(addressList.get(0).getCountryCode());
            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void loadAvailableDrivers() {
        ArrayList<DriverDeoModel> listener = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addressList;
                        try {
                            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            countryName1 = addressList.get(0).getCountryName();

//                            ///query
                            DatabaseReference dirver_location_ref = FirebaseDatabase.getInstance()
                                    .getReference("UserLocation").child(roomid);
                            GeoFire gf = new GeoFire(dirver_location_ref);
                            GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.getLatitude(),
                                    location.getLongitude()), distance);
                            geoQuery.removeAllListeners();
                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    Common.driverFound.add(new DriverDeoModel(key, location));
                                    listener.add(new DriverDeoModel(key, location));


                                }

                                @Override
                                public void onKeyExited(String key) {

                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {
                                    Common.driverFound.remove(new DriverDeoModel(key,location));

                                }

                                @Override
                                public void onGeoQueryReady() {
                                    if (distance < LIMIT_RANGE) {
                                        distance++;
                                        //  loadAvailableDrivers(); // continue search in new distance
                                    } else {
                                        distance = 1.0;// reset it
                                        addDriverMarker();
                                    }


                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();

                                }
                            });
                            DatabaseReference userLocationRoom = FirebaseDatabase.getInstance()
                                    .getReference("UserLocation").child(roomid);
                            userLocationRoom.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    // Have new User
                                    GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                                    GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),geoQueryModel.getL().get(1));
                                    DriverDeoModel driverDeoModel = new DriverDeoModel(snapshot.getKey(),geoLocation);
                                    Location newUserLocation = new Location("");
                                    newUserLocation.setLatitude(geoLocation.latitude);
                                    newUserLocation.setLongitude(geoLocation.longitude);
                                    float newDistance = location.distanceTo(newUserLocation) / 1000 ;// in km
                                    if (newDistance <= LIMIT_RANGE)
                                    {
                                        findDriverByKey(driverDeoModel);// if user in range, add to map
                                    }


                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void addDriverMarker() {
        if (Common.driverFound.size() > 0) {
            Observable.fromIterable(Common.driverFound)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(driverDeoModel -> {
                        // on Next
                        findDriverByKey(driverDeoModel);
                    }, throwable -> {
                        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }, () -> {
                    });

        } else {
            Snackbar.make(getView(), getString(R.string.driver_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void findDriverByKey(DriverDeoModel driverDeoModel) {
        FirebaseDatabase.getInstance()
                .getReference("Users").child(driverDeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            driverDeoModel.setDriverInfoModel(snapshot.getValue(DriverInfoModel.class));
                            if (!driverDeoModel.getDriverInfoModel().getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                iFirebaseDriverInfoListener.onDriverInfoLoadSuccess(driverDeoModel);
                            }

                        } else {
                            iFirebaseFailedListener.onFirebaseLoadFailed(getString(R.string.not_found_key) + driverDeoModel.getDriverInfoModel().getFullname());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iFirebaseFailedListener.onFirebaseLoadFailed(error.getMessage());

                    }
                });

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocation();


    }

    private void showMsg(String s, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(s)
                .setPositiveButton("OK", onClickListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int result2 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showMsg("You need to allow access to the Permission", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }
    }

    private void checkLocation() {
        if (!checkPermission()) {
            requestPermission();
        } else {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

                @SuppressLint("MissingPermission")
                @Override
                public boolean onMyLocationButtonClick() {
                    fusedLocationProviderClient.getLastLocation()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnSuccessListener(location -> {
                                LatLng userLatng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatng, 18f));

                            });
                    return true;
                }


            });
            View locationButton = ((View) mapFragment.getView(). findViewById(Integer.parseInt("1"))
                    .getParent())
                    .findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            //right bottom
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.setMargins(0, 0, 0, 250);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean location = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (location) {
                    Toast.makeText(getContext(), "Permission Granted.", Toast.LENGTH_SHORT).show();
                    checkLocation();
                } else {
                    Toast.makeText(getContext(), "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDriverInfoLoadSuccess(DriverDeoModel driverDeoModel) {
        //if already  have marker with this key , doesn't  set again
        if (!Common.markerList.containsValue(driverDeoModel.getKey())) {
            driverLocationRef = FirebaseDatabase.getInstance().getReference("UserLocation").child(roomid);
            currentUserRef = driverLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            Common.markerList.put(driverDeoModel.getKey(),
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(driverDeoModel.getGeoLocation().latitude, driverDeoModel.getGeoLocation().longitude))
                            .title(Common.buildName(driverDeoModel.getDriverInfoModel().getUsername()))
                            //  .title(driverDeoModel.getDriverInfoModel().getUsername())
                            .snippet(driverDeoModel.getDriverInfoModel().getBio())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))

                    ));


        }

        if (!TextUtils.isEmpty(roomid)) {
            DatabaseReference driverlocaion = FirebaseDatabase.getInstance()
                    .getReference("UserLocation").child(roomid).child(driverDeoModel.getKey());
            driverlocaion.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.hasChildren()) {

                        if (Common.markerList.get(driverDeoModel.getKey()) != null) {
                            Common.markerList.get(driverDeoModel.getKey()).remove();//remove marker
                            Common.markerList.remove(driverDeoModel.getKey());// remove marker infor has map
                            Common.driverlocationSubcrible.remove(driverDeoModel.getKey());//remove drive infomation too
                            driverlocaion.removeEventListener(this);// // remove evenlistender;

                        }

                    } else {
                        if (Common.markerList.get(driverDeoModel.getKey()) != null) {
                            GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                            AnimationMode animationMode = new AnimationMode(false, geoQueryModel);
                            if (Common.driverlocationSubcrible.get(driverDeoModel.getKey()) != null) {
                                Marker currentMarker = Common.markerList.get(driverDeoModel.getKey());
                                AnimationMode oldPostion = Common.driverlocationSubcrible.get(driverDeoModel.getKey());
                                String from = new StringBuilder()
                                        .append(oldPostion.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(oldPostion.getGeoQueryModel().getL().get(1))
                                        .toString();
                                String to = new StringBuilder()
                                        .append(animationMode.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(animationMode.getGeoQueryModel().getL().get(1))
                                        .toString();
                                moveMarkerAnimation(driverDeoModel.getKey(), animationMode, currentMarker, from, to,driverDeoModel);


                            } else {
                                //First location init
                                Common.driverlocationSubcrible.put(driverDeoModel.getKey(), animationMode);

                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void moveMarkerAnimation(String key, AnimationMode animationMode, Marker currentMarker, String from, String to,DriverDeoModel driverDeoModel) {
        if (!animationMode.isRun()) {
            //requestApi
            compositeDisposable.add(iGoogleAPI.getDirections("driving",
                            "less_driving",
                            from,to,
                            getString(R.string.MAPS_API_KEY))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(returnResult ->{
                        Log.d("API_RETURN", returnResult);
                        try {
                            //parse JSON
                            JSONObject jsonObject = new JSONObject(returnResult);
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                            for (int i = 0; i < jsonArray.length();i++)
                            {
                                JSONObject  route = jsonArray.getJSONObject(i);
                                JSONObject poly = route.getJSONObject("overview_polyline");
                                String  polyline =  poly.getString("points");
                                //polylindeList = Common.decodePoly(polyline);
                                animationMode.setPolylindeList(Common.decodePoly(polyline));
                            }
                            //moving
                            // handler =  new Handler();
                            //  index = -1;
                            //  next = 1;
                            animationMode.setIndex(-1);
                            animationMode.setIndex(-1);
                            animationMode.setNext(1);


                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (animationMode.getPolylindeList() !=null && animationMode.getPolylindeList().size() > 1 ){
                                        if (animationMode.getIndex() < animationMode.getPolylindeList().size()-2){
                                            //index++
                                            animationMode.setIndex(animationMode.getIndex()+1);
                                            // next  = index+1;
                                            animationMode.setNext(animationMode.getIndex()+1);
                                            //   start = polylindeList.get(index);
                                            animationMode.setStart(animationMode.getPolylindeList().get(animationMode.getIndex()));
                                            //  end = polylindeList.get(next);
                                            animationMode.setEnd(animationMode.getPolylindeList().get(animationMode.getNext()));

                                        }
                                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
                                        valueAnimator.setDuration(3000);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(value -> {
                                            //  v =  value.getAnimatedFraction();
                                            animationMode.setV(value.getAnimatedFraction());
                                            //  lat = v*end.latitude + (1-v) * start.latitude;
                                            animationMode.setLat(animationMode.getV()* animationMode.getEnd().latitude+
                                                    (1-animationMode.getV())
                                                            * animationMode.getStart().latitude);
                                            // lng = v*end.longitude + (1-v) * start.longitude;
                                            animationMode.setLng(animationMode.getV()* animationMode.getEnd().longitude+
                                                    (1-animationMode.getV())
                                                            * animationMode.getStart().longitude);
                                            LatLng newPos = new LatLng(animationMode.getLat(),animationMode.getLng());
                                            currentMarker.setPosition(newPos);
                                            currentMarker.setAnchor(0.5f,0.5f);
                                            currentMarker.setRotation(Common.getBearing(animationMode.getStart(),newPos));

                                        });
                                        valueAnimator.start();
                                        if (animationMode.getIndex()  < animationMode.getPolylindeList().size() - 2 ){//reach destination
                                            animationMode.getHandler().postDelayed(this,1500);
                                        }else if (animationMode.getIndex()  < animationMode.getPolylindeList().size() - 1){//done
                                            animationMode.setRun(false);
                                            Common.driverlocationSubcrible.put(key,animationMode);//update data


                                        }


                                    }


                                }
                            };
                            //run handle
                            animationMode.getHandler().postDelayed(runnable,1500);



                        }catch (Exception e)
                        {
                            Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_SHORT).show();
                        }

                    })

            );
        }
    }


}