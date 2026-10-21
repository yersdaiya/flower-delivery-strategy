package flowerdelivery.packaging;

public class LuxuryPackaging implements PackagingStrategy {
    @Override
    public String getPackagingDescription() {
        return "Premium box, Satin ribbon and Balloon gel";
    }

    @Override
    public double calculatePackagingCost() {
        return 2000.0;
    }
}