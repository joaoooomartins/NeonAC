package com.neonac.storage;

import com.neonac.api.storage.Storage;
import com.neonac.api.storage.StorageException;
import com.neonac.api.violation.Violation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class JdbcStorage implements Storage {

    protected Connection connection;
    protected final Logger logger = Logger.getLogger("NeonAC-Storage");
    private final List<Violation> recentViolations = new ArrayList<>();
    private int retryCount = 3;
    private long retryDelayMs = 500;

    protected abstract String buildUrl();
    protected abstract String[] schemaStatements();
    protected String[] getCredentials() {
        return null;
    }

    protected void configureRetries(int count, long delayMs) {
        this.retryCount = count;
        this.retryDelayMs = delayMs;
    }

    @Override
    public void init() throws StorageException {
        connect();
    }

    private void connect() throws StorageException {
        String url = buildUrl();
        try {
            String[] creds = getCredentials();
            this.connection = creds != null
                    ? DriverManager.getConnection(url, creds[0], creds[1])
                    : DriverManager.getConnection(url);
            try (Statement st = connection.createStatement()) {
                for (String sql : schemaStatements()) {
                    st.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to initialise JDBC storage: " + url, e);
        }
    }

    private boolean isValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection getConnection() throws SQLException {
        if (!isValid()) {
            logger.warning("[NeonAC] Connection lost, reconnecting...");
            reconnect();
        }
        return connection;
    }

    private void reconnect() throws SQLException {
        closeQuietly();
        String url = buildUrl();
        String[] creds = getCredentials();
        this.connection = creds != null
                ? DriverManager.getConnection(url, creds[0], creds[1])
                : DriverManager.getConnection(url);
    }

    private void closeQuietly() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void shutdown() {
        closeQuietly();
    }

    private <T> T executeWithRetry(String desc, SqlAction<T> action) {
        SQLException lastError = null;
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            try {
                return action.execute(getConnection());
            } catch (SQLException e) {
                lastError = e;
                logger.log(Level.WARNING, "[NeonAC] " + desc + " failed (attempt " + (attempt + 1) + "): " + e.getMessage());
                if (attempt < retryCount) {
                    try {
                        reconnect();
                        Thread.sleep(retryDelayMs * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (SQLException re) {
                        logger.log(Level.WARNING, "[NeonAC] Reconnect failed: " + re.getMessage());
                    }
                }
            }
        }
        if (lastError != null) {
            logger.log(Level.SEVERE, "[NeonAC] " + desc + " failed after " + (retryCount + 1) + " attempts", lastError);
        }
        return null;
    }

    @Override
    public void saveViolation(Violation violation) {
        synchronized (recentViolations) {
            recentViolations.add(0, violation);
            while (recentViolations.size() > 200) recentViolations.remove(recentViolations.size() - 1);
        }
        executeWithRetry("saveViolation", conn -> {
            String sql = "INSERT INTO neonac_violations (uuid, check_id, vl, confidence, ts) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, violation.getPlayerUuid());
                ps.setString(2, violation.getCheck().getId());
                ps.setDouble(3, violation.getViolationLevel());
                ps.setDouble(4, violation.getConfidence());
                ps.setLong(5, violation.getTimestamp());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public double getViolationLevel(UUID playerUuid, String checkId) {
        Double result = executeWithRetry("getVL", conn -> {
            String sql = "SELECT vl FROM neonac_vl WHERE uuid=? AND check_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setString(2, checkId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble("vl");
                }
            }
            return 0.0;
        });
        return result != null ? result : 0.0;
    }

    @Override
    public Map<String, Double> getAllViolationLevels(UUID playerUuid) {
        Map<String, Double> result = executeWithRetry("getAllVL", conn -> {
            Map<String, Double> map = new LinkedHashMap<>();
            String sql = "SELECT check_id, vl FROM neonac_vl WHERE uuid=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        map.put(rs.getString("check_id"), rs.getDouble("vl"));
                    }
                }
            }
            return map;
        });
        return result != null ? result : new LinkedHashMap<>();
    }

    @Override
    public void setViolationLevel(UUID playerUuid, String checkId, double vl) {
        executeWithRetry("setVL", conn -> {
            upsertVl(conn, playerUuid, checkId, vl);
            return null;
        });
    }

    protected abstract void upsertVl(Connection conn, UUID uuid, String checkId, double vl) throws SQLException;

    @Override
    public void resetViolationLevels(UUID playerUuid) {
        executeWithRetry("resetVL", conn -> {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM neonac_vl WHERE uuid=?")) {
                ps.setString(1, playerUuid.toString());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public List<Violation> getRecentViolations(UUID playerUuid, int limit) {
        return recentViolations.stream()
                .filter(v -> v.getPlayerUuid().equals(playerUuid.toString()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Object getRawHandle() {
        return connection;
    }

    @FunctionalInterface
    protected interface SqlAction<T> {
        T execute(Connection conn) throws SQLException;
    }
}
