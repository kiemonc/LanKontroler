package scheduleActivity;

import android.content.res.Resources;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import lankontroller.Schedule;

public class MyDateFormat {

    private Schedule schedule;
    private Resources res;
    private String  weekDaysString [] = {"ndz","pon","wt","śr","czw","pt","sob"};

    public MyDateFormat(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Konwertuje sekundy po północy na godziny i minuty
     * @return godzina
     */
    public String getTimeString() {
        return getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString();
    }


    /**
     *
     * @return czas harmonogarmu
     */
    public LocalTime getTime() {
        int secondsAfterMidnight = schedule.time;
        LocalTime localTime = LocalTime.ofSecondOfDay(secondsAfterMidnight);
        return localTime;
    }

    /**
     * Konwertuje dni po 1970.1.1 na date lub wartośc binarna dni tygodnia na jednego stringa
     * @return data lub dni tygodnia
     */
    public String getDateString() {
        String result = "";
        if(schedule.ifWeekdays) {
            List<Integer> weekDays = getWeekdays();
            if(weekDays.size() == 7) {
                return "Codziennie";
            } else {
                for(Integer weekday : weekDays) {
                    result += weekDaysString[weekday-1];
                    result += " ";
                }
            }
        } else {
            result = getDate().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")).toString();
        }
        return result;
    }

    public List<Integer> getWeekdays() {
        List<Integer> weekdaysList = new ArrayList<>();
        int weekDays = schedule.weekdays;
        if((weekDays & 10000000) != 0) {
            weekdaysList.addAll(Arrays.asList(Calendar.MONDAY,Calendar.TUESDAY,Calendar.WEDNESDAY,Calendar.THURSDAY,Calendar.FRIDAY,Calendar.SATURDAY,Calendar.SUNDAY));
        } else {
            int compareNumber = 1;
            for(int i = 0; i < 7; i++) {
                if((compareNumber & weekDays) != 0) {
                    switch (i) {
                        case 0:
                            weekdaysList.add(Calendar.MONDAY);
                            break;
                        case 1:
                            weekdaysList.add(Calendar.TUESDAY);
                            break;
                        case 2:
                            weekdaysList.add(Calendar.WEDNESDAY);
                            break;
                        case 3:
                            weekdaysList.add(Calendar.THURSDAY);
                            break;
                        case 4:
                            weekdaysList.add(Calendar.FRIDAY);
                            break;
                        case 5:
                            weekdaysList.add(Calendar.SATURDAY);
                            break;
                        case 6:
                            weekdaysList.add(Calendar.SUNDAY);
                            break;
                    }
                }
                compareNumber = compareNumber << 1;
            }
        }
        return weekdaysList;
    }


    /**
     *
     * @return data harmonogramu
     */
    public LocalDate getDate() {
        int daysAfterEpoch = schedule.date;
        LocalDate epochDate = LocalDate.of(1970,1,1);
        LocalDate date = epochDate.plusDays(daysAfterEpoch);
        return date;
    }

    public static int weekdaysToInt(List<Integer> weekdays) {
        int result = 0;
        for(Integer day : weekdays) {
            switch (day) {
                case Calendar.MONDAY:
                    result += 1;
                    break;
                case Calendar.TUESDAY:
                    result += 2;
                    break;
                case Calendar.WEDNESDAY:
                    result += 4;
                    break;
                case Calendar.THURSDAY:
                    result += 8;
                    break;
                case Calendar.FRIDAY:
                    result += 16;
                    break;
                case Calendar.SATURDAY:
                    result += 32;
                    break;
                case Calendar.SUNDAY:
                    result += 64;
                    break;
            }
        }
        return result;
    }
}
