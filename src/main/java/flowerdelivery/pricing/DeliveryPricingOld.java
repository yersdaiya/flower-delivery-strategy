package flowerdelivery.pricing;

public class DeliveryPricingOld {
    // Барлық логика бір жерде (if-else көмегімен) — бұл рефакторингке дейінгі ескі тәсіл ("bad" way)
    public double calculatePrice(String strategyType, double basePrice, double distanceKm) {
        if (strategyType.equalsIgnoreCase("STANDARD")) {
            return basePrice + (distanceKm * 150);
        } else if (strategyType.equalsIgnoreCase("EXPRESS")) {
            return (basePrice + (distanceKm * 250)) * 1.5;
        } else if (strategyType.equalsIgnoreCase("HOLIDAY")) {
            return (basePrice + (distanceKm * 200)) * 2.0;
        } else {
            throw new IllegalArgumentException("Белгісіз жеткізу түрі: " + strategyType);
        }
    }
}