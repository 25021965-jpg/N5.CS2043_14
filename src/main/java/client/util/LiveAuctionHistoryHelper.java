package client.util;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import model.Bid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LiveAuctionHistoryHelper {
    private LiveAuctionHistoryHelper() {
    }

    public static void initializeHistoryTable(TableView<Bid> table, ObservableList<Bid> historyList) {
        table.setItems(historyList);
    }

    public static List<Bid> parseBidHistory(String data) {
        List<Bid> history = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return history;
        }

        String[] bidTokens = data.split("\\|");
        for (String token : bidTokens) {
            if (token.isBlank()) {
                continue;
            }
            try {
                String[] parts = token.split(";");
                if (parts.length >= 3) {
                    Bid bid = new Bid();
                    bid.setUsername(parts[0]);
                    BigDecimal amount = new BigDecimal(parts[1]);
                    bid.setAmount(amount);
                    bid.setAmountString(String.format("%,.2f", amount) + " USD");
                    bid.setTimeString(parts[2]);
                    history.add(bid);
                }
            } catch (Exception ignored) {
                // ignore malformed bid entries
            }
        }
        return history;
    }

    public static int restoreCachedHistory(
            List<Bid> cachedHistory,
            int cachedCounter,
            ObservableList<Bid> bidHistoryList,
            XYChart.Series<String, Number> chartSeries,
            TableView<Bid> bidHistoryTable
    ) {
        if (cachedHistory == null || cachedHistory.isEmpty()) {
            return 0;
        }

        bidHistoryList.clear();
        bidHistoryList.addAll(cachedHistory);
        int currentCounter = cachedCounter;

        chartSeries.getData().clear();
        List<Bid> reversedForChart = new ArrayList<>(bidHistoryList);
        Collections.reverse(reversedForChart);
        for (int i = 0; i < reversedForChart.size(); i++) {
            double priceInMillions = reversedForChart.get(i).getAmount().doubleValue() / 1_000_000;
            chartSeries.getData().add(new XYChart.Data<>(String.valueOf(i), priceInMillions));
        }

        bidHistoryTable.refresh();
        return currentCounter;
    }

    public static void loadBidHistory(
            List<Bid> history,
            ObservableList<Bid> bidHistoryList,
            XYChart.Series<String, Number> chartSeries,
            TableView<Bid> bidHistoryTable
    ) {
        if (history == null || history.isEmpty()) {
            return;
        }

        bidHistoryList.clear();
        bidHistoryList.addAll(history);
        bidHistoryList.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        chartSeries.getData().clear();
        int bidCounter = 0;
        List<Bid> chartOrder = new ArrayList<>(bidHistoryList);
        Collections.reverse(chartOrder);
        for (Bid bid : chartOrder) {
            double priceInMillions = bid.getAmount().doubleValue() / 1_000_000;
            chartSeries.getData().add(new XYChart.Data<>(String.valueOf(bidCounter++), priceInMillions));
        }

        for (int i = 0; i < bidHistoryList.size(); i++) {
            bidHistoryList.get(i).setStatus(i == 0 ? "LEADING" : "OUTBID");
        }

        bidHistoryTable.refresh();
    }

    public static boolean isDuplicateBid(
            ObservableList<Bid> bidHistoryList,
            String bidTime,
            String bidderUsername,
            BigDecimal incomingPrice
    ) {
        if (bidHistoryList.isEmpty()) {
            return false;
        }
        Bid lastBid = bidHistoryList.getFirst();
        return lastBid.getTimeString().equals(bidTime)
                && lastBid.getUsername().equals(bidderUsername)
                && lastBid.getAmount().compareTo(incomingPrice) == 0;
    }

    public static int addChartData(
            int bidCounter,
            XYChart.Series<String, Number> chartSeries,
            BigDecimal price
    ) {
        double priceInMillions = price.doubleValue() / 1_000_000;
        chartSeries.getData().add(new XYChart.Data<>(String.valueOf(bidCounter), priceInMillions));
        if (chartSeries.getData().size() > 20) {
            chartSeries.getData().removeFirst();
        }
        return bidCounter + 1;
    }
}
