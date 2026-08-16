package com.earac.storage;

import java.io.File;

/**
 * SQLite-backed storage. Requires {@code org.xerial:sqlite-jdbc} on the classpath.
 */
public final class SqliteStorage extends JdbcStorage {

    private final File file;

    public SqliteStorage(File dataFolder) {
        this.file = new File(dataFolder, "earac.db");
    }

    @Override
    protected String buildUrl() {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }

    @Override
    protected String[] schemaStatements() {
        return new String[]{
                "CREATE TABLE IF NOT EXISTS earac_vl (uuid TEXT, check_id TEXT, vl REAL, PRIMARY KEY (uuid, check_id))",
                "CREATE TABLE IF NOT EXISTS earac_violations (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, check_id TEXT, vl REAL, confidence REAL, ts BIGINT)"
        };
    }
}
