package flowerdelivery.delivery;

public class Delivery {
    public enum Status { SCHEDULED, DISPATCHED, CANCELLED }

    private final int id;
    private final String orderId;
    private final String address;
    private final String zone;
    private Status status;

    public Delivery(int id, String orderId, String address, String zone, Status status) {
        this.id = id;
        this.orderId = orderId;
        this.address = address;
        this.zone = zone;
        this.status = status;
    }

    public void dispatch() {
        if (status != Status.SCHEDULED)
            throw new DeliveryException("Cannot dispatch delivery in status " + status);
        status = Status.DISPATCHED;
    }

    public void cancel() {
        if (status != Status.SCHEDULED)
            throw new DeliveryException("Cannot cancel delivery in status " + status);
        status = Status.CANCELLED;
    }

    public int getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getAddress() { return address; }
    public String getZone() { return zone; }
    public Status getStatus() { return status; }
}