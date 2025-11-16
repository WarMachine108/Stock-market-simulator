import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class yahoofin{

    private static final HttpClient client = HttpClient.newHttpClient();

    public static String getRawData(String symbol, String interval, String range) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" +
                         symbol + "?interval=" + interval + "&range=" + range;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

public class stockAPI {
    public static void main(String[] args) {

        String symbol = "%5ENSEI"; // NIFTY50

        // Call stockAPI
        String json = yahoofin.getRawData(symbol, "1d", "max");

        System.out.println(json);
    }
}
