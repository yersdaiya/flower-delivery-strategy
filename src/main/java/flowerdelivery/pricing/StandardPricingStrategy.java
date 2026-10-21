package flowerdelivery.pricing;

public class StandardPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice, double distanceKm) {
        return basePrice + (distanceKm * 150);
    }
}