package lankontroller;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule {
    private static HttpRequest httpRequest;
    public boolean ifWeekdays;

    private static int numOfSchedules = 0;

    /**
     * Binarnie
     * 10 000 000 - każdy dzień
     * 01 000 000 - niedziela
     * 00 000 001 - poniedziałek
     */
    public int weekdays;

    /**
     * Sekundy od 0:00
     */
    public int time;

    /**
     * Dni po 1.1.1970
     */
    public int date;
    public String description;
    public boolean ifOn;
    public boolean ifOnHeating;
    public int id;

    public Schedule(int id, String description, boolean ifOn, boolean ifOnHeating, int time, int date, boolean ifWeekdays, int weekdays) {
        this.httpRequest = httpRequest;
        this.id = id;
        this.description = description;
        this.ifOn = ifOn;
        this.ifOnHeating = ifOnHeating;
        this.time = time;
        this.date = date;
        this.ifWeekdays = ifWeekdays;
        this.weekdays = weekdays;
    }



    /**
     * 10 to numer wyjścia wirtualnego EVENT1
     * @return s'id'='opis'*'nr wyjścia'*'wartość'*'czas'*''
     */
    @Override
    public String toString() {
        if(ifWeekdays) {
            return "s" + id + "=" + description + "*10*" + booleanToString(ifOnHeating) + "*" + time + "*0*" + weekdays + "*" + booleanToString(ifOn);
        } else {
            return "s" + id + "=" + description + "*10*" + booleanToString(ifOnHeating) + "*" + time + "*" + date + "*0*" + booleanToString(ifOn);
        }
    }

    /**
     * @param var zmienna logiczna
     * @return true - 1; flase - 0
     */
    private static String booleanToString(boolean var) {
        return var? "1" : "0";
    }

    /**
     * Zamienia stringa na boolean
     * @param text
     * @return
     */
    private static boolean stringToBoolean(String text) {
        if(text.equals("0")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Ustawia httpRequest wspólny dla każdego harmonogramu
     * @param httpRequest
     */
    public static void setHttpRequest(HttpRequest httpRequest) {
        Schedule.httpRequest = httpRequest;
    }

    /**
     * Wysyła listę harmonogramów do LK3
     * @param schedules list harmonogramów
     * @throws IOException
     */
    public static void send(List<Schedule> schedules) throws IOException {
        String schedulesString = "";
        int i = 0;
        for(Schedule schedule : schedules) {
            schedule.id = i;
            schedulesString += "&";
            schedulesString += schedule;

            i++;
        }

        httpRequest.post("/post.cgi?sched_save","{\"bytes\":{"+schedulesString+"&&}}");
        numOfSchedules = schedules.size();
    }

    /**
     * Włącza lub wyłącza dany harmonogram
     * @param ifOn true - włącz; false - wyłącz
     * @throws IOException
     */
    public void turnOn(boolean ifOn) throws IOException {
        httpRequest.postFrame("/inpa.cgi?schedon="+id+"*"+booleanToString(ifOn));
        this.ifOn = ifOn;
    }

    /**
     * Komunikuje się z LK3 i odzcytuje listę harmonogramów
     * @return lista harmonogramów
     */
    public static List<Schedule> getSchedules() throws IOException {
        List<Schedule> schedules = new LinkedList<>();

        String response = httpRequest.getResponse("/xml/sched.xml");
        Pattern patt = Pattern.compile("(?<=\\<sched)(.*?)(?=<\\/sched\\d*\\>)");
        Matcher matcher = patt.matcher(response);
        while(matcher.find()) {
            String founded = matcher.group();
            if(founded.contains("0*0**0*0*0*0*0")) {
                continue;
            }
            Schedule newSchedule = createScheduleFromString(founded);
            if(newSchedule != null) {
                schedules.add(newSchedule);
            }
        }
        numOfSchedules = schedules.size();
        return schedules;
    }


    /**
     * Tworzy obiekt schedule na podstawie odpowiedzi z LK3 w postaci:
     * 'id'>'czy istnieje'*'ifOn'*'description'*'wyjście'*'ifOnHeating'*'time'*'date'*'week days'
     * wyjscie 10 to EVENT1
     * time sekundy od 0:00
     * data to dni po 1.1.1970
     * Jeśli data jest 0 to brane są pod uwage dni tygodnia
     * Dni tygodnia to binarnie zapisany int
     * @param scheduleString opisane wyżej
     */
    private static Schedule createScheduleFromString(String scheduleString) {
        int id = -1;
        boolean ifExists = false;
        boolean ifOn = false;
        String description = "";
        boolean ifOnHeating = false;
        int time = 0;
        int date = 0;
        boolean ifWeekDays = false;
        int weekDays = 0;
        Pattern patt = Pattern.compile("(?<=\\*|\\>)(.*?)(?=\\*)|\\d{1,}");
        Matcher matcher = patt.matcher(scheduleString);
        int i = 0;
        while (matcher.find()) {
            switch (i) {
                case 0:
                    id = Integer.parseInt(matcher.group());
                    break;
                case 1:
                    ifExists = stringToBoolean(matcher.group());
                    if(!ifExists) {
                        return null;
                    }
                    break;
                case 2:
                    ifOn = stringToBoolean(matcher.group());
                    break;
                case 3:
                    description = matcher.group();
                    break;
                case 4:
                    //wyjście, ale nie używam bo jest zawsze 10
                    break;
                case 5:
                    ifOnHeating = stringToBoolean(matcher.group());
                    break;
                case 6:
                    time = Integer.parseInt(matcher.group());
                    break;
                case 7:
                    date = Integer.parseInt(matcher.group());
                    break;
                case 8:
                    weekDays = Integer.parseInt(matcher.group());
                    break;
                default:
            }
            i++;
        }
        ifWeekDays = (date == 0);

        if (ifExists) {
            return new Schedule(id, description, ifOn, ifOnHeating, time, date, ifWeekDays, weekDays);
        } else {
            return null;
        }
    }

    public static int getNumOfSchedules() {
        return numOfSchedules;
    }

    public static void setNumOfSchedules(int value) {
        numOfSchedules = value;
    }


}
