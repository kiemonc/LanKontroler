package lankontroller;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Zamodelowany LK
 */

public class LanKontroller implements Serializable {
	private static final int SCHED_EVE_NUM = 4;
	private static final int TERMOMETER_GOOD_STATE_EVE_NUM = 3;

	//najwyższa temperatura zadana
	public static final int FIRST_HEATING_STEP_EVE_NUM = 0;
	public static final int SECOND_HEATING_STEP_EVE_NUM = 1;
	public static final int THIRD_HEATING_STEP_EVE_NUM = 2;

	//stany grzania
	public static final int HEATING_OFF = 0;
	public static final int HEATING_ON = 1;
	public static final int HEATING_AUTO_OFF = 2;
	public static final int HEATING_AUTO_ON = 3;
	public static final int HEATING_AUTO = 4;
	public static final int HEATING_UNKNOWN_STATE = -1;

	public static final String TEMP_SCHEDULE_DESRCIPTION = "tempschedule123";

	//numer EVENTÓW
	/**
	 * grzenia bez harmonogramu
	 */
	private static final int EVENT5_NUM = 64;
	/**
	 * grzanie z harmonogramem
	 */
	private static final int EVENT6_NUM = 65;


	private HttpRequest httpRequest;
	private Termometer termometer;


	/**
	 * Różnica w stopniach pomiędzy kolejnymi złączeniami faz
	 */
	private double temperatureSteps;

	/**
	 * Histereza działania pojedyńczej fazy
	 */
	private double hysteresis;

	/**
	 *
	 * @param ipAdress adres IP z kropkami
	 */
	public LanKontroller(String ipAdress,double temperatureSteps, double hysteresis) {
		this.httpRequest = new HttpRequest(ipAdress);
		this.termometer = new Termometer(httpRequest);
		Event.setHttpRequest(httpRequest);
		Schedule.setHttpRequest(httpRequest);
		this.temperatureSteps = temperatureSteps;
		this.hysteresis = hysteresis;


	}

	/**
	 * Wyłącza wszystkie zdarzenia oraz wszystkie wyjścia LK
	 * @throws IOException
	 */
	public void turnOffHeating() throws IOException {
		Event.removeEvents();
		httpRequest.postFrame("/outs.cgi?out1=0&out2=0&out3=0");
	}

	/**
	 * Pobiera wartośc tempmeratury z termometru
	 * @return Temperatura w stopniach Celsjusza
	 * @throws IOException
	 */
	public double getTemperature() throws IOException {
		return termometer.getTemperature();
	}

	/**
	 * Włącza grzanie, wysyła i włącza zdarzenia oraz ustawia je na permanetne, przez co nie można sterować wyjściami w inny sposób jak tylko przez zdarzenia
	 * Losowo przydziela fazy (wyjścia) tak aby były one wykorzystane równomiernie
	 * param temperature zadana temperatura
	 * @throws IOException
	 */
	public void turnOnHeating(double temperature, boolean onSchedule) throws IOException {
		Event.zeroNumber();
		List<Event> events = new ArrayList<>();
		events.add(new Event(temperature, hysteresis, onSchedule));
		events.add(new Event(temperature-temperatureSteps, hysteresis, onSchedule));
		events.add(new Event(temperature-temperatureSteps*2, hysteresis, onSchedule));


		Random random = new Random();

		int [] number = new int[3];
		number[0] = random.nextInt(3);


		if(random.nextBoolean()) {
			number[1] = 0;
			number[2] = 1;
		} else {
			number[1] = 1;
			number[2] = 0;
		}

		if(number[0]==0) {
			number[1]++;
			number[2]++;
		} else if(number[0]==1 && number[1]==1) {
			number[1]++;
		} else if(number[0]==1 && number[2]==1) {
			number[2]++;
		}

		for(int i = 0; i < 3; i++) {
			number[i]++;
			events.get(i).setOut(number[i]);
		}

		for(Event event : events) {
			event.uploadEvent();
		}

		//Włacza zdarzenia, bo po dodaniu są one wyłączone. Wszystkie razem ponieważ mniej obciążany jest LK3
		Event.turnOnEvents();
		
	}

