package com.example.gogo.Model;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class AnimationMode {
    private boolean isRun;
    private GeoQueryModel geoQueryModel;
    //moving marker

    private List<LatLng> polylindeList;
    private Handler handler;
    private  int index,next;
    private LatLng start,end;
    private float v;
    private double lat,lng;

    public AnimationMode(boolean isRun, GeoQueryModel geoQueryModel) {
        this.isRun = isRun;
        this.geoQueryModel = geoQueryModel;
        this.handler = new Handler();
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    public GeoQueryModel getGeoQueryModel() {
        return geoQueryModel;
    }

    public void setGeoQueryModel(GeoQueryModel geoQueryModel) {
        this.geoQueryModel = geoQueryModel;
    }

    public List<LatLng> getPolylindeList() {
        return polylindeList;
    }

    public void setPolylindeList(List<LatLng> polylindeList) {
        this.polylindeList = polylindeList;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public LatLng getStart() {
        return start;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
