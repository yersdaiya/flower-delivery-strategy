package flowerdelivery.inventory;

public class Reservation {
    public enum Status { HELD, CONFIRMED, RELEASED }

    private final int id;
    private final String orderId;
    private final int flowerId;
    private final int quantity;
    private Status status;

    public Reservation(int id, String orderId, int flowerId, int quantity, Status status) {
        this.id = id;
        this.orderId = orderId;
        this.flowerId = flowerId;
        this.quantity = quantity;
        this.status = status;
    }

    public void confirm() {
        if (status != Status.HELD)
            throw new InventoryException("Cannot confirm reservation in status " + status);
        status = Status.CONFIRMED;
    }

    public void release() {
        if (status != Status.HELD)
            throw new InventoryException("Cannot release reservation in status " + status);
        status = Status.RELEASED;
    }

    public int getId() { return id; }
    public String getOrderId() { return orderId; }
    public int getFlowerId() { return flowerId; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
}