package flowerdelivery.payment;

import flowerdelivery.common.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {
    private static final String GOOD_CARD = "4111111111111111";
    private static final String DECLINED_CARD = "4111111111110000";

    @TempDir Path dir;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        PaymentRepository repo = new PaymentRepository(new Database(dir.resolve("pay.db").toString()));
        service = new PaymentService(repo, new PaymentRules());
    }

    @Test
    void chargeMarksPaymentAsPaid() {
        int id = service.charge("order-1", GOOD_CARD, 5000);
        assertEquals(Payment.Status.PAID, service.status(id));
    }

    @Test
    void refundMarksPaymentAsRefunded() {
        int id = service.charge("order-1", GOOD_CARD, 5000);
        service.refund(id);
        assertEquals(Payment.Status.REFUNDED, service.status(id));
    }

    @Test
    void declinedCardFails() {
        assertThrows(PaymentException.class, () -> service.charge("order-1", DECLINED_CARD, 5000));
    }

    @Test
    void invalidCardNumberFails() {
        assertThrows(PaymentException.class, () -> service.charge("order-1", "123", 5000));
    }

    @Test
    void invalidAmountFails() {
        assertThrows(PaymentException.class, () -> service.charge("order-1", GOOD_CARD, 0));
    }

    @Test
    void refundedPaymentCannotBeRefundedAgain() {
        int id = service.charge("order-1", GOOD_CARD, 5000);
        service.refund(id);
        assertThrows(PaymentException.class, () -> service.refund(id));
    }
}