package flowerdelivery.packaging;

public class PackagingContext {
    private PackagingStrategy strategy;

    public PackagingContext(PackagingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PackagingStrategy strategy) {
        this.strategy = strategy;
    }

    public String executeDescription() {
        return strategy.getPackagingDescription();
    }

    public double executeCost() {
        return strategy.calculatePackagingCost();
    }
}