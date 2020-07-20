package LanKontrollerComunication;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 */
public class Termometer {
	private LanKontrollerComunication.HttpRequest httpRequest;
	
	public Termometer(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
	
	public double getTemperature() throws IOException {
		double temperature;
		String response = null;
		
		response = httpRequest.getResponse("/xml/ix.xml");
		
		Pattern patt = Pattern.compile("(?<=\\<ds2\\>)(.*?)(?=<\\/ds2\\>)");
		Matcher matcher = patt.matcher(response);
		if (matcher.find()) {
		    temperature = Integer.parseInt(matcher.group())/10.0;
		} else {
			temperature = -1;
		}
		return temperature;
	}
}
