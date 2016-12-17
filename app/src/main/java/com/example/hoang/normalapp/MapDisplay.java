package com.example.hoang.normalapp;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Hoang Nguyen on 11/25/2016.
 */

public class MapDisplay {
    private MapView mapView;
    private final LatLng boyer = new LatLng(40.156546, -76.989814);
    private final LatLng frey = new LatLng(40.157363, -76.987602);
    private final LatLng jordan = new LatLng(40.157875, -76.986918);
    private Location myLocation;
    private LatLng currentLatng;
    private Bundle savedInstanceState;

    public MapDisplay(MapView mapView, Bundle savedInstanceState) {
        this.mapView = mapView;
        this.savedInstanceState = savedInstanceState;
    }

    public void getMap() {
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); //without this, map showed but was empty

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);
                myLocation = googleMap.getMyLocation();

                if (myLocation != null) {
                    currentLatng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatng, 15.0f));
                } else {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 15.0f));
                }


                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.getUiSettings().setAllGesturesEnabled(true);

                final Marker boyerHall = googleMap.addMarker(new MarkerOptions().position(boyer).title("Boyer Hall"));
                final Marker freyHall = googleMap.addMarker(new MarkerOptions().position(frey).title("Frey Hall"));
                final Marker jordanCenter = googleMap.addMarker(new MarkerOptions().position(jordan).title("Jordan Center"));
            }
        });
    }
}
