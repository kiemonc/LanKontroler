package com.example.lankontroller;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import LanKontrollerComunication.LanKontroller;

public class MainActivity extends AppCompatActivity {

    private LanKontroller lk;
    private Archive archive;
    private int number;
    private String unknown;
    private final int refreshTime = 10000;
    private boolean connected;
    private int backButtonCount;
    private ApplicationConfig config;
    private final static int SAVE_SETTINGS_CODE = 1;
    private Thread refreshing;
    private String currentTemperature;
    private boolean[] state;
    private double targetTemperature;
    private boolean heatingState;
    private ProgressBar progressBar;
    private boolean appHeatingState;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        config = new ApplicationConfig("192.168.1.100", 5.0, 0.2, "mikolajchmielecki2000@gmail.com");
        config.readConfigFromDisk(getApplicationContext());

        lk = new LanKontroller(config.ipAdress, config.temperatureSteps, config.hysteresis);

        //ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        unknown = getString(R.string.unknown);

        backButtonCount = 0;

        connected = false;

        archive = new Archive(Environment.getExternalStorageDirectory().toString(),"archive");

        //kółko ładowania
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        //wyłączenie rotacji ekranu
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //jedno odświeżenie stanu LK3
        new RefreshingTask().execute("");
        appHeatingState = heatingState;


        //zapytania http można robić tylko w oddielnym wątku, a aktualizować tekst można aktualizować tylko w wątku głównym
        refreshing = new Thread() {

            @Override
            public void run() {
                try {

                    while (!isInterrupted()) {
                        new RefreshingTask().execute("");
                        Thread.sleep(refreshTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        refreshing.start();


        //porzyciski w zaleźności od stanu ogrzewania zmieniają kolor, ale dopiero po sprawdzeniu stanu ogrzewania w LK

        //przycisk wyłączania ogrzewania
        final Button buttonOff = findViewById(R.id.buttonOff);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //potrzebne do wyświetlania kółka ładowania
                if(appHeatingState == true) {
                    appHeatingState = false;
                }

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            setProgressBarVisibilityOnUI();
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

                //potrzebne do wyświetlania kółka ładowania
                if(appHeatingState == false) {
                    appHeatingState = true;
                }

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        EditText temperatureField= (EditText) findViewById(R.id.editTextNumber);
                        double temperature = 0;
                        try {
                            temperature = Double.parseDouble(temperatureField.getText().toString());
                        } catch(NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if(temperature > 20 && temperature < 70) {

                            try {
                                setProgressBarVisibilityOnUI();
                                //ustawianie temperatury
                                lk.turnOnHeating(temperature, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            UserDialog exampleDialog = new UserDialog(getString(R.string.information), getString(R.string.bad_format_temperature));
                            exampleDialog.show(getSupportFragmentManager(), "poza zakresem");
                        }
                    }
                };
                thread.start();
            }
        });

        //przycisk wysyłania maila
        final Button send = (Button) this.findViewById(R.id.emailButton);
        send.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new SendingEmail().execute("");

            }
        });

