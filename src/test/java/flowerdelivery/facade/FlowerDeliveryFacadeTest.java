package flowerdelivery.facade;

import flowerdelivery.delivery.Delivery;
import flowerdelivery.inventory.Reservation;
import flowerdelivery.payment.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static flowerdelivery.facade.FlowerShop.GOOD_CARD;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlowerDeliveryFacadeTest {
    @TempDir Path dir;

    @Test
    void successfulOrderCompletesWholeChain() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);

        shop.placeOrder(5, "CENTER", GOOD_CARD);

        shop.thenOrderChainIs(ProcessStatus.COMPLETED)
                .thenRosesInStockAre(15)
                .thenReservationIs(Reservation.Status.CONFIRMED)
                .thenDeliveryIs(Delivery.Status.DISPATCHED)
                .thenPaymentIs(Payment.Status.PAID);
    }

    @Test
    void orderingMoreRosesThanInStockFailsBeforeAnythingIsCommitted() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);

        assertThrows(OrderFailedException.class, () -> shop.placeOrder(25, "CENTER", GOOD_CARD));

        shop.thenRosesInStockAre(20).thenNoOrderWasRecorded();
    }
}