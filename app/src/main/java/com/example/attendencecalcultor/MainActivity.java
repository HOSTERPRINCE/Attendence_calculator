package com.example.attendencecalcultor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button butt;
    TextView result;
    EditText edit;
    EditText edit2;
    TextToSpeech tts;

    private static final String TAG = "MainActivity";

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        butt = findViewById(R.id.button);
        result = findViewById(R.id.result);
        edit = findViewById(R.id.attend);
        edit2 = findViewById(R.id.classes);

        if (result == null || butt == null || edit == null || edit2 == null) {
            Log.e(TAG, "Error initializing views. Ensure all views exist in the layout.");
            Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            return;
        }
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "TTS: Language is not supported");
                    }
                } else {
                    Log.e(TAG, "TTS: Initialization failed");
                }
            }
        });
        butt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                try {
                    String attStr = edit.getText().toString().trim();
                    String clStr = edit2.getText().toString().trim();
                    if (!attStr.matches("\\d+") || !clStr.matches("\\d+")) {
                        Toast.makeText(MainActivity.this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int att = Integer.parseInt(attStr);
                    int cl = Integer.parseInt(clStr);

                    if (cl <= 0 || att < 0) {
                        Toast.makeText(MainActivity.this, "Total classes must be positive and attendance can't be negative", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    float percentage = ((float) att / cl) * 100;

                    String resultText;

                    if (percentage >= 75) {
                        int skippedClasses = 0;
                        while (((float) (att) / (cl + skippedClasses)) * 100 >= 75) {
                            skippedClasses++;
                        }
                        resultText = (skippedClasses - 1) + " classes can be skipped";
                    } else {
                        int counter = 0;
                        while (percentage < 75) {
                            counter++;
                            cl++;
                            percentage = ((float) (att + counter) / cl) * 100;
                        }
                        resultText = counter + " more classes required to reach 75%";
                    }

                    result.setText(resultText);
                    speakResult(resultText);

                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid input for attendance or classes: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                } catch (ArithmeticException e) {
                    Log.e(TAG, "Arithmetic error: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Calculation error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "An unexpected error occurred: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void speakResult(String resultText) {
        if (tts != null) {
            tts.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
