package flowerdelivery.delivery;

public class DeliveryService {
    private final DeliveryRepository repository;
    private final ZoneRules rules;

    public DeliveryService(DeliveryRepository repository, ZoneRules rules) {
        this.repository = repository;
        this.rules = rules;
    }

    public int schedule(String orderId, String address, String zone) {
        if (address == null || address.isBlank())
            throw new DeliveryException("Address must not be empty");
        rules.validate(zone, repository.countScheduledInZone(zone));
        return repository.insert(new Delivery(0, orderId, address, zone, Delivery.Status.SCHEDULED));
    }

    public void dispatch(int deliveryId) {
        Delivery d = get(deliveryId);
        d.dispatch();
        repository.updateStatus(deliveryId, d.getStatus());
    }

    public void cancel(int deliveryId) {
        Delivery d = get(deliveryId);
        d.cancel();
        repository.updateStatus(deliveryId, d.getStatus());
    }

    public Delivery.Status status(int deliveryId) {
        return get(deliveryId).getStatus();
    }

    private Delivery get(int id) {
        return repository.find(id)
                .orElseThrow(() -> new DeliveryException("Delivery not found: " + id));
    }
}