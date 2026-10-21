# Flower Delivery: Facade over a Three-Stage Chain

Assignment 4: a Facade layer over three independent subsystems, with durable state (SQLite),
two-depth compensation and a runnable console demo.

## The domain

A customer orders flowers. One logical operation, `placeOrder`, drives a chain of three subsystems:

| Step | Subsystem (package) | What it does | Entity status |
|---|---|---|---|
| **A** | `inventory` | reserves the flowers | `Reservation`: HELD → CONFIRMED / RELEASED |
| **B** | `delivery` | schedules a courier for a zone | `Delivery`: SCHEDULED → DISPATCHED / CANCELLED |
| **C** | `payment` | charges the card | `Payment`: PENDING → PAID → REFUNDED |

Each subsystem has its own pure business-logic class (`StockRules`, `ZoneRules`, `PaymentRules`),
its own persistence gateway (`InventoryRepository`, `DeliveryRepository`, `PaymentRepository`),
its own entity with legal status transitions, its own service and its own exception type.
No subsystem imports another subsystem or the facade. Each can be used alone (see the tests in
`src/test/java/flowerdelivery/{inventory,delivery,payment}`).

### Facades (`flowerdelivery.facade`)

* `FlowerDeliveryFacade`: the client workflow. `placeOrder(...)` runs A → B → C with real sequencing and
  decision logic; `resumePending()` is called at start-up and rolls back chains interrupted by a restart.
* `FlowerDeliveryAdminFacade`: the narrow administrative facade: `listAll`, `listUnfinished`, `inspect`,
  `compensateManually`.
* `ChainCompensator` (package-private): the shared rollback logic. It reads the persisted status of each
  step and only undoes what is really on disk (C if `PAID`, B if `SCHEDULED`, A if `HELD`), so it is safe to run twice.

### Durable state

Everything lives in one SQLite file, `data/flowers.db`. Each subsystem writes its own tables.
Chain progress is stored in the facade-owned table `order_process`
(`PENDING_B`, `PENDING_C`, `COMPLETED`, `COMPENSATING`, `COMPENSATED`) together with the ids of the
reservation, delivery and payment. After a restart the facade reads this table, not memory.

## Run it

Requirements: JDK 17+ and Maven.

```bash
mvn -q compile exec:java -Dexec.mainClass=flowerdelivery.Main
```

(Alternatively, run `Main` from the IDE.)

**Reset the persisted store to a clean state:** choose menu option `8`, or delete the file `data/flowers.db`
before starting. The stock is re-seeded automatically (Rose 50, Tulip 30, Lily 20).

### Demo menu

| Option | What it shows |
|---|---|
| 1 | flower stock |
| 2 | place an order with your own input |
| 3 | B fails after A committed: only A is compensated |
| 4 | C fails after A and B committed: B is cancelled, then A is released |
| 5 | all orders, read from disk |
| 6 | inspect one order (status of A, B, C and the failure reason) |
| 7 | compensate an unfinished order manually |
| 8 | reset all data |

How the failures are reached with real input:

* **B fails**: the zone is not covered (anything except `CENTER`, `LEFT_BANK`, `RIGHT_BANK`) or the zone already
  has 3 scheduled deliveries ("no courier available").
* **C fails**: the card is not 16 digits, or ends with `0000` (declined by the bank), or the amount is out of range.
* **Failure before anything commits**: ordering more flowers than are in stock, a quantity outside 1-50# flower-delivery

/// package flowerdelivery.facade;

import flowerdelivery.inventory.InventoryService;
import flowerdelivery.delivery.DeliveryService;

public class FlowerDeliveryPartnerFacade {
private final InventoryService inventoryService;
private final DeliveryService deliveryService;

    public FlowerDeliveryPartnerFacade(InventoryService inventoryService, DeliveryService deliveryService) {
        this.inventoryService = inventoryService;
        this.deliveryService = deliveryService;
    }

    public int checkPartnerStock(int flowerId) {
        return inventoryService.availableQuantity(flowerId);
    }
}