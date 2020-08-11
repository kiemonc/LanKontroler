package settingsActivity;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.InputStream;

import java.io.OutputStreamWriter;
import java.io.Serializable;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Przechowuje informacje o zapisanych ustwieniach aplikacji
 */
public class ApplicationConfig implements Serializable {

    private final String FILE_NAME = "config.txt";


    public ApplicationConfig(String ipAdress, double temperatureSteps, double hysteresis, String emailAdress) {
        this.hysteresis = hysteresis;
        this.ipAdress = ipAdress;
        this.temperatureSteps = temperatureSteps;
        this.emailAdress = emailAdress;
    }

    public String ipAdress;

    /**
     * Różnica w stopniach pomiędzy kolejnymi złączeniami faz
     */
    public double temperatureSteps;

    /**
     * Histereza działania pojedyńczej fazy
     */
    public double hysteresis;

    /**
     * Adres email na który ma być wysyłane archiwum
     */
    public String emailAdress;


    /**
     * Zapisuje ustawienia do pamieci telefonu
     */
    public void saveConfig(Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
            String data = "";
            data += "<ip>";
            data += ipAdress;
            data += "</ip>";
            data += "\n";
            data += "<his>";
            data += hysteresis;
            data += "</his>";
            data += "\n";
            data += "<diff>";
            data += temperatureSteps;
            data += "</diff>";
            data += "<email>";
            data += emailAdress;
            data += "</email>";
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Odczytuje dane z pamięci telefonu, jeśli nie ma zapisanych to ustawia domyślne
     */
        public void readConfigFromDisk(Context context) {
        String data = "";

        try {
            InputStream inputStream = context.openFileInput(FILE_NAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                data = stringBuilder.toString();
            }
            //ustawianie odczytanych wartości

            //adress ip
            Pattern patt = Pattern.compile("(?<=\\<ip\\>)(.*?)(?=\\<\\/ip\\>)");
            Matcher matcher = patt.matcher(data);
            if(matcher.find()) {
                ipAdress = matcher.group();
            }

            //histereza
            patt = Pattern.compile("(?<=\\<his\\>)(.*?)(?=\\<\\/his\\>)");
            matcher = patt.matcher(data);
            if(matcher.find()) {
                hysteresis = Double.parseDouble(matcher.group());
            }

            //różnica pomiędzy stopniani grzania
            patt = Pattern.compile("(?<=\\<diff\\>)(.*?)(?=\\<\\/diff\\>)");
            matcher = patt.matcher(data);
            if(matcher.find()) {
                temperatureSteps = Double.parseDouble(matcher.group());
            }

            //adres eamil
            patt = Pattern.compile("(?<=\\<email\\>)(.*?)(?=\\<\\/email\\>)");
            matcher = patt.matcher(data);
            if(matcher.find()) {
                emailAdress = matcher.group();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
    }




}