	/**
	 * Zwraca stan trzech wyjśc LK 1,2,3
	 * Kolejne cyfry są odpowiedzialne za dane wyjście
	 * 101 - wyj 1 włączone; wyj 2 wyłączone; wyj 3 włączone
	 * @return wartośc opisująca stan wyjść
	 * @throws IOException
	 */
	public boolean[] getRelayState() throws IOException {
		String state = httpRequest.getResponse("/inpa.cgi");
		Pattern patt = Pattern.compile("(?<=\\<out\\>\\d)(.*?)(?=\\d\\d\\<\\/out\\>)");
		Matcher matcher = patt.matcher(state);
		int number = 0;
		if(matcher.find()) {
			number = Integer.parseInt(matcher.group());
		}
		boolean [] stateTab = new boolean[3];
		for(int i = 0; i < 3; i++) {
			if (number % 10 == 1) {
				stateTab[2-i] = true;
			} else {
				stateTab[2-i] = false;
			}
			number /= 10;
		}
		return stateTab;
	}


	/**
	 * sprawdza czy wszystkie zdarzenia są włączone i jeśli tak to zwraca, że ogrzewanie jest włączone w innych przypadkach zwraca, że ogrzewanie jest wyłączone
	 * @return stan grzania
	 * @throws IOException
	 */
	public int getHeatingState() throws IOException {
		String stateString = httpRequest.getResponse("/xml/eve2.xml");
		int[] eventStates = new int[3];
		for(int i = 0; i < 3; i++) {
			Pattern patt = Pattern.compile("(?<=\\<ev"+Integer.toString(i)+"\\>\\d\\*)(\\d)");
			Matcher matcher = patt.matcher(stateString);
			if(matcher.find()) {
				eventStates[i] = Integer.parseInt(matcher.group());
			}
		}
		boolean state;
		if(eventStates[0] == 1 && eventStates[1] == 1 && eventStates[2] == 1) {
			state = true;
		} else {
			state = false;
		}

		if(!state) {
			//wiadomo, że jest wyłączone
			return LanKontroller.HEATING_OFF;
		} else {
			//jest wlączone
			int[] eventNumber = new int[3];
			for(int i = 0; i < 3; i++) {
				Pattern patt = Pattern.compile("(?=\\d*\\*\\d*\\*\\d*\\*\\d*\\*\\d\\<\\/ev1\\>)\\d{1,}");
				Matcher matcher = patt.matcher(stateString);
				if(matcher.find()) {
					eventNumber[i] = Integer.parseInt(matcher.group());
				}
			}

			String eventState = httpRequest.getResponse("/xml/eve.xml");

			//jeśli wszystkie EVENTY mają wartość 64 to grzanie odbywa się bez harmonogramu
			if(eventNumber[0] == LanKontroller.EVENT5_NUM && eventNumber[1] == LanKontroller.EVENT5_NUM && eventNumber[2] == LanKontroller.EVENT5_NUM){
				//grzanie bez harmonogramu
				if(eventState.contains("<eve3>10</eve3>")) {
					return LanKontroller.HEATING_ON;
				} else if(eventState.contains("<eve3>00</eve3>")) {
					return  LanKontroller.HEATING_OFF;
				} else {
					return LanKontroller.HEATING_UNKNOWN_STATE;
				}
			} else if(eventNumber[0] == LanKontroller.EVENT6_NUM && eventNumber[1] == LanKontroller.EVENT6_NUM && eventNumber[2] == LanKontroller.EVENT6_NUM) {
				//grznie z harmonogramem
				//sprawdzanie czy warunki harmonogramu są spełnione
				if(eventState.contains("<eve4>11</eve4>")) {
					return LanKontroller.HEATING_AUTO_ON;
				} else if(eventState.contains("<eve4>00</eve4>") || eventState.contains("<eve4>10</eve4>") || eventState.contains("<eve4>01</eve4>")) {
					return LanKontroller.HEATING_AUTO_OFF;
				} else {
					return LanKontroller.HEATING_UNKNOWN_STATE;
				}
			} else {
				return LanKontroller.HEATING_UNKNOWN_STATE;
			}
		}
	}

