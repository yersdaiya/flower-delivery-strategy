package flowerdelivery.facade;

import flowerdelivery.delivery.Delivery;
import flowerdelivery.inventory.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static flowerdelivery.facade.FlowerShop.DECLINED_CARD;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompensationCFailsTest {
    @TempDir Path dir;

    @Test
    void whenPaymentFailsDeliveryIsCancelledThenReservationIsReleased() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);

        assertThrows(OrderFailedException.class, () -> shop.placeOrder(5, "CENTER", DECLINED_CARD));

        shop.thenOrderChainIs(ProcessStatus.COMPENSATED)
                .thenDeliveryIs(Delivery.Status.CANCELLED)
                .thenReservationIs(Reservation.Status.RELEASED)
                .thenRosesInStockAre(20)
                .thenNoPaymentWasCreated();
    }
}
