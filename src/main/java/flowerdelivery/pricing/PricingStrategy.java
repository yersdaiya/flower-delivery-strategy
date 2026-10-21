package flowerdelivery.pricing;

public interface PricingStrategy {
    double calculatePrice(double basePrice, double distanceKm);
}