	/**
	 * Sprawdza zadaną temperature w zdarzeniach. Sprawdza tylko zdarzenie 0 ponieważ, ono ma zawsze maksymalną temperaturę
	 * @return zadana temperatura
	 * @throws IOException
	 */
	public double getTargetTemperature() throws IOException {
		String stateString = httpRequest.getResponse("/xml/eve2.xml");
		Pattern patt = Pattern.compile("(?<=\\<ev"+FIRST_HEATING_STEP_EVE_NUM+"\\>\\d\\*\\d\\*\\d\\d\\*\\d\\*)(\\d{1,})");
		Matcher matcher = patt.matcher(stateString);
		double temperature = 0;
		if(matcher.find()) {
			temperature = Double.parseDouble(matcher.group())/100.0;
		}
		return temperature;
	}

	/**
	 * Zadaje i włącza zdarzenia odpowiedzalne za sprawdzanie poprawności działania termometru oraz zdarzenia odpowiedzialnego za działanie schedulera
	 * @throws IOException
	 */
	public void setGoodStatusAndSchedEvents() throws IOException {
		//dodanie zdarzeń
		String request = "/inpa.cgi?event="+TERMOMETER_GOOD_STATE_EVE_NUM+"*11*0*100*0*0*31*65*0*1*100&event="+SCHED_EVE_NUM+"*11*0*100*0*1*33*54*0*1*100";
		httpRequest.postFrame(request);

		//włączenie zdarzeń
		request = "/inpa.cgi?eventon="+TERMOMETER_GOOD_STATE_EVE_NUM+"*1&eventon="+SCHED_EVE_NUM+"*1&eventper="+TERMOMETER_GOOD_STATE_EVE_NUM+"*1&eventper="+SCHED_EVE_NUM+"*1";
		httpRequest.postFrame(request);
	}

	/**
	 * Sprawdza czy LK ma dodane zdarzenia odpowiedzalne za sprawdzanie stanu termometru oraz schedulera
	 * @return czy stan jest ok
	 * @throws IOException
	 */
	public boolean checkStatusAndSchedEvetns() throws IOException {
		String response = httpRequest.getResponse("/xml/eve2.xml");
		if(response.contains("1*1*11*0*100*0*0*31*65*0*1*100*1") && response.contains("1*1*11*0*100*0*1*33*54*0*1*100*1")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @return list harmonogramów zapisanych w LK3
	 * @throws IOException
	 */
	public List<Schedule> getschedules() throws IOException {
		return Schedule.getSchedules();
	}


	@RequiresApi(api = Build.VERSION_CODES.O)
	public void updateTime() throws IOException {
		LocalDateTime localDateTime = LocalDateTime.now();
		long time = localDateTime.toEpochSecond(ZoneOffset.UTC);
		httpRequest.postFrame("/stm.cgi?t_man="+time);
	}


	/**
	 * @return data zapisana w LK3; null kiedy nie znaleziono
	 * @throws IOException
	 */
	public Date getTime() throws IOException {
		String response = httpRequest.getResponse("/xml/co.xml");
		Pattern patt = Pattern.compile("(?<=\\<time\\>)(.*?)(?=<\\/time\\>)");
		Matcher matcher = patt.matcher(response);
		if (matcher.find()) {
			return new Date(Long.parseLong(matcher.group())*1000);
		}
		return null;
	}


	/**
	 * Włącza grzanie gdy harmonogram jest załączony
	 * Dodaje harmonogram włączenia lub wyłączenia grzania o aktualnej godzinie
	 * @param ifOnHeatingOnSchedule true - włacz grzanie; false - wyłącz grzanie
	 */
	public void turnOnHeatingOnSchedule(boolean ifOnHeatingOnSchedule) throws IOException {
		List<Schedule> scheduleList = Schedule.getSchedules();
		int id = Schedule.getNumOfSchedules();
		for(Schedule schedule : scheduleList) {
			if(schedule.description.equals(LanKontroller.TEMP_SCHEDULE_DESRCIPTION)) {
				id = schedule.id;
			}
		}

		if(id < scheduleList.size()) {
			scheduleList.remove(id);
		}

		LocalDateTime dateTime = LocalDateTime.now();
		dateTime = dateTime.plusSeconds(5);
		LocalTime localTime = dateTime.toLocalTime();
		LocalDate localDate = dateTime.toLocalDate();
		Schedule newSchedule = new Schedule(id,LanKontroller.TEMP_SCHEDULE_DESRCIPTION,true,ifOnHeatingOnSchedule, localTime.toSecondOfDay(), (int) localDate.toEpochDay(),false,0);
		scheduleList.add(newSchedule);
		Schedule.send(scheduleList);
	}

}
