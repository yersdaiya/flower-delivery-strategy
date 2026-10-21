package flowerdelivery.delivery;

import flowerdelivery.common.Database;

import java.sql.*;
import java.util.Optional;

public class DeliveryRepository {
    private final Database db;

    public DeliveryRepository(Database db) {
        this.db = db;
        createTable();
    }

    private void createTable() {
        try (Connection c = db.connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS deliveries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, order_id TEXT NOT NULL, " +
                    "address TEXT NOT NULL, zone TEXT NOT NULL, status TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new DeliveryException("Cannot create table", e);
        }
    }

    public int insert(Delivery d) {
        String sql = "INSERT INTO deliveries (order_id, address, zone, status) VALUES (?, ?, ?, ?)";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getOrderId());
            ps.setString(2, d.getAddress());
            ps.setString(3, d.getZone());
            ps.setString(4, d.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DeliveryException("Cannot save delivery", e);
        }
    }

    public Optional<Delivery> find(int id) {
        String sql = "SELECT id, order_id, address, zone, status FROM deliveries WHERE id = ?";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Delivery(
                        rs.getInt("id"), rs.getString("order_id"), rs.getString("address"),
                        rs.getString("zone"), Delivery.Status.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            throw new DeliveryException("Cannot read delivery", e);
        }
    }

    public void updateStatus(int id, Delivery.Status status) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE deliveries SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DeliveryException("Cannot update delivery", e);
        }
    }

    public int countScheduledInZone(String zone) {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE zone = ? AND status = 'SCHEDULED'";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, zone);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DeliveryException("Cannot count deliveries", e);
        }
    }
}