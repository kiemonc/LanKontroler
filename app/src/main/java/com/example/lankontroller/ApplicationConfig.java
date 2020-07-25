package com.example.lankontroller;

public class ApplicationConfig {

    public String ipAdress;

    /**
     * Różnica w stopniach pomiędzy kolejnymi złączeniami faz
     */
    public int temperatureSteps;

    /**
     * Histereza działania pojedyńczej fazy
     */
    public int hysteresis;


    /**
     * Zapisuje ustawienia do pamieci telefonu
     */
    public void saveConfig() {

    }

    /**
     * Odczytuje dane z pamięci telefonu, jeśli nie ma zapisanych to ustawia domyślne
     */
    public void readConfigFromDisk() {

    }

}
