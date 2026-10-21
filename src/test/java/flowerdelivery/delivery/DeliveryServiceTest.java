package flowerdelivery.delivery;

import flowerdelivery.common.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {
    @TempDir Path dir;
    private DeliveryService service;

    @BeforeEach
    void setUp() {
        DeliveryRepository repo = new DeliveryRepository(new Database(dir.resolve("del.db").toString()));
        service = new DeliveryService(repo, new ZoneRules());
    }

    @Test
    void scheduledDeliveryHasScheduledStatus() {
        int id = service.schedule("order-1", "Abay 1", "CENTER");
        assertEquals(Delivery.Status.SCHEDULED, service.status(id));
    }

    @Test
    void cancelChangesStatus() {
        int id = service.schedule("order-1", "Abay 1", "CENTER");
        service.cancel(id);
        assertEquals(Delivery.Status.CANCELLED, service.status(id));
    }

    @Test
    void unsupportedZoneFails() {
        assertThrows(DeliveryException.class, () -> service.schedule("order-1", "Far away 5", "REMOTE"));
    }

    @Test
    void zoneWithoutFreeCouriersFails() {
        service.schedule("o1", "A", "CENTER");
        service.schedule("o2", "B", "CENTER");
        service.schedule("o3", "C", "CENTER");
        assertThrows(DeliveryException.class, () -> service.schedule("o4", "D", "CENTER"));
    }

    @Test
    void dispatchedDeliveryCannotBeCancelled() {
        int id = service.schedule("order-1", "Abay 1", "CENTER");
        service.dispatch(id);
        assertThrows(DeliveryException.class, () -> service.cancel(id));
    }
}