package flowerdelivery.inventory;

import flowerdelivery.common.Database;

import java.sql.*;
import java.util.Optional;

public class InventoryRepository {
    private final Database db;

    public InventoryRepository(Database db) {
        this.db = db;
        createTables();
    }

    private void createTables() {
        try (Connection c = db.connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS flowers (" +
                    "id INTEGER PRIMARY KEY, name TEXT NOT NULL, quantity INTEGER NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, order_id TEXT NOT NULL, " +
                    "flower_id INTEGER NOT NULL, quantity INTEGER NOT NULL, status TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new InventoryException("Cannot create tables", e);
        }
    }

    public void saveFlower(Flower f) {
        String sql = "INSERT OR REPLACE INTO flowers (id, name, quantity) VALUES (?, ?, ?)";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, f.getId());
            ps.setString(2, f.getName());
            ps.setInt(3, f.getQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new InventoryException("Cannot save flower", e);
        }
    }

    public Optional<Flower> findFlower(int id) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, quantity FROM flowers WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Flower(rs.getInt("id"), rs.getString("name"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            throw new InventoryException("Cannot read flower", e);
        }
    }

    public void updateFlowerQuantity(int id, int quantity) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE flowers SET quantity = ? WHERE id = ?")) {
            ps.setInt(1, quantity);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new InventoryException("Cannot update flower", e);
        }
    }

    public int insertReservation(Reservation r) {
        String sql = "INSERT INTO reservations (order_id, flower_id, quantity, status) VALUES (?, ?, ?, ?)";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getOrderId());
            ps.setInt(2, r.getFlowerId());
            ps.setInt(3, r.getQuantity());
            ps.setString(4, r.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new InventoryException("Cannot save reservation", e);
        }
    }

    public Optional<Reservation> findReservation(int id) {
        String sql = "SELECT id, order_id, flower_id, quantity, status FROM reservations WHERE id = ?";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Reservation(
                        rs.getInt("id"), rs.getString("order_id"), rs.getInt("flower_id"),
                        rs.getInt("quantity"), Reservation.Status.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            throw new InventoryException("Cannot read reservation", e);
        }
    }

    public void updateReservationStatus(int id, Reservation.Status status) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE reservations SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new InventoryException("Cannot update reservation", e);
        }
    }
}