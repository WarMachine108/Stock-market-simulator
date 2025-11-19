//NETWORKING WAALE
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//FILE HANDLING
import java.io.FileWriter;
import java.io.File;
//FOR URL MODIFICATION 
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//JSON THING
import com.google.gson.*;

//INPUT AND SHI
import java.util.*;

class yahoofin {

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

    public static void searchStocks(String query) {
        try {
            String url = "https://query1.finance.yahoo.com/v1/finance/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray quotes = obj.getAsJsonArray("quotes");

            System.out.println("\nSearch results for \"" + query + "\":\n");

            for (JsonElement e : quotes) {
                JsonObject item = e.getAsJsonObject();

                if (!item.has("symbol") || !item.has("shortname"))
                    continue;

                String symbol = item.get("symbol").getAsString();
                String name = item.get("shortname").getAsString();

                System.out.println(symbol + " -> " + name);
            }

            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> searchStocksReturnList(String query) {
        ArrayList<String> out = new ArrayList<>();

        try {
            String url = "https://query1.finance.yahoo.com/v1/finance/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray quotes = obj.getAsJsonArray("quotes");

            for (JsonElement e : quotes) {
                JsonObject item = e.getAsJsonObject();
                if (item.has("symbol"))
                    out.add(item.get("symbol").getAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }
}

public class stockAPI {

    public static String prettyJson(String uglyJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(uglyJson);
        return gson.toJson(je);
    }

    public static void saveToFile(String data) {
        try {

            File folder = new File("stocks");
            if (!folder.exists()) folder.mkdir();

            File[] oldFiles = folder.listFiles();
            if (oldFiles != null) {
                for (File f : oldFiles) f.delete();
            }

            Thread.sleep(50);

            File file = new File(folder, "stockname.json");
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.close();

            System.out.println("Saved JSON to: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void bgupdate(String symbol, long intervalMillis) {

        Thread updater = new Thread(() -> {
            while (true) {
                try {
                    String json = yahoofin.getRawData(symbol, "60m", "1mo");
                    String pretty = prettyJson(json);

                    saveToFile(pretty);

                    Thread.sleep(intervalMillis);

                } catch (Exception e) {
                    System.out.println("Background update failed.");
                }
            }
        });

        updater.setDaemon(true); 
        updater.start();
    }

    public static double getPrice(String symbol) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String raw = response.body();

            int start = raw.indexOf('{');
            if (start != -1) {
                raw = raw.substring(start);
            }
            int end = raw.lastIndexOf('}');
            if (end != -1) {
                raw = raw.substring(0, end + 1);
            }
            raw = raw.replace(")]}'", "").trim();

            JsonObject json = JsonParser.parseString(raw).getAsJsonObject();

            JsonObject meta = json
                    .getAsJsonObject("chart")
                    .getAsJsonArray("result")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("meta");

            return meta.get("regularMarketPrice").getAsDouble();

        } catch (Exception e) {
            System.out.println("Gson Error fetching price for " + symbol + ": " + e);
            return -1;
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter stock name to search: ");
        String query = sc.nextLine();

        yahoofin.searchStocks(query);

        System.out.print("\nEnter the symbol you want to track: ");
        String symbol = sc.nextLine();
        bgupdate(symbol, 10000);
        System.out.println("\nAuto-updating in background for: " + symbol);
        System.out.println("Press Ctrl+C to stop the program.\n");

        while (true) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {}
        }   
    }
}