        //przycisk usuwania archiwum
        final Button remove = (Button) this.findViewById(R.id.removeArchive);
        remove.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                archive.remove();
                updateArchiveSizeValue();
            }
        });


    }

    @Override
    public void onBackPressed() {


        //wyłączenie focusu na edytowalnym tekscie po kliknieciu przycisku wstecz
        EditText temperatureTargetText = (EditText) findViewById(R.id.editTextNumber);

        if(!temperatureTargetText.hasFocus()) {
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
     * @param item przycisk ustwień
     * @return nie wiem
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


    private class SendingEmail extends AsyncTask<String, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            try {
                Email email = new Email("grzalkachmielecki@gmail.com", "oiklop12345");
                email.send(getString(R.string.email_subject), getString(R.string.email_body),  config.emailAdress, archive.getFullPath(), archive.getFileName());

                //wyświetlanie komunikatu o powodzeniu
                UserDialog exampleDialog = new UserDialog(getString(R.string.information), getString(R.string.sending_succesfull));
                exampleDialog.show(getSupportFragmentManager(), "powodzenie wysyłania maila");
            } catch (MessagingException e) {
                //wyświetlanie komunikatu o niepowoadzeniu
                UserDialog exampleDialog = new UserDialog(getString(R.string.information), getString(R.string.sending_failed));
                exampleDialog.show(getSupportFragmentManager(), "błąd wysyłania maila");
            }
            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Do things like hide the progress bar or change a TextView
        }
    }

    private class RefreshingTask extends AsyncTask<String, Integer, String> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            //sprawdzanie czy LK3 ma dodane zdarzenia odpowiadzialne za dobry stan termometru oraz scheduler
            try {
                if (!lk.checkStatusAndSchedEvetns()) {
                    lk.setGoodStatusAndSchedEvents();
                }
                currentTemperature = Double.toString(lk.getTemperature());
                state = lk.getRelayState();
                targetTemperature = lk.getTargetTemperature();
                heatingState = lk.getHeatingState();
                connected = true;
            } catch (IOException e) {
                currentTemperature = unknown;
                state = null;
                targetTemperature = -1;
                heatingState = false;
                connected = false;
            }


            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    if(connected) {
                        refresh();
                    } else {
                        setUnknownState();
                    }
                }
            });
        }
    }



    /**
     * Reagowanie na rezultat okna z przyznawaniem uprawnień
     * Wychodzi z aplikacji jeśli uprawnienia nie zostały przydzielone
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
            }
        }


    /**
     * Ustawia wygląd okna. Zmienia kolory i wyświetla komunikat o braku połączenia z LK3
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setUnknownState() {
        //wyświetla tekst z informacją
        TextView notConnected = (TextView) findViewById(R.id.connectionState);
        notConnected.setVisibility(View.VISIBLE);

        //temperatura
        TextView temperature = (TextView) findViewById(R.id.temperatureValue);
        temperature.setText(getString(R.string.unknown));

        //przekaźniki
        List<TextView> stateTextList = new ArrayList<>();
        stateTextList.add((TextView) findViewById(R.id.state0));
        stateTextList.add((TextView) findViewById(R.id.state1));
        stateTextList.add((TextView) findViewById(R.id.state2));
        for(TextView stateText : stateTextList) {
            stateText.setText(getString(R.string.unknown));
            stateText.setBackgroundColor(getColor(R.color.unknown));
        }

        //przyciski
        Button buttonOn = (Button) findViewById(R.id.buttonOn);
        Button buttonOff = (Button) findViewById(R.id.buttonOff);
        buttonOn.setBackgroundTintList(getColorStateList(R.color.btn_normal));
        buttonOff.setBackgroundTintList(getColorStateList(R.color.btn_normal));

    }

    /**
     * Odświeża stan okna
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void refresh() {



        //komunikat o połączeniu
        TextView connectionState = (TextView) findViewById(R.id.connectionState);
        connectionState.setVisibility(View.INVISIBLE);


        //temperatura
        TextView temperature = (TextView) findViewById(R.id.temperatureValue);
        temperature.setText(currentTemperature);

        //plik z archiwum
        archive.update(currentTemperature, state);

        //przekaźniki
        List<TextView> stateTextList = new ArrayList<>();
        stateTextList.add((TextView) findViewById(R.id.state0));
        stateTextList.add((TextView) findViewById(R.id.state1));
        stateTextList.add((TextView) findViewById(R.id.state2));
        int i = 0;
        for(TextView stateText : stateTextList) {
            if(state != null && state[i]) {
                stateText.setText(getString(R.string.on));
                stateText.setBackgroundColor(getColor(R.color.on));
            } else {
                stateText.setText(getString(R.string.off));
                stateText.setBackgroundColor(getColor(R.color.off));
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
        if (heatingState) {
            buttonOn.setBackgroundTintList(getColorStateList(R.color.btn_on));
            buttonOff.setBackgroundTintList(getColorStateList(R.color.btn_normal));
        } else {
            buttonOn.setBackgroundTintList(getColorStateList(R.color.btn_normal));
            buttonOff.setBackgroundTintList(getColorStateList(R.color.btn_off));
        }

        //kółko ładowania
        if(appHeatingState == heatingState && progressBar.getVisibility() != View.INVISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        //rozmiar archiwum
        updateArchiveSizeValue();

    }

    /**
     * Wyświetla kółko ładowania
     */
    private void setProgressBarVisibilityOnUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Aktualizuje wyświetlany rozmiar pliku archiwum
     */
    private void updateArchiveSizeValue() {
        File archiveFile = new File(archive.getFullPath());
        TextView archiveSize = (TextView) findViewById(R.id.archieSizeValue);
        if(archiveFile.exists()) {
            long size = archiveFile.length();
            archiveSize.setText(" " + bytesToString(size));
        } else {
            archiveSize.setText(" 0 B");
        }
    }

    private String bytesToString(long size) {
        String value;
        if(size<1000) {
            return size + " B";
        } else if(size<1000000) {
            return size/1000.0 + " KB";
        } else {
            return size/1000000.0 + " MB";
        }
    }

}

