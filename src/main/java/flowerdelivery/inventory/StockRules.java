package flowerdelivery.inventory;

public class StockRules {
    public static final int MAX_PER_ORDER = 50;

    public void validate(int requested, int available) {
        if (requested <= 0 || requested > MAX_PER_ORDER)
            throw new InventoryException("Quantity must be between 1 and " + MAX_PER_ORDER);
        if (requested > available)
            throw new InventoryException("Out of stock: requested " + requested + ", available " + available);
    }
}