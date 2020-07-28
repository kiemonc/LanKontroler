package com.example.lankontroller;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import LanKontrollerComunication.LanKontroller;

public class MainActivity extends AppCompatActivity {

    private LanKontroller lk;
    private int number;
    private String unknown;
    private final int refreshTime = 10000;
    private boolean connected;
    private int backButtonCount;
    private ApplicationConfig config;
    private final static int SAVE_SETTINGS_CODE = 1;
    Thread refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        config = new ApplicationConfig("192.168.1.100", 5.0, 0.2);
        config.readConfigFromDisk(getApplicationContext());

        lk = new LanKontroller(config.ipAdress, config.temperatureSteps, config.hysteresis);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unknown = getString(R.string.unknown);

        backButtonCount = 0;

        connected = false;



        //wyłączenie rotacji ekranu
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //zapytania http można robić tylko w oddielnym wątku, a aktualizować tekst można aktualizować tylko w wątku głównym
        refreshing = new Thread() {

            @Override
            public void run() {
                try {

                    while (!isInterrupted()) {

                        //sprawdzanie temperatury i stanu przekaźników i stanu grzania
                        String currentTemperatureTemp;
                        boolean[] stateTemp;
                        double targetTemperatureTemp;
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
                        final double targetTemperature = targetTemperatureTemp;
                        final boolean heatingState = heatingStateTemp;
                        runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void run() {
                                //połączenie
                                if(!connected) {
                                    //alert na szarym polu
                                    //runOnUiThread(() -> Toast.makeText(MainActivity.this, "Brak połączenia z LK", Toast.LENGTH_SHORT).show());

                                    //wyświetla tekst z informacją
                                    TextView notConnected = (TextView) findViewById(R.id.connectionState);
                                    notConnected.setVisibility(View.VISIBLE);
                                } else {
                                    //Ukrywa tekst z informacją
                                    TextView connected = (TextView) findViewById(R.id.connectionState);
                                    connected.setVisibility(View.INVISIBLE);
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
                                            stateText.setBackgroundColor(getColor(R.color.on));
                                        } else {
                                            stateText.setText(getString(R.string.off));
                                            stateText.setBackgroundColor(getColor(R.color.off));
                                        }
                                    }
                                    i++;
                                }
                                //zadana temperatura
                                EditText targetTemperatureText = (EditText) findViewById(R.id.editTextNumber);
                                if(!targetTemperatureText.hasFocus()) {
                                    targetTemperatureText.setText(Double.toString(targetTemperature));
                                }

                                //przyciski
                                Button buttonOn = (Button) findViewById(R.id.buttonOn);
                                Button buttonOff = (Button) findViewById(R.id.buttonOff);
                                if(connected) {
                                    if (heatingState) {
                                        buttonOn.setBackgroundColor(getColor(R.color.on));
                                        buttonOff.setBackgroundResource(android.R.drawable.btn_default);
                                    } else {
                                        buttonOn.setBackgroundResource(android.R.drawable.btn_default);
                                        buttonOff.setBackgroundColor(getColor(R.color.off));
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
                        double temperature = 0;
                        try {
                            temperature = Double.parseDouble(temperatureField.getText().toString());
                        } catch(NumberFormatException e) {

                        }
                        if(temperature > 20 && temperature < 70) {
                            try {
                                lk.turnOnHeating(temperature);
                            } catch (IOException e) {

                            }
                        } else {
                            UserDialog exampleDialog = new UserDialog(getString(R.string.information), getString(R.string.bad_format));
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

        if(temperatureTargetText.hasFocus() == false) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            temperatureTargetText.clearFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings,menu);
        return true;
    }


    /**
     * Obsługa przycisku ustawień. Otwiera okno ustwień po kliknięciu w przycisk
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.settingsItem:
                 Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                 settingsIntent.putExtra("config", (Serializable) config);
                 startActivityForResult(settingsIntent,1);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_SETTINGS_CODE) {
            config = (ApplicationConfig) data.getExtras().getSerializable("config");
            config.saveConfig(getApplicationContext());

            //załadowanie nowych ustawień do Lan Kontrolera
            lk = new LanKontroller(config.ipAdress, config.temperatureSteps, config.hysteresis);
        }
    }

}

