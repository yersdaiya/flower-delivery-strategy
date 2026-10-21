package flowerdelivery.payment;

public class PaymentService {
    private final PaymentRepository repository;
    private final PaymentRules rules;

    public PaymentService(PaymentRepository repository, PaymentRules rules) {
        this.repository = repository;
        this.rules = rules;
    }

    public int charge(String orderId, String cardNumber, int amount) {
        rules.validate(cardNumber, amount);
        int id = repository.insert(
                new Payment(0, orderId, rules.lastFour(cardNumber), amount, Payment.Status.PENDING));
        Payment payment = get(id);
        payment.pay();
        repository.updateStatus(id, payment.getStatus());
        return id;
    }

    public void refund(int paymentId) {
        Payment payment = get(paymentId);
        payment.refund();
        repository.updateStatus(paymentId, payment.getStatus());
    }

    public Payment.Status status(int paymentId) {
        return get(paymentId).getStatus();
    }

    private Payment get(int id) {
        return repository.find(id)
                .orElseThrow(() -> new PaymentException("Payment not found: " + id));
    }
}