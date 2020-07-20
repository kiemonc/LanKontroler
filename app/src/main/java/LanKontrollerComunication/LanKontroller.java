package LanKontrollerComunication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LanKontroller {
	private HttpRequest httpRequest;
	private Termometer termometer;
	private int temperatureSteps;
	private double histeresis;
	
	
	public LanKontroller(String ipAdress) {
		this.httpRequest = new HttpRequest(ipAdress);
		this.termometer = new Termometer(httpRequest);
		Event.setHttpRequest(httpRequest);
		temperatureSteps = 2;
		histeresis = 0.5;
	}
	
	public void shutOffHeating() throws IOException {
		Event.removeEvents();
		httpRequest.postFrame("/outs.cgi?out1=0&out2=0&out3=0");
	}
	
	public double getTemperature() throws IOException {
		return termometer.getTemperature();
	}
	
	public void turnOnHeating(int temperature) throws IOException {
		List<Event> events = new ArrayList<>();
		events.add(new Event(temperature,histeresis));
		events.add(new Event(temperature-temperatureSteps,histeresis));
		events.add(new Event(temperature-temperatureSteps*2,histeresis));
		
		
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
		
	}
	
	public int getState() throws IOException {
		String state = httpRequest.getResponse("/inpa.cgi");
		Pattern patt = Pattern.compile("(?<=\\<out\\>\\d)(.*?)(?=\\d\\d\\<\\/out\\>)");
		Matcher matcher = patt.matcher(state);
		int number = 0;
		if(matcher.find()) {
			number = Integer.parseInt(matcher.group());
		}
		
		
		return number;
	}
}
