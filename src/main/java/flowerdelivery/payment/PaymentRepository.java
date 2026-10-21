package flowerdelivery.payment;

import flowerdelivery.common.Database;

import java.sql.*;
import java.util.Optional;

public class PaymentRepository {
    private final Database db;

    public PaymentRepository(Database db) {
        this.db = db;
        createTable();
    }

    private void createTable() {
        try (Connection c = db.connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, order_id TEXT NOT NULL, " +
                    "card_last4 TEXT NOT NULL, amount INTEGER NOT NULL, status TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new PaymentException("Cannot create table", e);
        }
    }

    public int insert(Payment p) {
        String sql = "INSERT INTO payments (order_id, card_last4, amount, status) VALUES (?, ?, ?, ?)";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getOrderId());
            ps.setString(2, p.getCardLast4());
            ps.setInt(3, p.getAmount());
            ps.setString(4, p.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new PaymentException("Cannot save payment", e);
        }
    }

    public Optional<Payment> find(int id) {
        String sql = "SELECT id, order_id, card_last4, amount, status FROM payments WHERE id = ?";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Payment(
                        rs.getInt("id"), rs.getString("order_id"), rs.getString("card_last4"),
                        rs.getInt("amount"), Payment.Status.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            throw new PaymentException("Cannot read payment", e);
        }
    }

    public void updateStatus(int id, Payment.Status status) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE payments SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PaymentException("Cannot update payment", e);
        }
    }
}