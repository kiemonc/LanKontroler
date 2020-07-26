package com.example.lankontroller;

public class ApplicationConfig {

    public ApplicationConfig(String ipAdress, int temperatureSteps, double hysteresis) {
        this.hysteresis = hysteresis;
        this.ipAdress = ipAdress;
        this.temperatureSteps = temperatureSteps;

    }

    public String ipAdress;

    /**
     * Różnica w stopniach pomiędzy kolejnymi złączeniami faz
     */
    public int temperatureSteps;

    /**
     * Histereza działania pojedyńczej fazy
     */
    public double hysteresis;


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
