package com.vijaya.speechtotext;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.*;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTv;
    private ImageButton mSpeakBtn;

    private TextToSpeech tts;

    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;


    private static final String TAG = "MyActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("PREFS",0);
        edit = prefs.edit();

        mVoiceInputTv = (TextView) findViewById(R.id.voiceInput);
        mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);



        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS){

                    tts.setLanguage(Locale.US);

                    tts.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
                    Log.i(TAG, "\"Hello\"");
                    mVoiceInputTv.append("Hello");
                    //mVoiceInputTv.append("\nWhat is your name?");
                } else {
                //    ready = false;
                    Log.i(TAG, "Failure");
                    //tts.speak("This is Hello number 2 because the first one didn't work", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello to you");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mVoiceInputTv.setText(result.get(0));
                    Log.i(TAG, result.get(0));
                    if(result.get(0).equalsIgnoreCase("hello")) {
                        tts.speak("What is your name", TextToSpeech.QUEUE_FLUSH, null);
                        mVoiceInputTv.append("\nWhat is your name?");
                        Log.i(TAG, "\"What is your name?\"");
                    }else if(result.get(0).contains("name")) {
                        // Set the Greeting by indexing
                        String name = result.get(0).substring(result.get(0).lastIndexOf(' ') + 1);
                        // Setting into Editor
                        edit.putString("name", name).apply();
                        tts.speak("Hello, " + name,
                                TextToSpeech.QUEUE_FLUSH, null);
                        mVoiceInputTv.append("\nHello, " + name);
                        Log.i(TAG, "\"Hello, "+ name+"\"");
                    }else if(result.get(0).contains("not feeling good")){
                        tts.speak("I can understand. Please tell your symptoms in short",
                                TextToSpeech.QUEUE_FLUSH, null);
                        mVoiceInputTv.append("\nI can understand. Please tell your symptoms in short");
                        Log.i(TAG, "\"I can understand. Please tell your symptoms in short.\"");
                    }else if(result.get(0).contains("thank you")){
                        tts.speak("Thank you too, "+prefs.getString("name","")+" Take care.", TextToSpeech.QUEUE_FLUSH, null);
                        mVoiceInputTv.append("\nThank you too, "+prefs.getString("name","")+" Take care.");
                        Log.i(TAG, "\"Thank you too, "+prefs.getString("name","")+" Take care.\"");
                    }else if(result.get(0).contains("what time")){
                        SimpleDateFormat sdfDate =new SimpleDateFormat("HH:mm");//dd/MM/yyyy
                        Date now = new Date();
                        String[] strDate = sdfDate.format(now).split(":");
                        if(strDate[1].contains("00"))strDate[1] = "o'clock";
                        tts.speak("The time is : "+sdfDate.format(now), TextToSpeech.QUEUE_FLUSH, null);
                        mVoiceInputTv.append("\nThe time is : "+sdfDate.format(now));
                        Log.i(TAG, "\"The time is : "+sdfDate.format(now)+"\"");
                    }else if(result.get(0).contains("medicine")){
                        tts.speak("I think you have fever. Please take this medicine.",
                                TextToSpeech.QUEUE_FLUSH, null);
                        mVoiceInputTv.append("\nI think you have fever. Please take this medicine");
                        Log.i(TAG, "\"I think you have fever. Please take this medicine.\"");
                    } else {
                        tts.speak("Sorry, I cant help you with that", TextToSpeech.QUEUE_FLUSH, null);
                        mVoiceInputTv.append("\nSorry, I cant help you with that");
                        Log.i(TAG, "\"Sorry, I cant help you with that.\"");
                    }
                }
                break;
            }

        }
    }
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}