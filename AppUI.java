import java.util.Optional;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.beans.property.*;

public class AppUI extends Application {

    private Label nameValue; 
    private Label balanceValue; 
    private TableView<Stock> table;
    private ObservableList<Stock> stockList;
    private static User user; 

    @Override
    public void start(Stage stage) {
        user = new User("Tanmay");
    
        Label totalLabel = new Label("Total Profit/Loss: ₹0.00");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label balancelabel = new Label("User Details: "); 
        balancelabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        nameValue = new Label("Name: " + user.name);
        nameValue.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        balanceValue = new Label("Balance: ₹" + user.balance);
        balanceValue.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Button buyBtn = new Button("Buy");
        Button sellBtn = new Button("Sell");
        Button graphbtn = new Button("Toggle Graph"); 
        Button refreshBtn = new Button("Refresh Price");
        Button exitBtn = new Button("Exit");
        exitBtn.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, buyBtn, sellBtn, refreshBtn, graphbtn, exitBtn);
        buttons.getStyleClass().add("button-bar");
        buttons.setAlignment(Pos.CENTER);

        table = new TableView<>();
        stockList = FXCollections.observableArrayList();

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Stock, String> nameCol = new TableColumn<>("Stock");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));

        TableColumn<Stock, Number> priceCol = new TableColumn<>("Buy Price");
        priceCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getPrevPrice()));

        TableColumn<Stock, Number> curpr = new TableColumn<>("Current Price");
        curpr.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getCurPrice()));

        TableColumn<Stock, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()));

        TableColumn<Stock, Number> proloss = new TableColumn<>("Profit/Loss");
        proloss.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().profitloss()));

        table.getColumns().addAll(nameCol, priceCol, curpr, qtyCol, proloss);
        table.setRowFactory(tv -> new TableRow<Stock>() {
            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);

                if (empty || stock == null) {
                    setStyle("");
                } else {
                    double pl = stock.profitloss();

                    if (pl > 0) {
                        setStyle("-fx-background-color: rgba(0,255,0,0.15);");
                    } else if (pl < 0) {
                        setStyle("-fx-background-color: rgba(255,0,0,0.15);"); 
                    } else {
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            }
        });
        table.setItems(stockList);
        user.loadData();
        stockList.addAll(user.stockHoldings.values());
        table.refresh();
        buyBtn.setOnAction(e -> openBuyDialog(totalLabel));
        sellBtn.setOnAction(e -> openSellDialog(totalLabel));
        refreshBtn.setOnAction(e -> refreshPrices(totalLabel));
        graphbtn.setOnAction(e -> showgraph());

        VBox bottomBox = new VBox(10, totalLabel, balancelabel, nameValue, balanceValue);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(10,0,10,20));
        Label mainTitle = new Label("STOCK MARKET SIMULATOR");
        mainTitle.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold;");
        mainTitle.setAlignment(Pos.CENTER);
        mainTitle.setMaxWidth(Double.MAX_VALUE);
        VBox contentBox = new VBox(20, mainTitle, buttons, table, bottomBox);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_CENTER);

        VBox root = new VBox(contentBox);
        root.setStyle("-fx-background-color: #000000;");

        String darkCSS = """
        .root {
            -fx-base: #000000;
            -fx-background-color: #000000;
        }

        .label {
            -fx-text-fill: white;
        }

        .button {
            -fx-background-color: linear-gradient(#2e2e2e, #1e1e1e);
            -fx-text-fill: white;
            -fx-padding: 8 18;
            -fx-background-radius: 8;
            -fx-font-size: 14px;
            -fx-border-color: #444444;
            -fx-border-radius: 8;
        }

        .button:hover {
            -fx-background-color: linear-gradient(#3c3c3c, #2a2a2a);
            -fx-cursor: hand;
        }

        .button:pressed {
            -fx-background-color: #111111;
            -fx-translate-y: 1px;
        }

        .table-view {
            -fx-background-color: #111111;
            -fx-border-color: #333333;
        }

       .table-view .column-header-background {
            -fx-background-color: #111111;
            -fx-padding: 0;
            -fx-background-insets: 0;
        }

        .table-view .corner {
            -fx-background-color: #111111;
            -fx-border-color: transparent;
        }
        .table-view .column-header {
            -fx-background-color: #111111;
            -fx-border-color: transparent;
            -fx-padding: 5 10 5 10;
        }

        .table-view .filler {
            -fx-background-color: #111111;
        }

        .table-row-cell {
            -fx-background-color: #111111;
        }
        .table-row-cell:filled:selected {
            -fx-background-color: #4fc3f7;
            -fx-text-fill: black;
        }

        .table-view .table-cell {
            -fx-text-fill: white !important;
            -fx-font-weight: normal;
        }

        .table-view .column-header .label {
            -fx-text-fill: white !important;
            -fx-font-weight: bold;
        }

        .dialog-pane {
            -fx-background-color: #111111;
        }
        .dialog-pane .label {
            -fx-text-fill: white;
        }
        .dialog-pane .button {
            -fx-background-color: #333333;
            -fx-text-fill: white;
        }
        .button-bar {
            -fx-background-color: #151515;
            -fx-padding: 15 0 15 0;
            -fx-spacing: 12;
            -fx-alignment: center;
            -fx-border-color: #333333;
            -fx-border-width: 0 0 1 0;
        }
        """;

        Scene scene = new Scene(root, 600, 800);
        scene.getStylesheets().add("data:text/css," + darkCSS);
        stage.setScene(scene);

        stage.show();

        Timeline autoUpdate = new Timeline(
                new KeyFrame(Duration.seconds(10), ev -> {
                    for (Stock s : stockList) s.updatePrice();
                    table.refresh();
                    updateTotalLabel(totalLabel);
                })
        );
        autoUpdate.setCycleCount(Timeline.INDEFINITE);
        autoUpdate.play();
    }

    private double getTotalProfitLoss() {
        return stockList.stream().mapToDouble(Stock::profitloss).sum();
    }

    private void updateTotalLabel(Label totalLabel) {
        totalLabel.setText(String.format("Total Profit/Loss: ₹%.2f", getTotalProfitLoss()));
    }
    private void showgraph(){ 
        Stock s = table.getSelectionModel().getSelectedItem();
        if (s == null){ 
            showAlert("No stock selected!");
            return; 
        }
        graphing.display(s.symbol);
    }
    private void openBuyDialog(Label totalLabel) {

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setHeaderText("Enter Stock Name");
        nameDialog.setContentText("Stock name:");
        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isEmpty()) return;

        String stockName = nameResult.get();

        var results = yahoofin.searchStocksReturnList(stockName);
        if (results.isEmpty()) {
            showAlert("No matching stocks found!");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(results.get(0), results);
        dialog.setHeaderText("Select Stock Symbol");
        Optional<String> chosenSymbol = dialog.showAndWait();
        if (chosenSymbol.isEmpty()) return;

        String symbol = chosenSymbol.get();

        double price = stockAPI.getPrice(symbol);
        if (price == -1) {
            showAlert("Error fetching price for " + symbol);
            return;
        }

        TextInputDialog qtyDialog = new TextInputDialog();
        qtyDialog.setHeaderText("Enter Quantity");
        Optional<String> qtyResult = qtyDialog.showAndWait();
        if (qtyResult.isEmpty()) return;

        int qty = Integer.parseInt(qtyResult.get());

        boolean ok = user.buy(symbol, qty);

        if (!ok) {
            showAlert("Could not buy stock.");
            return;
        }
        stockList.setAll(user.stockHoldings.values());
        table.refresh();

        updateTotalLabel(totalLabel);
        balanceValue.setText("Balance: ₹" + user.balance);

        showAlert("Bought " + qty + " shares of " + stockName);
    }


   private void openSellDialog(Label totalLabel) {
        Stock selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a stock first!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Sell Shares of " + selected.name);
        dialog.setContentText("Quantity:");

        dialog.showAndWait().ifPresent(q -> {
            int qty = Integer.parseInt(q);

            boolean ok = user.sell(selected.symbol, qty);

            if (!ok) {
                showAlert("Sell failed.");
                return;
            }

            stockList.setAll(user.stockHoldings.values());
            table.refresh();

            updateTotalLabel(totalLabel);
            balanceValue.setText("Balance: ₹" + user.balance);
        });
    }


    private void refreshPrices(Label totalLabel) {
        stockList.forEach(Stock::updatePrice);
        table.refresh();
        updateTotalLabel(totalLabel);
        showAlert("Prices refreshed");
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
