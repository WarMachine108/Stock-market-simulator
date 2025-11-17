import java.util.*;
import java.io.*;

public class Stock implements Runnable{
    String name="";
    Double currentPrice;
    Double previousPrice;
    int quantity;
    public Stock(String name,Double currentPrice,Double previousPrice,int quantity){
        this.name=name;
        this.currentPrice=currentPrice;
        this.previousPrice=previousPrice;
        this.quantity=quantity;
    }
    public void run(){
        System.out.println("Stock Thread running");
    }
    public void updatePrice(Double currentPrice){
        this.currentPrice=currentPrice;
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
}
