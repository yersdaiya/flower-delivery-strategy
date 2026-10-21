package flowerdelivery.facade;

import flowerdelivery.delivery.DeliveryException;
import flowerdelivery.delivery.DeliveryService;
import flowerdelivery.inventory.InventoryException;
import flowerdelivery.inventory.InventoryService;
import flowerdelivery.payment.PaymentException;
import flowerdelivery.payment.PaymentService;

import java.util.List;
import java.util.UUID;

public class FlowerDeliveryFacade {
    private final InventoryService inventory;
    private final DeliveryService delivery;
    private final PaymentService payment;
    private final OrderProcessRepository processes;
    private final ChainCompensator compensator;

    public FlowerDeliveryFacade(InventoryService inventory, DeliveryService delivery,
                                PaymentService payment, OrderProcessRepository processes) {
        this.inventory = inventory;
        this.delivery = delivery;
        this.payment = payment;
        this.processes = processes;
        this.compensator = new ChainCompensator(inventory, delivery, payment, processes);
    }

    public String placeOrder(int flowerId, int quantity, String address, String zone,
                             String cardNumber, int amount) {
        String orderId = UUID.randomUUID().toString().substring(0, 8);

        // Step A: reserve flowers (if this fails, nothing has been committed)
        int reservationId;
        try {
            reservationId = inventory.reserve(orderId, flowerId, quantity);
        } catch (InventoryException e) {
            throw new OrderFailedException("Step A (inventory) failed: " + e.getMessage(), e);
        }
        OrderProcess process = new OrderProcess(orderId, flowerId, quantity, address, zone, amount);
        process.setReservationId(reservationId);
        process.setStatus(ProcessStatus.PENDING_B);
        processes.insert(process);

        // Step B: schedule delivery (failure -> compensate A only)
        try {
            process.setDeliveryId(delivery.schedule(orderId, address, zone));
            process.setStatus(ProcessStatus.PENDING_C);
            processes.update(process);
        } catch (DeliveryException e) {
            process.setFailureReason("Step B (delivery) failed: " + e.getMessage());
            compensator.compensate(process);
            throw new OrderFailedException(process.getFailureReason(), e);
        }

        // Step C: take payment (failure -> compensate B, then A)
        try {
            process.setPaymentId(payment.charge(orderId, cardNumber, amount));
            processes.update(process);
        } catch (PaymentException e) {
            process.setFailureReason("Step C (payment) failed: " + e.getMessage());
            compensator.compensate(process);
            throw new OrderFailedException(process.getFailureReason(), e);
        }

        // Finalize: flowers are consumed and the courier leaves
        inventory.confirm(process.getReservationId());
        delivery.dispatch(process.getDeliveryId());
        process.setStatus(ProcessStatus.COMPLETED);
        processes.update(process);
        return orderId;
    }

    /** Call on application start: rolls back chains that were interrupted by a restart. */
    public int resumePending() {
        List<OrderProcess> unfinished = processes.findUnfinished();
        for (OrderProcess p : unfinished) {
            p.setFailureReason("Interrupted by restart");
            compensator.compensate(p);
        }
        return unfinished.size();
    }
}