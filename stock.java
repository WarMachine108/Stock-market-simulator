import java.util.*;
import java.io.*;

class Stock implements Runnable{
    String name="";
    String symbol="";
    Double currentPrice; // PRICE WHICH UPDATES
    Double previousPrice; //BUY PRICE
    int quantity;
    public Stock(String name,String symbol, Double currentPrice,Double previousPrice,int quantity){
        this.name=name;
        this.symbol=symbol;
        this.currentPrice=currentPrice;
        this.previousPrice=currentPrice;
        this.quantity=quantity;
    }
    public void run(){
        System.out.println("Stock Thread running");
    }
    public void updatePrice(){
        double newPrice = stockAPI.getPrice(symbol);

        if (newPrice == -1) {
            System.out.println("Failed to update " + symbol);
            return;
        }
        currentPrice = newPrice;
        System.out.println(name + " → Updated current price: " + newPrice);
    }
    public Double getPrevPrice(){
        return previousPrice;
    }
    public void addQuantity(int quantity){
        this.quantity+=quantity;
    }
    public void removeQuantity(int quantity){
        this.quantity-=quantity;
    }
    public int getQuantity(){
        return quantity;
    }
    public Double getCurPrice(){ 
        return currentPrice; 
    }
    public Double profitloss(){ 
        return (currentPrice-previousPrice)*quantity;
    }
}
