package flowerdelivery.facade;

import flowerdelivery.delivery.Delivery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static flowerdelivery.facade.FlowerShop.GOOD_CARD;
import static org.junit.jupiter.api.Assertions.*;

class AdminFacadeTest {
    @TempDir Path dir;

    @Test
    void adminCanInspectCompletedOrder() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);
        String orderId = shop.placeOrder(5, "CENTER", GOOD_CARD);

        String report = shop.inspect(orderId);

        assertTrue(report.contains("COMPLETED"));
        assertTrue(report.contains("PAID"));
    }

    @Test
    void adminCanCompensateInterruptedChainManually() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);
        String orderId = shop.givenChainInterruptedAfterStepB(5, "CENTER");

        shop.compensateManually(orderId);

        shop.thenOrderChainIs(ProcessStatus.COMPENSATED)
                .thenDeliveryIs(Delivery.Status.CANCELLED)
                .thenRosesInStockAre(20);
    }

    @Test
    void completedOrderCannotBeCompensated() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);
        String orderId = shop.placeOrder(5, "CENTER", GOOD_CARD);

        assertThrows(OrderFailedException.class, () -> shop.compensateManually(orderId));
    }
}