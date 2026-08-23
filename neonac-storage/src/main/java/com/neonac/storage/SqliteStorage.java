package com.neonac.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.neonac.api.storage.StorageException;

public final class SqliteStorage extends JdbcStorage {

    private final File file;

    public SqliteStorage(File dataFolder) {
        this.file = new File(dataFolder, "neonac.db");
        configureRetries(2, 300);
    }

    @Override
    protected String buildUrl() {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }

    @Override
    public void init() throws StorageException {
        super.init();
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA busy_timeout=5000");
        } catch (SQLException e) {
            logger.warning("[NeonAC] Failed to set SQLite pragmas: " + e.getMessage());
        }
    }

    @Override
    protected String[] schemaStatements() {
        return new String[]{
                "CREATE TABLE IF NOT EXISTS neonac_vl (uuid TEXT, check_id TEXT, vl REAL, PRIMARY KEY (uuid, check_id))",
                "CREATE TABLE IF NOT EXISTS neonac_violations (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, check_id TEXT, vl REAL, confidence REAL, ts BIGINT)",
                "CREATE INDEX IF NOT EXISTS idx_violations_uuid ON neonac_violations(uuid)"
        };
    }

    @Override
    protected void upsertVl(Connection conn, UUID uuid, String checkId, double vl) throws SQLException {
        String sql = "INSERT OR REPLACE INTO neonac_vl (uuid, check_id, vl) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, checkId);
            ps.setDouble(3, vl);
            ps.executeUpdate();
        }
    }
}
