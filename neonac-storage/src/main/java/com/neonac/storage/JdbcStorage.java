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
abstract class JdbcStorage implements Storage {

    protected Connection connection;

    protected abstract String buildUrl();

    protected abstract String[] schemaStatements();
    protected String[] getCredentials() {
        return null;
    }

    @Override
    public void init() throws StorageException {
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

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void saveViolation(Violation violation) {
        String sql = "INSERT INTO NeonAC_violations (uuid, check_id, vl, confidence, ts) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, violation.getPlayerUuid());
            ps.setString(2, violation.getCheck().getId());
            ps.setDouble(3, violation.getViolationLevel());
            ps.setDouble(4, violation.getConfidence());
            ps.setLong(5, violation.getTimestamp());
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public double getViolationLevel(UUID playerUuid, String checkId) {
        String sql = "SELECT vl FROM NeonAC_vl WHERE uuid=? AND check_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, checkId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("vl");
            }
        } catch (SQLException ignored) {
        }
        return 0.0;
    }

    @Override
    public Map<String, Double> getAllViolationLevels(UUID playerUuid) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT check_id, vl FROM NeonAC_vl WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("check_id"), rs.getDouble("vl"));
                }
            }
        } catch (SQLException ignored) {
        }
        return result;
    }

    @Override
    public void setViolationLevel(UUID playerUuid, String checkId, double vl) {
        String sql = "INSERT INTO NeonAC_vl (uuid, check_id, vl) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE vl=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, checkId);
            ps.setDouble(3, vl);
            ps.setDouble(4, vl);
            ps.executeUpdate();
        } catch (SQLException ignored) {
            replaceVl(playerUuid, checkId, vl); // SQLite uses a different upsert; attempt REPLACE fallback.
        }
    }

    private void replaceVl(UUID uuid, String checkId, double vl) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO NeonAC_vl (uuid, check_id, vl) VALUES (?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, checkId);
            ps.setDouble(3, vl);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void resetViolationLevels(UUID playerUuid) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM NeonAC_vl WHERE uuid=?")) {
            ps.setString(1, playerUuid.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public List<Violation> getRecentViolations(UUID playerUuid, int limit) {
        return new ArrayList<>();
    }

    @Override
    public Object getRawHandle() {
        return connection;
    }
}
