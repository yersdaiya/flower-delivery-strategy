package flowerdelivery.facade;

import flowerdelivery.delivery.DeliveryService;
import flowerdelivery.inventory.InventoryService;
import flowerdelivery.payment.PaymentService;

import java.util.List;

public class FlowerDeliveryAdminFacade {
    private final InventoryService inventory;
    private final DeliveryService delivery;
    private final PaymentService payment;
    private final OrderProcessRepository processes;
    private final ChainCompensator compensator;

    public FlowerDeliveryAdminFacade(InventoryService inventory, DeliveryService delivery,
                                     PaymentService payment, OrderProcessRepository processes) {
        this.inventory = inventory;
        this.delivery = delivery;
        this.payment = payment;
        this.processes = processes;
        this.compensator = new ChainCompensator(inventory, delivery, payment, processes);
    }

    public List<OrderProcess> listAll() { return processes.findAll(); }

    public List<OrderProcess> listUnfinished() { return processes.findUnfinished(); }

    public String inspect(String orderId) {
        OrderProcess p = find(orderId);
        return "Order " + p.getOrderId() + " | chain: " + p.getStatus()
                + "\n  A reservation: " + (p.getReservationId() == null ? "-"
                : inventory.reservationStatus(p.getReservationId()))
                + "\n  B delivery:    " + (p.getDeliveryId() == null ? "-"
                : delivery.status(p.getDeliveryId()))
                + "\n  C payment:     " + (p.getPaymentId() == null ? "-"
                : payment.status(p.getPaymentId()))
                + (p.getFailureReason() == null ? "" : "\n  reason: " + p.getFailureReason());
    }

    public void compensateManually(String orderId) {
        OrderProcess p = find(orderId);
        if (p.getStatus() == ProcessStatus.COMPLETED || p.getStatus() == ProcessStatus.COMPENSATED) {
            throw new OrderFailedException("Nothing to compensate, order is " + p.getStatus());
        }
        p.setFailureReason("Compensated manually by admin");
        compensator.compensate(p);
    }

    private OrderProcess find(String orderId) {
        return processes.find(orderId)
                .orElseThrow(() -> new OrderFailedException("Order not found: " + orderId));
    }
}