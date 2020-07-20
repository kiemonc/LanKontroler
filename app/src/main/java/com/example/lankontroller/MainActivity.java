package com.example.lankontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import LanKontrollerComunication.LanKontroller;

public class MainActivity extends AppCompatActivity {

    private LanKontroller lk;
    private int number;
    private String unknown = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lk = new LanKontroller("192.168.1.100");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        String currentTemperaturetemp;
                        try {
                            currentTemperaturetemp = Double.toString(lk.getTemperature());
                        } catch (IOException e) {
                            currentTemperaturetemp = "unknown";
                        }
                        final String currentTemperature = currentTemperaturetemp;
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView temperature = (TextView) findViewById(R.id.textView3);
                                temperature.setText(currentTemperature);
                            }
                        });
                    }
                } catch (InterruptedException e) {

                }
            }
        };

        thread.start();

    }
}