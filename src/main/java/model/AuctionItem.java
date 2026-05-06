package model;

public class AuctionItem {
    private String name;
    private double currentPrice;
    private double step;
    private String endTime;
    private String imagePath;

    public AuctionItem(String name, double currentPrice, double step, String endTime, String imagePath) {
        this.name = name;
        this.currentPrice = currentPrice;
        this.step = step;
        this.endTime = endTime;
        this.imagePath = imagePath;
    }
    // Getters
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public double getStep() { return step; }
    public String getEndTime() { return endTime; }
    public String getImagePath() { return imagePath; }
}
