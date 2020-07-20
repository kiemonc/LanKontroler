package LanKontrollerComunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Komunikuje się z LK za pośrednictwem zapytań http
 */
public class HttpRequest {
	private String ipAdress;
	public HttpRequest(String ipAdress) { 
		this.ipAdress = ipAdress;
	}

	/**
	 * Pobiera od LK odpowiedź na dane zapytanie
	 * @param question zapytanie z pomienięciem adrazy IP
	 * @return treść odpowiedzi
	 * @throws IOException
	 */
	public String getResponse(String question) throws IOException {
		StringBuilder result = new StringBuilder();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(postFrame(question)));
	    String line;
	    while ((line = rd.readLine()) != null) {
	    	result.append(line);
	    }
	    rd.close();
	    return result.toString();
	}

	/**
	 * Wysyła zapytanie do LK bez pobierania odpowiedzi. Zwraca connection do wykorzystania w metodzie getResponse
	 * @param question zapytanie z pominięciem adrasu IP
	 * @return connection
	 * @throws IOException
	 */
	public InputStream postFrame(String question) throws IOException {
		URL url = new URL("http://"+ipAdress+question);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
	    return conn.getInputStream();
	}
}
