package com.neonac.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public final class MysqlStorage extends JdbcStorage {

    private final String url;
    private final String user;
    private final String password;

    public MysqlStorage(String host, int port, String database, String user,
                        String password, String params) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + params;
        this.user = user;
        this.password = password;
        configureRetries(3, 1000);
    }

    @Override
    protected String buildUrl() {
        return url;
    }

    @Override
    protected String[] getCredentials() {
        return new String[]{user, password};
    }

    @Override
    protected String[] schemaStatements() {
        return new String[]{
                "CREATE TABLE IF NOT EXISTS neonac_vl (uuid VARCHAR(36), check_id VARCHAR(64), vl DOUBLE, PRIMARY KEY (uuid, check_id))",
                "CREATE TABLE IF NOT EXISTS neonac_violations (id BIGINT AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(36), check_id VARCHAR(64), vl DOUBLE, confidence DOUBLE, ts BIGINT)",
                "CREATE INDEX IF NOT EXISTS idx_violations_uuid ON neonac_violations(uuid)"
        };
    }

    @Override
    protected void upsertVl(Connection conn, UUID uuid, String checkId, double vl) throws SQLException {
        String sql = "INSERT INTO neonac_vl (uuid, check_id, vl) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE vl=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, checkId);
            ps.setDouble(3, vl);
            ps.setDouble(4, vl);
            ps.executeUpdate();
        }
    }
}
