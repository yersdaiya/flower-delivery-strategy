package flowerdelivery.payment;

public class Payment {
    public enum Status { PENDING, PAID, REFUNDED }

    private final int id;
    private final String orderId;
    private final String cardLast4;
    private final int amount;
    private Status status;

    public Payment(int id, String orderId, String cardLast4, int amount, Status status) {
        this.id = id;
        this.orderId = orderId;
        this.cardLast4 = cardLast4;
        this.amount = amount;
        this.status = status;
    }

    public void pay() {
        if (status != Status.PENDING)
            throw new PaymentException("Cannot pay payment in status " + status);
        status = Status.PAID;
    }

    public void refund() {
        if (status != Status.PAID)
            throw new PaymentException("Cannot refund payment in status " + status);
        status = Status.REFUNDED;
    }

    public int getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getCardLast4() { return cardLast4; }
    public int getAmount() { return amount; }
    public Status getStatus() { return status; }
}