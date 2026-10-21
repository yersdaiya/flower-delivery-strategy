package flowerdelivery.inventory;

import flowerdelivery.common.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {
    @TempDir Path dir;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        InventoryRepository repo = new InventoryRepository(new Database(dir.resolve("inv.db").toString()));
        repo.saveFlower(new Flower(1, "Rose", 20));
        service = new InventoryService(repo, new StockRules());
    }

    @Test
    void reserveReducesStock() {
        int id = service.reserve("order-1", 1, 5);
        assertEquals(15, service.availableQuantity(1));
        assertEquals(Reservation.Status.HELD, service.reservationStatus(id));
    }

    @Test
    void releaseRestoresStock() {
        int id = service.reserve("order-1", 1, 5);
        service.release(id);
        assertEquals(20, service.availableQuantity(1));
        assertEquals(Reservation.Status.RELEASED, service.reservationStatus(id));
    }

    @Test
    void reservingMoreThanAvailableFails() {
        assertThrows(InventoryException.class, () -> service.reserve("order-1", 1, 25));
        assertEquals(20, service.availableQuantity(1));
    }

    @Test
    void releasedReservationCannotBeConfirmed() {
        int id = service.reserve("order-1", 1, 5);
        service.release(id);
        assertThrows(InventoryException.class, () -> service.confirm(id));
    }
}