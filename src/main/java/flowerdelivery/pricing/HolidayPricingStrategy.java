package flowerdelivery.pricing;

public class HolidayPricingStrategy implements PricingStrategy {
    private final ExternalFactorProvider factorProvider;

    public HolidayPricingStrategy(ExternalFactorProvider factorProvider) {
        this.factorProvider = factorProvider;
    }

    @Override
    public double calculatePrice(double basePrice, double distanceKm) {
        double multiplier = factorProvider.getSurgeMultiplier();
        return (basePrice + (distanceKm * 200)) * multiplier;
    }
}