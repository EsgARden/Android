package net.gotev.speechdemo;

import android.Manifest;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.SpeechUtil;
import net.gotev.speech.TextToSpeechCallback;
import net.gotev.speech.ui.SpeechProgressView;
import net.gotev.toyproject.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SpeechDelegate {

    private ImageButton button;
    private Button speak;
    private TextView text;
    private EditText textToSpeech;
    private SpeechProgressView progress;
    private LinearLayout linearLayout;
    private final String API_KEY = "CC7s0SEdWk8ecTT3i6VoTmsAPjA";
    private final String API_URL = "https://www.cleverbot.com/";
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    Frank frank = retrofit.create(Frank.class);

    public static class FrankResponse {
        public final String output;

        public FrankResponse(String output) {
            this.output = output;
        }
    }

    public interface Frank {
        @GET("/getreply")
        Call<FrankResponse> responses(
            @Query("input") String input,
            @Query("key") String key
        );
    }


//    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
//
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//        }
//
//        @Override
//        protected Boolean doInBackground(String... urls) {
//            try {
//
//                //------------------>>
//                HttpGet httppost = new HttpGet("YOU URLS TO JSON");
//                HttpClient httpclient = new DefaultHttpClient();
//                HttpResponse response = httpclient.execute(httppost);
//
//                // StatusLine stat = response.getStatusLine();
//                int status = response.getStatusLine().getStatusCode();
//
//                if (status == 200) {
//                    HttpEntity entity = response.getEntity();
//                    String data = EntityUtils.toString(entity);
//
//
//                    JSONObject jsono = new JSONObject(data);
//
//                    return true;
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//
//                e.printStackTrace();
//            }
//            return false;
//        }
//
//        protected void onPostExecute(Boolean result) {
//
//        }
//    }


    public static void main(String... args) throws IOException {
        // Create a very simple REST adapter which points the GitHub API
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        button = (ImageButton) findViewById(R.id.button);
        button.setOnClickListener(view -> onButtonClick());

        speak = (Button) findViewById(R.id.speak);
        speak.setOnClickListener(view -> onSpeakClick());

        text = (TextView) findViewById(R.id.text);
        textToSpeech = (EditText) findViewById(R.id.textToSpeech) ;
        progress = (SpeechProgressView) findViewById(R.id.progress);

        int[] colors = {
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        };
        progress.setColors(colors);

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(API_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        // Create an instance of our GitHub API interface.
//        Frank frank = retrofit.create(Frank.class);
//
//        // Create a call instance for looking up Retrofit contributors.
//        Call<FrankResponse> call = frank.responses("Hello", API_KEY);
//
//        call.enqueue(new Callback<FrankResponse>() {
//            @Override
//            public void onResponse(Call<FrankResponse> call, Response<FrankResponse> response) {
//                FrankResponse myItems = response.body();
//                Log.d("ANDRES_GAY", myItems.output);
//            }
//
//
//            @Override
//            public void onFailure(Call<FrankResponse> call, Throwable t) {
//                Log.e("ANDRES_MESSED", "someError" + t.getMessage());
//            }
//        });

//        // Fetch and print a list of the contributors to the library.
//        try {
//            List<Response> responses = call.execute().body();
//            for (Response response : responses) {
//                System.out.println(response.output);
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }

    private void onButtonClick() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            RxPermissions.getInstance(this)
                    .request(Manifest.permission.RECORD_AUDIO)
                    .subscribe(granted -> {
                        if (granted) { // Always true pre-M
                            onRecordAudioPermissionGranted();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.permission_required, Toast.LENGTH_LONG);
                        }
                    });
        }
    }

    private void onRecordAudioPermissionGranted() {
        button.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);

        try {
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(progress, MainActivity.this);

        } catch (SpeechRecognitionNotAvailable exc) {
            showSpeechNotSupportedDialog();

        } catch (GoogleVoiceTypingDisabledException exc) {
            showEnableGoogleVoiceTyping();
        }
    }

    private void onSpeakClick() {
        if (textToSpeech.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.input_something, Toast.LENGTH_LONG).show();
            return;
        }

        Speech.getInstance().say(textToSpeech.getText().toString().trim(), new TextToSpeechCallback() {
            @Override
            public void onStart() {
                Toast.makeText(MainActivity.this, "TTS onStart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "TTS onCompleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "TTS onError", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStartOfSpeech() {
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        //Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
    }

    @Override
    public void onSpeechResult(String result) {

        button.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);

        text.setText(result);

        Call<FrankResponse> call = frank.responses(result, API_KEY);

        call.enqueue(new Callback<FrankResponse>() {
            @Override
            public void onResponse(Call<FrankResponse> call, Response<FrankResponse> response) {
                FrankResponse myItems = response.body();
                Speech.getInstance().say(myItems.output);
            }


            @Override
            public void onFailure(Call<FrankResponse> call, Throwable t) {
                Log.e("ANDRES_MESSED", "someError" + t.getMessage());
            }
        });
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        text.setText("");
        for (String partial : results) {
            text.append(partial + " ");
        }
    }

    private void showSpeechNotSupportedDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        SpeechUtil.redirectUserToGoogleAppOnPlayStore(MainActivity.this);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.speech_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private void showEnableGoogleVoiceTyping() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_google_voice_typing)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .show();
    }

}
