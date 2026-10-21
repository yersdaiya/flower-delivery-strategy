
package flowerdelivery.inventory;

public class Flower {
    private final int id;
    private final String name;
    private int quantity;

    public Flower(int id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public void decrease(int amount) {
        if (amount > quantity) throw new InventoryException("Not enough flowers: " + name);
        quantity -= amount;
    }

    public void increase(int amount) { quantity += amount; }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
}