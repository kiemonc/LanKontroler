package LanKontrollerComunication;

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
	
	public Event(int out, double value, double histeresis) {
		this.out = out;
		this.histeresis = histeresis;
		this.value = value;
	}
	
	public Event(double value, double histeresis) {
		this(0,value,histeresis);
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
		request += Integer.toString(number);
		request += "*11*1*";
		request += Integer.toString((int) (value*100));
		request += "*";
		request += Integer.toString((int) (histeresis*100));
		request += "*0*";
		request += Integer.toString(out*2+1);
		request += "*0*0*0*0";
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
		String request = "/inpa.cgi?eventon=0*1&eventon=1*1&eventon=2*1&eventper=0*1&eventper=1*1&eventper=2*1";
		Event.httpRequest.postFrame(request);
	}

}
