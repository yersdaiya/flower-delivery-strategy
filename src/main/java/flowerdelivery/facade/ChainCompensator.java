package flowerdelivery.facade;

import flowerdelivery.delivery.Delivery;
import flowerdelivery.delivery.DeliveryService;
import flowerdelivery.inventory.InventoryService;
import flowerdelivery.inventory.Reservation;
import flowerdelivery.payment.Payment;
import flowerdelivery.payment.PaymentService;

class ChainCompensator {
    private final InventoryService inventory;
    private final DeliveryService delivery;
    private final PaymentService payment;
    private final OrderProcessRepository processes;

    ChainCompensator(InventoryService inventory, DeliveryService delivery,
                     PaymentService payment, OrderProcessRepository processes) {
        this.inventory = inventory;
        this.delivery = delivery;
        this.payment = payment;
        this.processes = processes;
    }

    /** Reverses only what is actually persisted: C, then B, then A. Safe to call twice. */
    void compensate(OrderProcess p) {
        p.setStatus(ProcessStatus.COMPENSATING);
        processes.update(p);

        if (p.getPaymentId() != null && payment.status(p.getPaymentId()) == Payment.Status.PAID) {
            payment.refund(p.getPaymentId());
        }
        if (p.getDeliveryId() != null && delivery.status(p.getDeliveryId()) == Delivery.Status.SCHEDULED) {
            delivery.cancel(p.getDeliveryId());
        }
        if (p.getReservationId() != null
                && inventory.reservationStatus(p.getReservationId()) == Reservation.Status.HELD) {
            inventory.release(p.getReservationId());
        }

        p.setStatus(ProcessStatus.COMPENSATED);
        processes.update(p);
    }
}