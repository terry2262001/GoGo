package com.example.gogo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gogo.Common.Common;
import com.example.gogo.Model.EventBus.SelectPlaceEvent;
import com.example.gogo.Remote.IGoogleAPI;
import com.example.gogo.Remote.RetrofitClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.gogo.databinding.ActivityRequestUser2Binding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RequestUserActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRequestUser2Binding binding;
    private SelectPlaceEvent selectPlaceEvent;

    //Routes
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions polylineOptions, blacPolylineOptions;
    private List<LatLng> polylineList;

    private Marker originMarker, destinationMarker;


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
        if (EventBus.getDefault().hasSubscriberForEvent(SelectPlaceEvent.class)) {
            EventBus.getDefault().removeStickyEvent(SelectPlaceEvent.class);

        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectePlaceEvent(SelectPlaceEvent event) {
        selectPlaceEvent = event;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_user2);

        inits();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void inits() {
        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @SuppressLint("MissingPermission")
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectPlaceEvent.getOrigin(),18f));
                return true;

            };


        });
        drawPath(selectPlaceEvent);

        View locationButton = ((View) findViewById(Integer.parseInt("1"))
                .getParent())
                .findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        //right bottom
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.setMargins(0, 0, 0, 250);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
//            boolean  success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_maps_style));
//            if (!success){
//                Toast.makeText(this, "Load map style failed", Toast.LENGTH_SHORT).show();
//            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void drawPath(SelectPlaceEvent selectPlaceEvent) {
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                        "less_driving",
                        selectPlaceEvent.getOriginString(),selectPlaceEvent.getDestinatiString(),
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
                           polylineList = Common.decodePoly(polyline);

                        }
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(12);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blacPolylineOptions = new PolylineOptions();
                        blacPolylineOptions.color(Color.GRAY);
                        blacPolylineOptions.width(12);
                        blacPolylineOptions.startCap(new SquareCap());
                        blacPolylineOptions.jointType(JointType.ROUND);
                        blacPolylineOptions.addAll(polylineList);
                        blackPolyline = mMap.addPolyline(blacPolylineOptions);

                        // Animator
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
                        valueAnimator.setDuration(1100);
                        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        valueAnimator.addUpdateListener(value -> {
                          List<LatLng> points = greyPolyline.getPoints();
                          int percentValue = (int)valueAnimator.getAnimatedValue();
                          int size = points.size();
                          int newpoints = (int) (size*(percentValue/100.0f));
                          List<LatLng> p = points.subList(0,newpoints);
                          blackPolyline.setPoints(p);

                        });
                        valueAnimator.start();
                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(selectPlaceEvent.getOrigin())
                                .include(selectPlaceEvent.getDestination())
                                .build();
                        // add car icon for origin;
                        JSONObject object = jsonArray.getJSONObject(0);
                        JSONArray legs   = object.getJSONArray("legs");
                        JSONObject legObjects = legs.getJSONObject(0);

                        JSONObject time  = legObjects.getJSONObject("duration");
                        String duration =  time.getString("text");

                        String start_address = legObjects.getString("start_address");
                        String end_address = legObjects.getString("end_address");


                        addOriginMarker(duration, start_address);
                        adDestinationMarter(end_address);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,160));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom-1));






                    }catch (Exception e)
                    {
                      //  Snackbar.make(this,e.getMessage(),Snackbar.LENGTH_SHORT).show();
                        Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                })

        );
    }

    private void adDestinationMarter(String end_address) {
        View view = getLayoutInflater().inflate(R.layout.destination_info_window,null);
        TextView  txtDestination = (TextView) view.findViewById(R.id.txtDestination);
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        txtDestination.setText(Common.formatAddress(end_address));

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getDestination()));

    }

    private void addOriginMarker(String duration, String start_address) {
        View view = getLayoutInflater().inflate(R.layout.origin_info_window, null);
        TextView  txtTime = (TextView) view.findViewById(R.id.txtTime);
        TextView  txtOrigin = (TextView) view.findViewById(R.id.txtOrigin);
        txtTime.setText(Common.formatDuration(duration));
        txtOrigin.setText(Common.formatAddress(start_address));


        //create icon for marker
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getOrigin()));



    }
}