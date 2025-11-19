import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class graphing extends Application {
    public static void display(String sym){ 
        try{ 
            String json = yahoofin.getRawData(sym, "60m", "1mo");
            Path p = Path.of("stocks/stockname.json");
            Files.writeString(p, json);
            Platform.runLater(() -> {
                    try {
                        Stage s = new Stage();
                        graphing g = new graphing();
                        g.start(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    private static final String DATA_FILE = "stocks/stockname.json";

    private final List<DataPoint> allPoints = new ArrayList<>();
    private LineChart<Number, Number> chart;
    private NumberAxis xAxis;
    private final DateTimeFormatter hourFmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd MMM");
    private boolean showingDaily = false; // false = 1D (hourly), true = 1M (daily)

    @Override
    public void start(Stage stage) {
        loadJsonData();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                long epoch = object.longValue();
                Instant ins = Instant.ofEpochSecond(epoch);
                if (showingDaily) {
                    return dayFmt.format(ins.atZone(ZoneId.systemDefault()).toLocalDate());
                } else {
                    return hourFmt.format(ins.atZone(ZoneId.systemDefault()).toLocalTime());
                }
            }
            @Override public Number fromString(String s) { return 0; }
        });

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Price");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true); // enable symbols so Tooltips can attach to nodes

        Button b1d = new Button("1D");
        Button b1m = new Button("1M");
        Label modeLabel = new Label("Mode: ");

        b1d.setOnAction(e -> { showingDaily = false; update1D(); });
        b1m.setOnAction(e -> { showingDaily = true; update1M(); });

        HBox controls = new HBox(8, modeLabel, b1d, b1m);
        controls.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(chart);
        root.setBottom(controls);

        Scene scene = new Scene(root, 900, 600);
        String darkCSS =
        """
        .root {
            -fx-base: #000000;
            -fx-background-color: #000000;
        }

        .chart-plot-background {
            -fx-background-color: #111111;
        }

        .chart-vertical-grid-lines,
        .chart-horizontal-grid-lines,
        .chart-alternative-row-fill {
            -fx-stroke: #333333;
        }

        .chart-title, .label {
            -fx-text-fill: white;
        }

        .axis .tick-label {
            -fx-fill: white;
        }

        .axis {
            -fx-tick-label-fill: white;
            -fx-axis-line-color: white;
            -fx-tick-mark-color: white;
        }

        .chart-series-line {
            -fx-stroke-width: 2px;
        }

        .button {
            -fx-background-color: #2b2b2b;
            -fx-text-fill: white;
        }
        """;

        scene.getStylesheets().add("data:text/css," + darkCSS);
        stage.setScene(scene);
        stage.setTitle("Barebones 1D/1M");
        b1d.fire(); // default
        stage.show();
    }

    private void update1D() {
        chart.getData().clear();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        List<DataPoint> pts = allPoints.stream()
                .filter(p -> Instant.ofEpochSecond(p.ts).atZone(ZoneId.systemDefault()).toLocalDate().equals(today))
                .sorted(Comparator.comparingLong(p -> p.ts)) // ensure sorted ascending
                .collect(Collectors.toList());

        if (pts.isEmpty()) {
            xAxis.setAutoRanging(true);
            return;
        }

        XYChart.Series<Number, Number> s = new XYChart.Series<>();
        for (DataPoint p : pts) s.getData().add(new XYChart.Data<>(p.ts, p.price));
        chart.getData().add(s);

        // Print each point to console (time, price)
        ZoneId zid = ZoneId.systemDefault();
        for (XYChart.Data<Number, Number> d : s.getData()) {
            long epoch = d.getXValue().longValue();
            String timeLabel = hourFmt.format(Instant.ofEpochSecond(epoch).atZone(zid).toLocalTime());
            System.out.printf("1D point -> time=%s (%d), price=%.2f%n", timeLabel, epoch, d.getYValue().doubleValue());
        }

        // Optional: attach tooltip to each data symbol (enable symbols if you disabled them)
        Platform.runLater(() -> {
            for (XYChart.Data<Number, Number> d : s.getData()) {
                Node node = d.getNode();
                if (node != null) {
                    long epoch = d.getXValue().longValue();
                    String timeLabel = hourFmt.format(Instant.ofEpochSecond(epoch).atZone(zid).toLocalTime());
                    String tip = timeLabel + " — " + String.format("%.2f", d.getYValue().doubleValue());
                    Tooltip.install(node, new Tooltip(tip));
                }
            }
        });

        applyXAxisPaddingAndStyle(pts.stream().mapToLong(dp -> dp.ts).toArray(), s);
    }

    private void update1M() {
        chart.getData().clear();
        // last close per day
        Map<LocalDate, DataPoint> last = new HashMap<>();
        for (DataPoint p : allPoints) {
            LocalDate d = Instant.ofEpochSecond(p.ts).atZone(ZoneId.systemDefault()).toLocalDate();
            DataPoint cur = last.get(d);
            if (cur == null || p.ts > cur.ts) last.put(d, p);
        }
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = today.minusDays(30);
        List<DataPoint> days = last.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(start) && !e.getKey().isAfter(today))
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        if (days.isEmpty()) {
            xAxis.setAutoRanging(true);
            return;
        }

        XYChart.Series<Number, Number> s = new XYChart.Series<>();
        List<Long> dayStarts = new ArrayList<>();
        for (DataPoint p : days) {
            long dayStart = Instant.ofEpochSecond(p.ts).atZone(ZoneId.systemDefault())
                    .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            dayStarts.add(dayStart);
            s.getData().add(new XYChart.Data<>(dayStart, p.price));
        }
        chart.getData().add(s);

        // Print each point to console (date start, price)
        ZoneId zid = ZoneId.systemDefault();
        for (XYChart.Data<Number, Number> d : s.getData()) {
            long epoch = d.getXValue().longValue();
            String dateLabel = dayFmt.format(Instant.ofEpochSecond(epoch).atZone(zid).toLocalDate());
            System.out.printf("1M point -> date=%s (%d), price=%.2f%n", dateLabel, epoch, d.getYValue().doubleValue());
        }

        // Optional: attach tooltip to each data symbol
        Platform.runLater(() -> {
            for (XYChart.Data<Number, Number> d : s.getData()) {
                Node node = d.getNode();
                if (node != null) {
                    long epoch = d.getXValue().longValue();
                    String dateLabel = dayFmt.format(Instant.ofEpochSecond(epoch).atZone(zid).toLocalDate());
                    String tip = dateLabel + " — " + String.format("%.2f", d.getYValue().doubleValue());
                    Tooltip.install(node, new Tooltip(tip));
                }
            }
        });

        long[] arr = dayStarts.stream().mapToLong(Long::longValue).toArray();
        applyXAxisPaddingAndStyle(arr, s);
    }

    // set small padding on the x-axis so points are not at the edges and make the series line thicker
    private void applyXAxisPaddingAndStyle(long[] sortedTs, XYChart.Series<Number, Number> series) {
        if (sortedTs == null || sortedTs.length == 0) {
            xAxis.setAutoRanging(true);
            applyYAxisAuto();
            return;
        }
        Arrays.sort(sortedTs);
        if (sortedTs.length == 1) {
            // single point, let axis auto-range for better look
            xAxis.setAutoRanging(true);
        } else {
            double min = sortedTs[0];
            double max = sortedTs[sortedTs.length - 1];
            double span = Math.max(1, max - min);
            double pad = Math.max(1, span * 0.03); // 3% padding
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(min - pad);
            xAxis.setUpperBound(max + pad);
            xAxis.setTickUnit((max - min) / 6.0); // readable number of ticks
        }

        // set Y axis near series min/max instead of starting at 0
        applyYAxisPaddingAndStyle(series);

        // style the series line after it is rendered
        Platform.runLater(() -> {
            if (series.getNode() != null) {
                series.getNode().setStyle("-fx-stroke-width: 2.5px;");
            }
        });
    }

    // compute Y axis bounds from the series data and add a small padding so axis doesn't start at 0
    private void applyYAxisPaddingAndStyle(XYChart.Series<Number, Number> series) {
        if (series == null || series.getData().isEmpty()) {
            applyYAxisAuto();
            return;
        }
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (XYChart.Data<Number, Number> d : series.getData()) {
            Number yv = d.getYValue();
            if (yv == null) continue;
            double v = yv.doubleValue();
            if (v < min) min = v;
            if (v > max) max = v;
        }
        if (min == Double.POSITIVE_INFINITY) {
            applyYAxisAuto();
            return;
        }

        double span = Math.max(1e-6, max - min);
        double pad = Math.max(1.0, span * 0.05); // 5% padding or at least 1 unit
        double lower = min - pad;
        double upper = max + pad;

        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(lower);
        yAxis.setUpperBound(upper);
        yAxis.setTickUnit((upper - lower) / 6.0);
    }

    private void applyYAxisAuto() {
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setAutoRanging(true);
    }

    private void loadJsonData() {
        allPoints.clear();
        String raw;
        try {
            raw = Files.readString(Path.of(DATA_FILE));
        } catch (IOException e) {
            System.err.println("read failed: " + e.getMessage());
            return;
        }
        List<Long> ts = parseLongArray(raw, "\"timestamp\"");
        List<Double> close = parseDoubleArray(raw, "\"close\"");
        int n = Math.min(ts.size(), close.size());
        for (int i = 0; i < n; i++) {
            Double c = close.get(i);
            if (c == null) continue;
            allPoints.add(new DataPoint(ts.get(i), c));
        }
        // ensure global storage is sorted ascending by time (helps all views)
        allPoints.sort(Comparator.comparingLong(p -> p.ts));
    }

    private static List<Long> parseLongArray(String src, String key) {
        int k = src.indexOf(key); if (k < 0) return List.of();
        int b = src.indexOf('[', k); if (b < 0) return List.of();
        int e = findClosing(src, b); if (e < 0) return List.of();
        String body = src.substring(b+1, e);
        String[] parts = body.split(",");
        List<Long> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim().replaceAll("[^0-9\\-]", "");
            if (t.isEmpty()) continue;
            try { out.add(Long.parseLong(t)); } catch (NumberFormatException ignored) {}
        }
        return out;
    }

    private static List<Double> parseDoubleArray(String src, String key) {
        int k = src.indexOf(key); if (k < 0) return List.of();
        int b = src.indexOf('[', k); if (b < 0) return List.of();
        int e = findClosing(src, b); if (e < 0) return List.of();
        String body = src.substring(b+1, e);
        String[] parts = body.split(",");
        List<Double> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty() || t.equals("null")) { out.add(null); continue; }
            try { out.add(Double.parseDouble(t)); } catch (NumberFormatException ex) { out.add(null); }
        }
        return out;
    }

    private static int findClosing(String s, int open) {
        int d = 0;
        for (int i = open; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') d++; else if (c == ']') { d--; if (d==0) return i; }
        }
        return -1;
    }

    private static class DataPoint {
        final long ts;
        final double price;
        DataPoint(long ts, double price) { this.ts = ts; this.price = price; }
    }

    public static void main(String[] args) { launch(); }
}
