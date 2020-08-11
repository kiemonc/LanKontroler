package lankontroller;

import java.io.IOException;

/**
 * Zdarzenie pamiętane przez LK. Tworzy, uplouduje, usuwa i pozycjonyje zdarzenia w LK
 */
public class Event {
	private double value;
	private double histeresis;
	private int out;
	private static HttpRequest httpRequest;
	private static int number = 0;
	private boolean onSchedule;

	//najwyższa temperatura zadana
	public static final int FIRST_HEATING_STEP_EVE_NUM = LanKontroller.FIRST_HEATING_STEP_EVE_NUM;
	public static final int SECOND_HEATING_STEP_EVE_NUM = LanKontroller.SECOND_HEATING_STEP_EVE_NUM;
	public static final int THIRD_HEATING_STEP_EVE_NUM = LanKontroller.THIRD_HEATING_STEP_EVE_NUM;
	
	public Event(int out, double value, double histeresis, boolean onSchedule) {
		this.out = out;
		this.histeresis = histeresis;
		this.value = value;
		this.onSchedule = onSchedule;
	}
	
	public Event(double value, double histeresis, boolean onSchedule) {
		this(0,value,histeresis,onSchedule);
	}

	/**
	 * zeruje statyczną zmienną number, w celu dodawania zdarzeń od zerowego miejsca
	 */
	public static void zeroNumber() {
		number = 0;
	}
	/**
	 * Ustawia nr wyjścia LK, którym steruje dane zdarzenie
	 * @param out wyjście
	 */
	public void setOut(int out) {
		this.out = out;
	}

	/**
	 * Ustawia referencje do httpRequest, który wysyła zdarzenia do LK
	 * @param httpRequest
	 */
	public static void setHttpRequest(HttpRequest httpRequest) {
		Event.httpRequest = httpRequest;
	}

	/**
	 * Wyłącza 3 pierwsze zdarzenia zapisane w LK
	 * @throws IOException
	 */
	public static void removeEvents() throws IOException {
		/*
		String request = "/inpa.cgi?";
		for(int i = 0; i < 3; i++) {
			request += "eventon=";
			request += Integer.toString(i);
			request += "*0";
			if(i != 2) {
				request += "&";
			}
		}
		httpRequest.postFrame(request);
		 */
		String request = "/inpa.cgi?eventon=0*0&eventon=1*0&eventon=2*0";
		Event.httpRequest.postFrame(request);
		Event.zeroNumber();
	}

	/**
	 * Wysyła zdarzenie w LK
	 * nr zdarzenia*?*wartośc*?histereza*?*wyjście*?...
	 * Wyjście 0 -> 0 wyłączone : 1 włączone
	 * Wyjście 1 -> 1 wyłączone : 2 włączone
	 * ...
	 * @throws IOException
	 */
	public void uploadEvent() throws IOException {
		//TODO połączenie zapytań http, zawiesza się lk
		String request = "/inpa.cgi?event=";
		switch(number) {
			case 0 :
				request += Integer.toString(FIRST_HEATING_STEP_EVE_NUM);
				break;
			case 1 :
				request += Integer.toString(SECOND_HEATING_STEP_EVE_NUM);
				break;
			case 2 :
				request += Integer.toString(THIRD_HEATING_STEP_EVE_NUM);
				break;
		}
		request += "*11*1*";
		request += Integer.toString((int) (value*100));
		request += "*";
		request += Integer.toString((int) (histeresis*100));

		//operator AND między zdarzeniami: temperatura poniżej zadanaej oraz termometr w stanie dobrym
		request += "*1*";
		request += Integer.toString(out*2+1);

		//warunek aby termometr był w dobrym stanie oraz jeżeli jest w harmonogramie to ma brać pod uwagę zdarzenie uwzględniające harmonogram
		if(onSchedule) {
			request += "*65";
		} else {
			request += "*64";
		}
		request += "*0*1*100";
		Event.httpRequest.postFrame(request);

		/*
		request = "/inpa.cgi?eventon=";
		request += Integer.toString(number);
		request += "*1";
		Event.httpRequest.postFrame(request);
		
		request = "/inpa.cgi?eventper=";
		request += Integer.toString(number);
		request += "*1";
		Event.httpRequest.postFrame(request);
		*/

		number++;
	}

	/**
	 * Włącza wszystkie zdarzenia oraz ustawia je na permanetne
	 * eventon=nr_zdarzenia*1 - włączanie
	 * ecentper=nr_zdarzenia*1 - permanetne
	 */
	public static void turnOnEvents() throws IOException {
		String request = "/inpa.cgi?eventon="+FIRST_HEATING_STEP_EVE_NUM+"*1&eventon="+SECOND_HEATING_STEP_EVE_NUM+"*1&eventon="+THIRD_HEATING_STEP_EVE_NUM+"*1&eventper=v"+FIRST_HEATING_STEP_EVE_NUM+"*1&eventper="+SECOND_HEATING_STEP_EVE_NUM+"*1&eventper="+THIRD_HEATING_STEP_EVE_NUM+"*1";
		Event.httpRequest.postFrame(request);
	}

}
