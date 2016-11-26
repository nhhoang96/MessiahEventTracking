package com.example.hoang.normalapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends Activity implements AIListener {
    private MapView mapView;
    private final LatLng boyer = new LatLng(40.156546, -76.989814);
    private final LatLng frey = new LatLng(40.157363, -76.987602);
    private final LatLng jordan = new LatLng(40.157875, -76.986918);
    private RelativeLayout locationOptions;
    private RelativeLayout dateOptions;
    private CheckBox boyerCheckBox;
    private CheckBox freyCheckBox;
    private CheckBox jordanCheckBox;
    private Button audioButton;
    private Spinner dropdownOptions;
    private EditText startDate;
    private ArrayAdapter<CharSequence> adapter;
    private Button filterButton;

    private AIService aiService;
    private TextToSpeech t1;

//    private HashMap<String, List<String>> categories;
//    private List<String> dropdownList;
//    private ExpandableListView expandable;
//    private expandAdapter expandAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) this.findViewById(R.id.mapview);

        locationOptions = (RelativeLayout) findViewById(R.id.locationOptions);
        dateOptions = (RelativeLayout) findViewById(R.id.dateOptions);

        boyerCheckBox = (CheckBox) this.findViewById(R.id.boyer);
        freyCheckBox = (CheckBox) this.findViewById(R.id.frey);
        jordanCheckBox = (CheckBox) this.findViewById(R.id.jordan);
        audioButton = (Button) this.findViewById(R.id.audioButton);
        startDate = (EditText) this.findViewById(R.id.startDate);
        filterButton = (Button) this.findViewById(R.id.filterButton);

        dropdownOptions = (Spinner) this.findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.options,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownOptions.setAdapter(adapter);
        dropdownOptions.setOnItemSelectedListener(new FilterOptionsAdapter());
//        expandable = (ExpandableListView) this.findViewById(R.id.expandList);
//        categories = DataProvider.getInfo();
//        dropdownList = new ArrayList<String>(categories.keySet());
//        expandAdapter = new expandAdapter(this, categories, dropdownList);
//        expandable.setAdapter(expandAdapter);

        MapDisplay display = new MapDisplay(mapView, savedInstanceState);
        display.getMap();

        boyerCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boyerCheckBox.isChecked() == false) {
                    boyerCheckBox.setChecked(true);
                    mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(boyer));
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 20.0f));
                    mapView.getMap().setIndoorEnabled(true);
                } else {
                    boyerCheckBox.setChecked(false);
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 15.0f));
                }
            }
        });

        freyCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!freyCheckBox.isChecked()) {
                    freyCheckBox.setChecked(true);
                    mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(frey));
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(frey, 20.0f));
                    mapView.getMap().setIndoorEnabled(true);
                } else {
                    freyCheckBox.setChecked(false);
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 15.0f));
                }
            }
        });

        jordanCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!jordanCheckBox.isChecked()) {
                    jordanCheckBox.setChecked(true);
                    mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(jordan));
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(jordan, 20.0f));
                    mapView.getMap().setIndoorEnabled(true);
                } else {
                    jordanCheckBox.setChecked(false);
                    mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 15.0f));
                }
            }
        });



        final AIConfiguration config = new AIConfiguration("384b243aa7c148b590da67014af0be92",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        filterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                aiService.startListening();
            }
        });
    }

    private class FilterOptionsAdapter implements AdapterView.OnItemSelectedListener {
        private int chosenLocation;

        public FilterOptionsAdapter() {
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 1) {
                if (dateOptions.getVisibility() == View.VISIBLE) {
                    dateOptions.setVisibility(View.INVISIBLE);
                }
                locationOptions.setVisibility(View.VISIBLE);
                audioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        promptSpeechInput();
                    }
                });
            } else if (position == 2) {
                if (locationOptions.getVisibility() == View.VISIBLE) {
                    locationOptions.setVisibility(View.INVISIBLE);
                }

                dateOptions.setVisibility(View.VISIBLE);
                startDate.addTextChangedListener(new DateWatch(startDate));
            }
            Toast.makeText(getBaseContext(),parent.getItemIdAtPosition(position) + " selected", Toast.LENGTH_LONG).show();
            chosenLocation = (int) parent.getItemIdAtPosition(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

        public int getChosenPosition() {
            return chosenLocation;
        }
    }
    @SuppressWarnings("deprecation")
    private void speakUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        t1.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        t1.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void promptSpeechInput() {
        try {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
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

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();
        final Result referenceRes = result;

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        } else {
            Toast.makeText(this, "NOT WORKING", Toast.LENGTH_LONG);
        }

        if (result.getResolvedQuery().contains("location")) {
            dropdownOptions.setSelection(1);
        } else if (result.getResolvedQuery().contains("date")) {
            dropdownOptions.setSelection(2);
        }

        t1 = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                t1.setLanguage(Locale.US);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speakGreater21(referenceRes.getFulfillment().getSpeech());
                } else {
                    speakUnder20(referenceRes.getFulfillment().getSpeech());
                }
            }
        });
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}
