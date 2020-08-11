package scheduleActivity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import lankontroller.Schedule;


/**
 * Pojedynczy wiersz w aktywności harmonogramów
 */
public class ScheduleRow {

    private Schedule schedule;
    private MyDateFormat myDateFormat;

    public ScheduleRow(Schedule schedule) {
        this.schedule = schedule;
        myDateFormat = new MyDateFormat(schedule);
    }

    /**
     * @return opis harmonogramu
     */
    public String getDescription() {
        return schedule.description;
    }

    /**
     *
     * @return true - harmonogram włączony; false - harmonogram wyłączony
     */
    public boolean getState() {
        return schedule.ifOn;
    }

    /**
     * Włacza lub wyłącza dany harmonogram
     * @param state true - włącz; false - wyłącz
     * @throws IOException
     */
    public void setState(boolean state) throws IOException {
        schedule.turnOn(state);
    }

    /**
     *
     * @return true - harmonogram włącza grzanie; false - harmonogram wyłacza grzanie
     */
    public boolean getEventValue() {
        return schedule.ifOnHeating;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    /**
     *
     * @return true - harmonogram włącza grzanie; fale - harmonogram wyłacza grzanie
     */
    public boolean getHeatingState() {
        return schedule.ifOnHeating;
    }


    public String getDate() {

        return myDateFormat.getDateString();
    }

    public String getTime() {
        return myDateFormat.getTimeString();
    }


}
