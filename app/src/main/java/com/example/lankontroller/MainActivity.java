package com.example.lankontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import LanKontrollerComunication.LanKontroller;

public class MainActivity extends AppCompatActivity {

    private LanKontroller lk;
    private int number;
    private String unknown;
    private final int refreshTime = 10000;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lk = new LanKontroller("192.168.1.100");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unknown = getString(R.string.unknown);

        connected = false;

        //wyłączenie rotacji ekranu
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //zapytania http można robić tylko w oddielnym wątku, a aktualizować tekst można aktualizować tylko w wątku głównym
        final Thread refreshing = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {

                        //sprawdzanie temperatury i stanu przekaźników i stanu grzania
                        String currentTemperatureTemp;
                        boolean[] stateTemp;
                        int targetTemperatureTemp;
                        boolean heatingStateTemp;
                        try {
                            currentTemperatureTemp = Double.toString(lk.getTemperature());
                            stateTemp = lk.getRelayState();
                            targetTemperatureTemp = lk.getTargetTemperature();
                            heatingStateTemp = lk.getHeatingState();
                            connected = true;
                        } catch (IOException e) {
                            currentTemperatureTemp = unknown;
                            stateTemp = null;
                            targetTemperatureTemp = -1;
                            heatingStateTemp = false;
                            connected = false;

                        }
                        final String currentTemperature = currentTemperatureTemp;
                        final boolean [] state = stateTemp;
                        final int targetTemperature = targetTemperatureTemp;
                        final boolean heatingState = heatingStateTemp;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //połączenie
                                if(connected == false) {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Brak połączenia z LK", Toast.LENGTH_SHORT).show());
                                }
                                //temperatura
                                TextView temperature = (TextView) findViewById(R.id.temperatureValue);
                                if(connected) {
                                    temperature.setText(currentTemperature);
                                } else {
                                    temperature.setText(unknown);
                                }

                                //przekaźniki
                                List<TextView> stateTextList = new ArrayList<>();
                                stateTextList.add((TextView) findViewById(R.id.state0));
                                stateTextList.add((TextView) findViewById(R.id.state1));
                                stateTextList.add((TextView) findViewById(R.id.state2));
                                int i = 0;
                                for(TextView stateText : stateTextList) {
                                    if(state == null) {
                                        stateText.setText(unknown);
                                    } else {
                                        if(state[i]) {
                                            stateText.setText(getString(R.string.on));
                                            stateText.setBackgroundColor(Color.GREEN);
                                        } else {
                                            stateText.setText(getString(R.string.off));
                                            stateText.setBackgroundColor(Color.RED);
                                        }
                                    }
                                    i++;
                                }
                                //zadana temperatura
                                EditText targetTemperatureText = (EditText) findViewById(R.id.editTextNumber);
                                if(targetTemperatureText.hasFocus() == false) {
                                    targetTemperatureText.setText(Integer.toString(targetTemperature));
                                }

                                //przyciski
                                Button buttonOn = (Button) findViewById(R.id.buttonOn);
                                Button buttonOff = (Button) findViewById(R.id.buttonOff);
                                if(connected) {
                                    if (heatingState) {
                                        buttonOn.setBackgroundColor(Color.GREEN);
                                        buttonOff.setBackgroundResource(android.R.drawable.btn_default);
                                    } else {
                                        buttonOn.setBackgroundResource(android.R.drawable.btn_default);
                                        buttonOff.setBackgroundColor(Color.RED);
                                    }
                                } else {
                                    buttonOn.setBackgroundResource(android.R.drawable.btn_default);
                                    buttonOff.setBackgroundResource(android.R.drawable.btn_default);
                                }


                            }
                        });
                        Thread.sleep(refreshTime);
                    }
                } catch (InterruptedException e) {

                }
            }
        };
        refreshing.start();


        //porzyciski w zaleźności od stanu ogrzewania zmieniają kolor, ale dopiero po sprawdzeniu stanu ogrzewania w LK

        //przycisk wyłączania ogrzewania
        final Button buttonOff = findViewById(R.id.buttonOff);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            lk.turnOffHeating();
                        } catch (IOException e) {

                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        });

        //przycisk właczenia ogrzewania
        final Button buttonOn = findViewById(R.id.buttonOn);
        buttonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        EditText temperatureField= (EditText) findViewById(R.id.editTextNumber);
                        int temperature = 0;
                        try {
                            temperature = Integer.parseInt(temperatureField.getText().toString());
                        } catch(NumberFormatException e) {

                        }
                        if(temperature > 20 && temperature < 70) {
                            try {
                                lk.turnOnHeating(temperature);
                            } catch (IOException e) {

                            }
                        } else {
                            UserDialog exampleDialog = new UserDialog("Informacja", "Wporawdzona temperatura jest poza zakresem");
                            exampleDialog.show(getSupportFragmentManager(), "poza zakresem");
                        }
                    }
                };
                thread.start();
            }
        });

    }

    @Override
    public void onBackPressed() {

        //wyłączenie focusu na edytowalnym tekscie po kliknieciu przycisku wstecz
        EditText temperatureTargetText = (EditText) findViewById(R.id.editTextNumber);
        temperatureTargetText.clearFocus();


    }


}

