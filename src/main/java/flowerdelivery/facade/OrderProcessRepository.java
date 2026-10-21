package flowerdelivery.facade;

import flowerdelivery.common.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderProcessRepository {
    private final Database db;

    public OrderProcessRepository(Database db) {
        this.db = db;
        createTable();
    }

    private void createTable() {
        try (Connection c = db.connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS order_process (" +
                    "order_id TEXT PRIMARY KEY, flower_id INTEGER NOT NULL, quantity INTEGER NOT NULL, " +
                    "address TEXT NOT NULL, zone TEXT NOT NULL, amount INTEGER NOT NULL, " +
                    "reservation_id INTEGER, delivery_id INTEGER, payment_id INTEGER, " +
                    "status TEXT NOT NULL, failure_reason TEXT)");
        } catch (SQLException e) {
            throw new OrderFailedException("Cannot create order_process table", e);
        }
    }

    public void insert(OrderProcess p) {
        String sql = "INSERT INTO order_process (order_id, flower_id, quantity, address, zone, amount, " +
                "reservation_id, delivery_id, payment_id, status, failure_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getOrderId());
            ps.setInt(2, p.getFlowerId());
            ps.setInt(3, p.getQuantity());
            ps.setString(4, p.getAddress());
            ps.setString(5, p.getZone());
            ps.setInt(6, p.getAmount());
            setNullable(ps, 7, p.getReservationId());
            setNullable(ps, 8, p.getDeliveryId());
            setNullable(ps, 9, p.getPaymentId());
            ps.setString(10, p.getStatus().name());
            ps.setString(11, p.getFailureReason());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new OrderFailedException("Cannot save order process", e);
        }
    }

    public void update(OrderProcess p) {
        String sql = "UPDATE order_process SET reservation_id = ?, delivery_id = ?, payment_id = ?, " +
                "status = ?, failure_reason = ? WHERE order_id = ?";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            setNullable(ps, 1, p.getReservationId());
            setNullable(ps, 2, p.getDeliveryId());
            setNullable(ps, 3, p.getPaymentId());
            ps.setString(4, p.getStatus().name());
            ps.setString(5, p.getFailureReason());
            ps.setString(6, p.getOrderId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new OrderFailedException("Cannot update order process", e);
        }
    }

    public Optional<OrderProcess> find(String orderId) {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM order_process WHERE order_id = ?")) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new OrderFailedException("Cannot read order process", e);
        }
    }

    public List<OrderProcess> findAll() {
        return query("SELECT * FROM order_process ORDER BY rowid");
    }

    public List<OrderProcess> findUnfinished() {
        return query("SELECT * FROM order_process " +
                "WHERE status IN ('PENDING_B', 'PENDING_C', 'COMPENSATING') ORDER BY rowid");
    }

    private List<OrderProcess> query(String sql) {
        List<OrderProcess> result = new ArrayList<>();
        try (Connection c = db.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) result.add(map(rs));
            return result;
        } catch (SQLException e) {
            throw new OrderFailedException("Cannot read order processes", e);
        }
    }

    private OrderProcess map(ResultSet rs) throws SQLException {
        OrderProcess p = new OrderProcess(rs.getString("order_id"), rs.getInt("flower_id"),
                rs.getInt("quantity"), rs.getString("address"), rs.getString("zone"), rs.getInt("amount"));
        p.setReservationId(nullableInt(rs, "reservation_id"));
        p.setDeliveryId(nullableInt(rs, "delivery_id"));
        p.setPaymentId(nullableInt(rs, "payment_id"));
        p.setStatus(ProcessStatus.valueOf(rs.getString("status")));
        p.setFailureReason(rs.getString("failure_reason"));
        return p;
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static void setNullable(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }
}
