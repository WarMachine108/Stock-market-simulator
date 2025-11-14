public class stock{
    private String symbol;
    private double price;

    stock(String symbol, double price){
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol(){ return this.symbol; }
    public double getPrice(){ return this.price; }
    
    public void setPrice(double newPrice){ this.price = newPrice;}
}