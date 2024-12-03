package com.example.dictionary;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class TranslateActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyCSImt7HBGMtdTKkF2sK64nTwnMypavlgQ";
    private static final String TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        EditText inputText = findViewById(R.id.input_text);
        TextView translatedText = findViewById(R.id.translated_text);
        Button translateButton = findViewById(R.id.button_translate);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        translateButton.setOnClickListener(v -> {
            String textToTranslate = inputText.getText().toString();
            if (textToTranslate.isEmpty()) {
                translatedText.setText("Please enter text to translate.");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);  // Show progress bar
            new TranslateTask(translatedText, progressBar).execute(textToTranslate, "en", "ar"); // Translate from English to Arabic
        });
    }

    private static class TranslateTask extends AsyncTask<String, Void, String> {
        private final TextView outputView;
        private final ProgressBar progressBar;

        TranslateTask(TextView outputView, ProgressBar progressBar) {
            this.outputView = outputView;
            this.progressBar = progressBar;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String text = params[0];
                String sourceLang = params[1];
                String targetLang = params[2];

                JSONObject requestBody = new JSONObject();
                requestBody.put("q", text);
                requestBody.put("source", sourceLang);
                requestBody.put("target", targetLang);
                requestBody.put("format", "text");

                URL url = new URL(TRANSLATE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("TranslateTask", "HTTP error code: " + responseCode);
                    return null; // Handle error appropriately
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                writer.close();

                Log.d("API_RESPONSE", response.toString());  // Log the API response

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray translations = jsonResponse.getJSONObject("data").getJSONArray("translations");
                return translations.getJSONObject(0).getString("translatedText");
            } catch (Exception e) {
                Log.e("TranslateTask", "Error translating text", e);
                return null; // Return null in case of an error
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);  // Hide progress bar once the task is complete
            if (result != null) {
                outputView.setText(result);
            } else {
                outputView.setText("Translation failed.");
            }
        }
    }
}