package flowerdelivery.packaging;

public class EcoPackaging implements PackagingStrategy {
    @Override
    public String getPackagingDescription() {
        return "Eco-friendly kraft paper and twine decoration";
    }

    @Override
    public double calculatePackagingCost() {
        return 500.0;
    }
}