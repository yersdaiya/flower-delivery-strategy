package flowerdelivery.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final String url;

    public Database(String filePath) {
        this.url = "jdbc:sqlite:" + filePath;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }
}