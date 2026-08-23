package com.neonac.storage;
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
                "CREATE TABLE IF NOT EXISTS NeonAC_vl (uuid VARCHAR(36), check_id VARCHAR(64), vl DOUBLE, PRIMARY KEY (uuid, check_id))",
                "CREATE TABLE IF NOT EXISTS NeonAC_violations (id BIGINT AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(36), check_id VARCHAR(64), vl DOUBLE, confidence DOUBLE, ts BIGINT)"
        };
    }
}
