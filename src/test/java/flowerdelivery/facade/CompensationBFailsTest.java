package flowerdelivery.facade;

import flowerdelivery.inventory.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static flowerdelivery.facade.FlowerShop.GOOD_CARD;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompensationBFailsTest {
    @TempDir Path dir;

    @Test
    void whenDeliveryFailsAfterReservationOnlyTheReservationIsReleased() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);

        assertThrows(OrderFailedException.class, () -> shop.placeOrder(5, "REMOTE", GOOD_CARD));

        shop.thenOrderChainIs(ProcessStatus.COMPENSATED)
                .thenReservationIs(Reservation.Status.RELEASED)
                .thenRosesInStockAre(20)
                .thenNoDeliveryWasCreated()
                .thenNoPaymentWasCreated();
    }
}