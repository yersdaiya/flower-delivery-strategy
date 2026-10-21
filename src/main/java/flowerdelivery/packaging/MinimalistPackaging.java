package flowerdelivery.packaging;

public class MinimalistPackaging implements PackagingStrategy {
    @Override
    public String getPackagingDescription() {
        return "Clear wrap and ribbon";
    }

    @Override
    public double calculatePackagingCost() {
        return 200.0;
    }
}