package flowerdelivery;

import flowerdelivery.common.Database;
import flowerdelivery.delivery.DeliveryRepository;
import flowerdelivery.delivery.DeliveryService;
import flowerdelivery.delivery.ZoneRules;
import flowerdelivery.facade.*;
import flowerdelivery.inventory.*;
import flowerdelivery.payment.PaymentRepository;
import flowerdelivery.payment.PaymentRules;
import flowerdelivery.payment.PaymentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Path DB_FILE = Path.of("data", "flowers.db");
    private static final String GOOD_CARD = "4111111111111111";
    private static final String DECLINED_CARD = "4111111111110000";

    private final Scanner in = new Scanner(System.in);
    private InventoryRepository inventoryRepo;
    private FlowerDeliveryFacade facade;
    private FlowerDeliveryAdminFacade admin;

    public static void main(String[] args) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        wire();
        int recovered = facade.resumePending();
        if (recovered > 0) {
            System.out.println(">> After restart: " + recovered + " unfinished order(s) were rolled back.");
        }
        boolean running = true;
        while (running) {
            printMenu();
            switch (readLine("> ").trim()) {
                case "1" -> showStock();
                case "2" -> placeOrderInteractive();
                case "3" -> demoDeliveryFailure();
                case "4" -> demoPaymentFailure();
                case "5" -> listOrders();
                case "6" -> inspectOrder();
                case "7" -> compensateOrder();
                case "8" -> reset();
                case "0" -> running = false;
                default -> System.out.println("Unknown option.");
            }
        }
        System.out.println("Bye.");
    }

    /** Composition root: the only place that knows the subsystems. The menu below talks to facades only. */
    private void wire() throws IOException {
        Files.createDirectories(DB_FILE.getParent());
        Database db = new Database(DB_FILE.toString());
        inventoryRepo = new InventoryRepository(db);
        InventoryService inventory = new InventoryService(inventoryRepo, new StockRules());
        DeliveryService delivery = new DeliveryService(new DeliveryRepository(db), new ZoneRules());
        PaymentService payment = new PaymentService(new PaymentRepository(db), new PaymentRules());
        OrderProcessRepository processes = new OrderProcessRepository(db);
        facade = new FlowerDeliveryFacade(inventory, delivery, payment, processes);
        admin = new FlowerDeliveryAdminFacade(inventory, delivery, payment, processes);
        seedFlowersIfEmpty();
    }

    private void seedFlowersIfEmpty() {
        if (inventoryRepo.findFlower(1).isEmpty()) {
            inventoryRepo.saveFlower(new Flower(1, "Rose", 50));
            inventoryRepo.saveFlower(new Flower(2, "Tulip", 30));
            inventoryRepo.saveFlower(new Flower(3, "Lily", 20));
        }
    }

    private void printMenu() {
        System.out.println("\n===== Flower Delivery =====");
        System.out.println("1. Show flower stock");
        System.out.println("2. Place an order (enter your own data)");
        System.out.println("3. DEMO: delivery fails (step B)  -> only A is rolled back");
        System.out.println("4. DEMO: payment fails (step C)   -> B, then A are rolled back");
        System.out.println("5. List all orders (from disk)");
        System.out.println("6. Inspect one order");
        System.out.println("7. Compensate an unfinished order manually");
        System.out.println("8. RESET all data");
        System.out.println("0. Exit");
    }

    private void showStock() {
        for (int id = 1; id <= 3; id++) {
            inventoryRepo.findFlower(id).ifPresent(f ->
                    System.out.println("  #" + f.getId() + " " + f.getName() + ": " + f.getQuantity()));
        }
    }

    private void placeOrderInteractive() {
        try {
            System.out.println("Zones: CENTER, LEFT_BANK, RIGHT_BANK (anything else is not covered)");
            System.out.println("Card: 16 digits (ending with 0000 is declined by the bank)");
            int flowerId = Integer.parseInt(readLine("Flower id (1-3): ").trim());
            int quantity = Integer.parseInt(readLine("Quantity: ").trim());
            String address = readLine("Address: ").trim();
            String zone = readLine("Zone: ").trim().toUpperCase();
            String card = readLine("Card number: ").trim();
            int amount = Integer.parseInt(readLine("Amount: ").trim());
            submit(flowerId, quantity, address, zone, card, amount);
        } catch (NumberFormatException e) {
            System.out.println("Please enter numbers where numbers are expected.");
        }
    }

    private void demoDeliveryFailure() {
        System.out.println("Ordering 5 roses to zone REMOTE (not covered) with a good card...");
        submit(1, 5, "Far village 1", "REMOTE", GOOD_CARD, 5000);
    }

    private void demoPaymentFailure() {
        System.out.println("Ordering 5 roses to CENTER with a card that the bank declines...");
        submit(1, 5, "Abay 10", "CENTER", DECLINED_CARD, 5000);
    }

    private void submit(int flowerId, int quantity, String address, String zone, String card, int amount) {
        int before = admin.listAll().size();
        try {
            String orderId = facade.placeOrder(flowerId, quantity, address, zone, card, amount);
            System.out.println("SUCCESS: order " + orderId + " completed.");
            System.out.println(admin.inspect(orderId));
        } catch (OrderFailedException e) {
            System.out.println("ORDER FAILED: " + e.getMessage());
            List<OrderProcess> all = admin.listAll();
            if (all.size() > before) {
                System.out.println(admin.inspect(all.get(all.size() - 1).getOrderId()));
            } else {
                System.out.println("(nothing was committed, so there is nothing to roll back)");
            }
        }
    }

    private void listOrders() {
        List<OrderProcess> all = admin.listAll();
        if (all.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        for (OrderProcess p : all) {
            System.out.println("  " + p.getOrderId() + " | " + p.getStatus() + " | zone " + p.getZone()
                    + (p.getFailureReason() == null ? "" : " | " + p.getFailureReason()));
        }
    }

    private void inspectOrder() {
        String orderId = readLine("Order id: ").trim();
        try {
            System.out.println(admin.inspect(orderId));
        } catch (OrderFailedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void compensateOrder() {
        List<OrderProcess> unfinished = admin.listUnfinished();
        if (unfinished.isEmpty()) {
            System.out.println("No unfinished orders to compensate.");
            return;
        }
        unfinished.forEach(p -> System.out.println("  " + p.getOrderId() + " | " + p.getStatus()));
        String orderId = readLine("Order id to compensate: ").trim();
        try {
            admin.compensateManually(orderId);
            System.out.println(admin.inspect(orderId));
        } catch (OrderFailedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void reset() throws IOException {
        Files.deleteIfExists(DB_FILE);
        wire();
        System.out.println("All data deleted, stock restored to the defaults.");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine() : "0";
    }
}