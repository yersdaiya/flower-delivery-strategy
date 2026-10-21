package flowerdelivery.facade;

import flowerdelivery.common.Database;
import flowerdelivery.delivery.*;
import flowerdelivery.inventory.*;
import flowerdelivery.payment.*;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Test DSL: tests read like sentences about a flower shop. */
class FlowerShop {
    static final int ROSE = 1;
    static final String GOOD_CARD = "4111111111111111";
    static final String DECLINED_CARD = "4111111111110000";

    private final Path dbFile;
    private final InventoryRepository inventoryRepo;
    private final InventoryService inventory;
    private final DeliveryService delivery;
    private final PaymentService payment;
    private final OrderProcessRepository processes;
    private final FlowerDeliveryFacade facade;
    private final FlowerDeliveryAdminFacade admin;

    private FlowerShop(Path dbFile) {
        this.dbFile = dbFile;
        Database db = new Database(dbFile.toString());
        this.inventoryRepo = new InventoryRepository(db);
        this.inventory = new InventoryService(inventoryRepo, new StockRules());
        this.delivery = new DeliveryService(new DeliveryRepository(db), new ZoneRules());
        this.payment = new PaymentService(new PaymentRepository(db), new PaymentRules());
        this.processes = new OrderProcessRepository(db);
        this.facade = new FlowerDeliveryFacade(inventory, delivery, payment, processes);
        this.admin = new FlowerDeliveryAdminFacade(inventory, delivery, payment, processes);
    }

    static FlowerShop openWithRoses(Path dbFile, int stock) {
        FlowerShop shop = new FlowerShop(dbFile);
        shop.inventoryRepo.saveFlower(new Flower(ROSE, "Rose", stock));
        return shop;
    }

    /** Simulates an application restart: new objects, same file on disk. */
    FlowerShop restart() {
        return new FlowerShop(dbFile);
    }

    // ---- actions ----

    String placeOrder(int roses, String zone, String card) {
        return facade.placeOrder(ROSE, roses, "Abay 1", zone, card, 5000);
    }

    /** A and B are committed on disk, chain is waiting for C (as if the app crashed). */
    String givenChainInterruptedAfterStepB(int roses, String zone) {
        String orderId = "interrupted-1";
        int reservationId = inventory.reserve(orderId, ROSE, roses);
        int deliveryId = delivery.schedule(orderId, "Abay 1", zone);
        OrderProcess p = new OrderProcess(orderId, ROSE, roses, "Abay 1", zone, 5000);
        p.setReservationId(reservationId);
        p.setDeliveryId(deliveryId);
        p.setStatus(ProcessStatus.PENDING_C);
        processes.insert(p);
        return orderId;
    }

    int resumePending() { return facade.resumePending(); }

    void compensateManually(String orderId) { admin.compensateManually(orderId); }

    String inspect(String orderId) { return admin.inspect(orderId); }

    // ---- assertions ----

    FlowerShop thenRosesInStockAre(int expected) {
        assertEquals(expected, inventory.availableQuantity(ROSE));
        return this;
    }

    FlowerShop thenOrderChainIs(ProcessStatus expected) {
        assertEquals(expected, onlyOrder().getStatus());
        return this;
    }

    FlowerShop thenReservationIs(Reservation.Status expected) {
        assertEquals(expected, inventory.reservationStatus(onlyOrder().getReservationId()));
        return this;
    }

    FlowerShop thenDeliveryIs(Delivery.Status expected) {
        assertEquals(expected, delivery.status(onlyOrder().getDeliveryId()));
        return this;
    }

    FlowerShop thenPaymentIs(Payment.Status expected) {
        assertEquals(expected, payment.status(onlyOrder().getPaymentId()));
        return this;
    }

    FlowerShop thenNoDeliveryWasCreated() {
        assertNull(onlyOrder().getDeliveryId());
        return this;
    }

    FlowerShop thenNoPaymentWasCreated() {
        assertNull(onlyOrder().getPaymentId());
        return this;
    }

    FlowerShop thenNoOrderWasRecorded() {
        assertTrue(admin.listAll().isEmpty());
        return this;
    }

    private OrderProcess onlyOrder() {
        List<OrderProcess> all = admin.listAll();
        assertEquals(1, all.size(), "expected exactly one order on disk");
        return all.get(0);
    }
}