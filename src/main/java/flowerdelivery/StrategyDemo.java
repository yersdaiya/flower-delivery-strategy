package flowerdelivery;

import flowerdelivery.pricing.*;
import flowerdelivery.packaging.*;

public class StrategyDemo {
    public static void main(String[] args) {
        System.out.println("=== FLOWER DELIVERY: STRATEGY PATTERN DEMO ===");

        double basePrice = 1000.0;
        double distance = 12.0;

        // 1. Pricing Strategies
        System.out.println("\n--- 1. Pricing Strategies ---");
        PricingStrategy standard = new StandardPricingStrategy();
        PricingStrategy express = new ExpressPricingStrategy();

        FakeFactorProvider fakeProvider = new FakeFactorProvider();
        fakeProvider.setMultiplier(2.0);
        PricingStrategy holiday = new HolidayPricingStrategy(fakeProvider);

        System.out.println("Standard Price: " + standard.calculatePrice(basePrice, distance) + " KZT");
        System.out.println("Express Price: " + express.calculatePrice(basePrice, distance) + " KZT");
        System.out.println("Holiday Price (Multiplier 2.0): " + holiday.calculatePrice(basePrice, distance) + " KZT");

        // 2. Context & Runtime Strategy Switching
        System.out.println("\n--- 2. Context & Runtime Strategy Switching ---");
        DeliveryPricingContext pricingContext = new DeliveryPricingContext(standard);
        System.out.println("Initial Strategy Calculation (Standard): " + pricingContext.executeCalculation(basePrice, distance) + " KZT");

        // Switch strategy at runtime to Express
        pricingContext.setStrategy(express);
        System.out.println("After Switching Strategy (Express): " + pricingContext.executeCalculation(basePrice, distance) + " KZT");

        // 3. Selector (Automatic Choice)
        System.out.println("\n--- 3. Selector (Automatic Choice) ---");
        DeliverySelector selector = new DeliverySelector(fakeProvider);
        PricingStrategy selected = selector.selectStrategy("HOLIDAY");
        System.out.println("Selector automatically picked 'HOLIDAY', result: " + selected.calculatePrice(basePrice, distance) + " KZT");

        // 4. Packaging Strategy Family
        System.out.println("\n--- 4. Packaging Strategy Family ---");
        PackagingContext packagingContext = new PackagingContext(new EcoPackaging());
        System.out.println("Package Type: " + packagingContext.executeDescription() + " | Cost: " + packagingContext.executeCost() + " KZT");

        packagingContext.setStrategy(new LuxuryPackaging());
        System.out.println("Package Type: " + packagingContext.executeDescription() + " | Cost: " + packagingContext.executeCost() + " KZT");

        System.out.println("\n=== DEMO COMPLETED SUCCESSFULLY ===");
    }
}
