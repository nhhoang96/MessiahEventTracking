package com.example.hoang.normalapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
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
    private TextToSpeech speech;

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

        MapDisplay display = new MapDisplay(mapView, savedInstanceState);
        display.getMap();

        boyerCheckBox.setOnCheckedChangeListener(new checkedBoxListener());
        freyCheckBox.setOnCheckedChangeListener(new checkedBoxListener());
        jordanCheckBox.setOnCheckedChangeListener(new checkedBoxListener());

        SystemClock.sleep(1000);
        final String gettingStarted = "Let's get started by letting us know what filter you would like to start with";
        speech = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    speech.setLanguage(Locale.US);
                    speakWords(gettingStarted);
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


    public void speakWords(String speechText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakGreater21(speechText);
        } else {
            speakUnder20(speechText);
        }
    }

    @SuppressWarnings("deprecation")
    private void speakUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        speech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private class checkedBoxListener implements CompoundButton.OnCheckedChangeListener {
        public checkedBoxListener() {

        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView == boyerCheckBox) {
                    conductFilter(boyerCheckBox, mapView, boyer);
                } else if (buttonView == freyCheckBox) {
                    conductFilter(freyCheckBox, mapView, frey);
                } else if (buttonView == jordanCheckBox) {
                    conductFilter(jordanCheckBox, mapView, jordan);
                }
            } else {
                mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(boyer, 15.0f));
            }
        }
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


    public void promptSpeechInput() {
        try {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");


            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a ){
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
                    conductFilter(boyerCheckBox,mapView, boyer);

                } else if (result.get(0).toLowerCase().contains("frey")) {
                    conductFilter(freyCheckBox,mapView, frey);

                } else if (result.get(0).toLowerCase().contains("jordan")) {
                    conductFilter(jordanCheckBox,mapView, jordan);

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
    public void conductFilter(CheckBox chosenCheckbox, MapView mapView, LatLng chosenLatLng) {
        chosenCheckbox.setChecked(true);
        mapView.getMap().setIndoorEnabled(true);
        mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(chosenLatLng));
        mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(chosenLatLng, 20.0f));


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

                String mappedValue = entry.getValue().toString().toLowerCase();

                if (mappedValue.contains("jordan")) {
                    conductFilter(jordanCheckBox, mapView, jordan);
                } else if (mappedValue.contains("frey")) {
                    conductFilter(freyCheckBox, mapView, frey);
                } else if (mappedValue.contains("boyer")) {
                    conductFilter(boyerCheckBox, mapView, boyer);
                } else if (isValidDate(mappedValue)) {
                    Toast.makeText(this, "Date accepted", Toast.LENGTH_LONG).show();
                    String reformatedDate = reformatDate(mappedValue);
                    DateWatch dateWatch = new DateWatch(startDate);
                    dateWatch.enterSpeechDate(reformatedDate);
                    conductFilter(boyerCheckBox, mapView, boyer);
                }
            }

        } else {
            Toast.makeText(this, "NOT WORKING", Toast.LENGTH_LONG);
        }

        if (result.getResolvedQuery().contains("location")) {
            dropdownOptions.setSelection(1);
        } else if (result.getResolvedQuery().contains("date")) {
            dropdownOptions.setSelection(2);
        }

        speech = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    speech.setLanguage(Locale.US);
                    speakWords(referenceRes.getFulfillment().getSpeech());
                }
            }
        });



    }

    // Check if the date is valid or not
    public boolean isValidDate(String inDate) {

        java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
        simpleDateFormat.setLenient(false);
        if (!inDate.contains("-")) {
            return false;
        } else {
            if(inDate.split("-").length >= 2) {
                Toast.makeText(this, "Valid date", Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(this, "Invalid date", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }

    public String reformatDate(String inputDate) {
        inputDate= inputDate.trim().substring(1,inputDate.length() - 1);
        String[] separateParts = inputDate.split("-");
        String newDate = separateParts[1] + "/" + separateParts[2] + "/" + separateParts[0];
        return newDate;
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
