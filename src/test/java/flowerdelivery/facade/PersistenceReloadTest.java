package flowerdelivery.facade;

import flowerdelivery.delivery.Delivery;
import flowerdelivery.inventory.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceReloadTest {
    @TempDir Path dir;

    @Test
    void interruptedChainIsReadFromDiskAndRolledBackAfterRestart() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);
        shop.givenChainInterruptedAfterStepB(5, "CENTER");

        FlowerShop afterRestart = shop.restart();

        // state survived the restart: it is read back from disk
        afterRestart.thenOrderChainIs(ProcessStatus.PENDING_C).thenRosesInStockAre(15);

        assertEquals(1, afterRestart.resumePending());

        afterRestart.thenOrderChainIs(ProcessStatus.COMPENSATED)
                .thenDeliveryIs(Delivery.Status.CANCELLED)
                .thenReservationIs(Reservation.Status.RELEASED)
                .thenRosesInStockAre(20);
    }

    @Test
    void restartWithNothingUnfinishedChangesNothing() {
        FlowerShop shop = FlowerShop.openWithRoses(dir.resolve("shop.db"), 20);

        assertEquals(0, shop.restart().resumePending());
    }
}