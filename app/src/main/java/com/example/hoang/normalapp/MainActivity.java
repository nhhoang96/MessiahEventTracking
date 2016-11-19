package com.example.hoang.normalapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private GoogleMap map;
    private final LatLng boyer = new LatLng(40.156546, -76.989814);
    private final LatLng frey = new LatLng(40.157363, -76.987602);
    private final LatLng jordan = new LatLng(40.157875, -76.986918);
    private CheckBox boyerCheckBox;
    private CheckBox freyCheckBox;
    private CheckBox jordanCheckBox;
    private Button audioButton;
    private HashMap<String, List<String>> categories;
    private List<String> dropdownList;
    private ExpandableListView expandable;
    private expandAdapter expandAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) this.findViewById(R.id.mapview);
        boyerCheckBox = (CheckBox) this.findViewById(R.id.boyer);
        freyCheckBox = (CheckBox) this.findViewById(R.id.frey);
        jordanCheckBox = (CheckBox) this.findViewById(R.id.jordan);
        audioButton = (Button) this.findViewById(R.id.audioButton);

        expandable = (ExpandableListView) this.findViewById(R.id.expandList);
        categories = DataProvider.getInfo();
        dropdownList = new ArrayList<String>(categories.keySet());
        expandAdapter = new expandAdapter(this, categories,dropdownList);
        expandable.setAdapter(expandAdapter);

        mapView.onCreate(savedInstanceState);
        mapView.onResume(); //without this, map showed but was empty

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(boyer));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 15.0f));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.getUiSettings().setAllGesturesEnabled(true);
                //map = googleMap;
                final Marker boyerHall = googleMap.addMarker(new MarkerOptions().position(boyer).title("Boyer Hall"));
                final Marker freyHall = googleMap.addMarker(new MarkerOptions().position(frey).title("Frey Hall"));
                final Marker jordanCenter = googleMap.addMarker(new MarkerOptions().position(jordan).title("Jordan Center"));
            }
        });

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

    }

    public void promptSpeechInput() {
        try {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");


            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a ){
            //Toast.makeText(MainActivity.this, "Sorry, strange voice", Toast.LENGTH_LONG).show();
            String appPackageName = "com.google.android.googlequicksearchbox";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    @Override
    public void onActivityResult(int request_code, int result_code, Intent i) {
        super.onActivityResult(request_code, result_code, i);
        switch (request_code) {
            case 100: if (result_code == RESULT_OK && i!= null) {
                ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Toast.makeText(MainActivity.this, result.get(0), Toast.LENGTH_LONG).show();
                if (result.get(0).toLowerCase().contains("boyer")) {
                    boyerCheckBox.setChecked(true);
                    mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(boyer));
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 20.0f));
                    mapView.getMap().setIndoorEnabled(true);

                } else if (result.get(0).toLowerCase().contains("frey")) {
                    freyCheckBox.setChecked(true);
                    mapView.getMap().setIndoorEnabled(true);
                    mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(frey));
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(frey, 20.0f));

                } else if (result.get(0).toLowerCase().contains("jordan")) {
                    jordanCheckBox.setChecked(true);
                    mapView.getMap().setIndoorEnabled(true);
                    mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(jordan));
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(jordan, 20.0f));

                } else if (result.get(0).toLowerCase().contains("restart")) {
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 15.0f));
                    freyCheckBox.setChecked(false);
                    boyerCheckBox.setChecked(false);
                    jordanCheckBox.setChecked(false);
                }
            }
                break;
        }
    }
}
