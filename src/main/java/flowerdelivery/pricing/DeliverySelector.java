package flowerdelivery.pricing;

public class DeliverySelector {
    private final ExternalFactorProvider factorProvider;

    public DeliverySelector(ExternalFactorProvider factorProvider) {
        this.factorProvider = factorProvider;
    }

    public PricingStrategy selectStrategy(String condition) {
        if (condition == null) {
            return new StandardPricingStrategy();
        }

        switch (condition.toUpperCase()) {
            case "EXPRESS":
                return new ExpressPricingStrategy();
            case "HOLIDAY":
                return new HolidayPricingStrategy(factorProvider);
            case "STANDARD":
            default:
                return new StandardPricingStrategy();
        }
    }
}