package com.example.main;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Zapisuje co określony czas dane takiej jak: temperatura i stany przekaźników
 */
public class Archive {
    private String path;
    private String name;
    private static final String extension = ".csv";
    private static final String fileHeader = "Czas;Temperatura;Przekaznik 1;Przekaznik 2;Przekaznik 3\n";
    private String fullPath;

    /**
     * Konstruktor
     *
     * @param path ściezka bez kropki i rozszerzenia
     */
    public Archive(String path, String name) {
        this.path = path;
        this.name = name;

        fullPath = path+"/"+name+extension;
        ifNotExists();
    }

    public String getArchive() {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(fullPath));

            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                fileInputStream.close();
                return stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Zapisuje aktualny stan do pliku. Dopisuje ostatnia linijkę
     * @param tempereature
     * @param relaySate
     */
    public void update(String tempereature, boolean[] relaySate) {

        ifNotExists();

        Date nowDate= Calendar.getInstance().getTime();
        DateFormat nowFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = nowFormat.format(nowDate);
        String row = now+";";
        if(tempereature != null && relaySate != null) {
            row += tempereature.replace('.',',') + ";" + booleanToInt(relaySate[0]) + ";" + booleanToInt(relaySate[1]) + ";" + booleanToInt(relaySate[2]) + "\n";
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(fullPath), true);
                fileOutputStream.write(row.getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    /**
     * Zamienia boolean na inta
     * @param var zmienna boolean
     * @return true - 1, false - 0
     */
    private int booleanToInt(boolean var) {
        return var? 1 : 0;
    }


    /**
     * Zwraca całą ściężkę do pliku z archiwum
     * @return ścieżka z nazwą i rozszerzeniem
     */
    public String getFullPath() {
        return fullPath;
    }

    /**
     * Usuwa archiwum z pamięci
     */
    public void remove() {
        File file = new File(fullPath);
        file.delete();
    }

    /**
     * Sprawdza czy istnieje plik z archiwum i w razie potrzeby tworzy go i dodaje nagłówek
     */
    private void ifNotExists() {
        File file = new File(fullPath);
        if(!file.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(fullPath));
                fileOutputStream.write(fileHeader.getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    /**
     *
     * @return nazwa pliku archiwum
     */
    public String getFileName() {
        return name+extension;
    }
}
