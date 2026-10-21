package flowerdelivery.inventory;

public class InventoryService {
    private final InventoryRepository repository;
    private final StockRules rules;

    public InventoryService(InventoryRepository repository, StockRules rules) {
        this.repository = repository;
        this.rules = rules;
    }

    public int reserve(String orderId, int flowerId, int quantity) {
        Flower flower = repository.findFlower(flowerId)
                .orElseThrow(() -> new InventoryException("Flower not found: " + flowerId));
        rules.validate(quantity, flower.getQuantity());
        flower.decrease(quantity);
        repository.updateFlowerQuantity(flowerId, flower.getQuantity());
        return repository.insertReservation(
                new Reservation(0, orderId, flowerId, quantity, Reservation.Status.HELD));
    }

    public void confirm(int reservationId) {
        Reservation r = getReservation(reservationId);
        r.confirm();
        repository.updateReservationStatus(reservationId, r.getStatus());
    }

    public void release(int reservationId) {
        Reservation r = getReservation(reservationId);
        r.release();
        Flower flower = repository.findFlower(r.getFlowerId())
                .orElseThrow(() -> new InventoryException("Flower not found: " + r.getFlowerId()));
        flower.increase(r.getQuantity());
        repository.updateFlowerQuantity(flower.getId(), flower.getQuantity());
        repository.updateReservationStatus(reservationId, r.getStatus());
    }

    public int availableQuantity(int flowerId) {
        return repository.findFlower(flowerId)
                .orElseThrow(() -> new InventoryException("Flower not found: " + flowerId))
                .getQuantity();
    }

    public Reservation.Status reservationStatus(int reservationId) {
        return getReservation(reservationId).getStatus();
    }

    private Reservation getReservation(int id) {
        return repository.findReservation(id)
                .orElseThrow(() -> new InventoryException("Reservation not found: " + id));
    }
}