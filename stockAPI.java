import java.net.URI;
import java.net.http.*;

//currently gives out current price of apple's stock, update later
public class stockAPI{ 
    
    public static void main(String[] args){
        private static final HttpClient client = HttpClient.newHttpClient();
        private static final String apiKey = "PUT_KEY_HERE";
        
        //change later to return price, right now its void prints the data in json -> parse it and return a double.
        public static void getPrice(String symbol) throws Exception{
            try{
            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create("https://api.twelvedata.com/price?symbol=AAPL&apikey=" + apiKey))
                                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body()); //modify to return after parsing json 
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
