package flowerdelivery.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PricingStrategyTest {

    @Test
    void testStandardPricing() {
        PricingStrategy strategy = new StandardPricingStrategy();
        double price = strategy.calculatePrice(1000, 10); // 1000 + 10 * 150 = 2500
        assertEquals(2500.0, price);
    }

    @Test
    void testExpressPricing() {
        PricingStrategy strategy = new ExpressPricingStrategy();
        double price = strategy.calculatePrice(1000, 10); // (1000 + 10 * 250) * 1.5 = 5250
        assertEquals(5250.0, price);
    }

    @Test
    void testHolidayPricingWithFakeProvider() {
        FakeFactorProvider fakeProvider = new FakeFactorProvider();
        fakeProvider.setMultiplier(2.5);

        PricingStrategy strategy = new HolidayPricingStrategy(fakeProvider);
        double price = strategy.calculatePrice(1000, 10); // (1000 + 10 * 200) * 2.5 = 7500
        assertEquals(7500.0, price);
    }

    @Test
    void testSelectorAutomaticChoice() {
        FakeFactorProvider fakeProvider = new FakeFactorProvider();
        DeliverySelector selector = new DeliverySelector(fakeProvider);

        PricingStrategy standard = selector.selectStrategy("STANDARD");
        assertInstanceOf(StandardPricingStrategy.class, standard);

        PricingStrategy express = selector.selectStrategy("EXPRESS");
        assertInstanceOf(ExpressPricingStrategy.class, express);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STANDARD", "EXPRESS", "HOLIDAY"})
    void testPriceIsNeverNegative(String type) {
        FakeFactorProvider fakeProvider = new FakeFactorProvider();
        DeliverySelector selector = new DeliverySelector(fakeProvider);
        PricingStrategy strategy = selector.selectStrategy(type);

        double price = strategy.calculatePrice(500, 5);
        assertTrue(price >= 0, "Баға ешқашан теріс болмауы тиіс!");
    }
}