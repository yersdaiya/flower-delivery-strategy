package flowerdelivery.pricing;

public class DeliveryPricingContext {
    private PricingStrategy strategy;

    public DeliveryPricingContext(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public double executeCalculation(double basePrice, double distanceKm) {
        return strategy.calculatePrice(basePrice, distanceKm);
    }
}