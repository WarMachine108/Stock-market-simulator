import java.util.*;
import java.io.*;

public class User {
    String name="";
    Double balance=0.0;
    HashMap<String, Stock> stockHoldings; 

    public User(String name){
        this.name=name;
        this.balance = 500000.0;
        this.stockHoldings = new HashMap<>();
    }

    public boolean buy(String symbol, int qty){ 
        if (qty<=0){ 
            System.out.println("Invalid Quantity. ");
            return false; 
        }
        Stock s = stockHoldings.getOrDefault(symbol, new Stock(symbol));
        s.updatePrice(); 
        double p = s.prevprice();
        double totalcost = p*qty; 
        if (balance < totalcost){ 
            System.out.println("Insufficient funds. ");
            return false; 
        }
        balance -= totalcost; 
        s.addquant(qty); 
        stockHoldings.put(symbol, s);
        saveData(); 
        return true; 
    }

    public boolean sell(String symbol, int qty){ 
        if (!stockHoldings.containsKey(symbol)){ 
            System.out.println("Stock not owned. "); 
            return false; 
        }
        Stock s = stockHoldings.get(symbol);
        if (s.curquant() < qty){ 
            System.out.println("Not enough quantity. ");
            return false; 
        }
        s.updatePrice(); 
        double p = s.prevprice(); 
        double totalcost = p*qty; 
        balance += totalcost; 
        s.remquant(qty);
        if (s.curquant() == 0){ 
            stockHoldings.remove(symbol);
        }
        else{ 
            stockHoldings.put(symbol, s); 
        }
        saveData(); 
        return true;
    }
    /* 
    void createText(){
        try {
            FileWriter myWriter = new FileWriter("UserInfo.txt");
            myWriter.write(name);
            myWriter.write(Double.toString(balance));
            
        } 
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    */
    public static void main(String[] args){ 

    }
}
