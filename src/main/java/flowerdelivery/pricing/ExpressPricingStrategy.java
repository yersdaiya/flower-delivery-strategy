package flowerdelivery.pricing;

public class ExpressPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice, double distanceKm) {
        return (basePrice + (distanceKm * 250)) * 1.5;
    }
}