package flowerdelivery.facade;

public class OrderProcess {
    private final String orderId;
    private final int flowerId;
    private final int quantity;
    private final String address;
    private final String zone;
    private final int amount;
    private Integer reservationId;
    private Integer deliveryId;
    private Integer paymentId;
    private ProcessStatus status;
    private String failureReason;

    public OrderProcess(String orderId, int flowerId, int quantity,
                        String address, String zone, int amount) {
        this.orderId = orderId;
        this.flowerId = flowerId;
        this.quantity = quantity;
        this.address = address;
        this.zone = zone;
        this.amount = amount;
    }

    public String getOrderId() { return orderId; }
    public int getFlowerId() { return flowerId; }
    public int getQuantity() { return quantity; }
    public String getAddress() { return address; }
    public String getZone() { return zone; }
    public int getAmount() { return amount; }
    public Integer getReservationId() { return reservationId; }
    public Integer getDeliveryId() { return deliveryId; }
    public Integer getPaymentId() { return paymentId; }
    public ProcessStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }

    public void setReservationId(Integer id) { this.reservationId = id; }
    public void setDeliveryId(Integer id) { this.deliveryId = id; }
    public void setPaymentId(Integer id) { this.paymentId = id; }
    public void setStatus(ProcessStatus status) { this.status = status; }
    public void setFailureReason(String reason) { this.failureReason = reason; }
}