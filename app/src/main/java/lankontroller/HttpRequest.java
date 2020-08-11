package lankontroller;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Komunikuje się z LK za pośrednictwem zapytań http
 */
public class HttpRequest {
	private String ipAdress;
	public HttpRequest(String ipAdress) {
		this.ipAdress = ipAdress;
	}
	final private int timeout = 15000;
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
		conn.setConnectTimeout(timeout);
		conn.setRequestMethod("GET");
		return conn.getInputStream();
	}

	HttpURLConnection con;
	/**
	 * Realizcja metody POST
	 * @param request adres z pominięciem adresu ip LK3, ale z uwzględnieniem /
	 * @param body wiadomość wysyłana do lK3
	 * @throws IOException
	 */
	public void post(String request, String body) throws IOException {
		String url = "http://"+ipAdress+request;
		byte[] postData = body.getBytes(StandardCharsets.UTF_8);

		try {

			URL myurl = new URL(url);
			con = (HttpURLConnection) myurl.openConnection();

			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Java client");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {

				wr.write(postData);
			}

			StringBuilder content;

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream()))) {

				while (br.readLine() != null) {
				}
			}

		} finally {

			con.disconnect();
		}
	}
}
