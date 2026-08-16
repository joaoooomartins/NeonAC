package com.earac.storage;

/**
 * MySQL/MariaDB-backed storage. Requires {@code com.mysql:mysql-connector-j} on the
 * classpath. Connection details are supplied by the caller (no hardcoded URLs), keeping
 * this module free of a dependency on the core configuration layer.
 */
public final class MysqlStorage extends JdbcStorage {

    private final String url;
    private final String user;
    private final String password;

    public MysqlStorage(String host, int port, String database, String user,
                        String password, String params) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + params;
        this.user = user;
        this.password = password;
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
                "CREATE TABLE IF NOT EXISTS earac_vl (uuid VARCHAR(36), check_id VARCHAR(64), vl DOUBLE, PRIMARY KEY (uuid, check_id))",
                "CREATE TABLE IF NOT EXISTS earac_violations (id BIGINT AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(36), check_id VARCHAR(64), vl DOUBLE, confidence DOUBLE, ts BIGINT)"
        };
    }
}
