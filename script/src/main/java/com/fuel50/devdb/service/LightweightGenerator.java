package com.fuel50.devdb.service;

import com.fuel50.devdb.model.ColumnSpec;
import com.fuel50.devdb.model.DatabaseSpec;
import com.fuel50.devdb.model.TableSpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.stream.Collectors;

public class LightweightGenerator {
    private final DatabaseSpec spec;
    private final Connection sourceConn;
    private final Map<String, Set<Long>> selectedIds = new HashMap<>();

    public LightweightGenerator(DatabaseSpec spec, Connection sourceConn) {
        this.spec = spec;
        this.sourceConn = sourceConn;
    }

    public void generate(String outputDir) throws Exception {
        System.out.println("🔄 Starting lightweight database generation...");

        // Create output directory
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // Step 1: Analyze and select data
        System.out.println("📊 Step 1: Analyzing and selecting data...");
        selectData();

        // Step 2: Generate SQL dump
        System.out.println("📝 Step 2: Generating SQL dump...");
        generateSqlDump(outputPath);

        // Step 3: Generate manifest
        System.out.println("📋 Step 3: Generating manifest...");
        generateManifest(outputPath);

        System.out.println("✅ Generation complete!");
    }

    private void selectData() throws SQLException {
        List<Map.Entry<String, TableSpec>> sortedTables = spec.getTables().entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    Integer order1 = e1.getValue().getProcessingOrder();
                    Integer order2 = e2.getValue().getProcessingOrder();
                    if (order1 == null)
                        order1 = 999;
                    if (order2 == null)
                        order2 = 999;
                    return order1.compareTo(order2);
                })
                .collect(Collectors.toList());

        // First, identify root tables
        List<String> rootTables = new ArrayList<>();
        for (Map.Entry<String, TableSpec> entry : sortedTables) {
            TableSpec table = entry.getValue();
            if (table.getSubset() != null && table.getSubset().isRoot()) {
                rootTables.add(entry.getKey());
            }
        }

        System.out.println("   Root tables: " + rootTables);

        // Process root tables first
        for (String tableName : rootTables) {
            selectTableData(tableName);
        }

        // Process dependent tables
        for (Map.Entry<String, TableSpec> entry : sortedTables) {
            String tableName = entry.getKey();

            if (!rootTables.contains(tableName)) {
                selectTableData(tableName);
            }
        }
    }

    private void selectTableData(String tableName) throws SQLException {
        TableSpec table = spec.getTables().get(tableName);
        if (table == null || table.getSubset() == null) {
            return;
        }

        System.out.println("   Processing table: " + tableName);

        String strategy = table.getSubset().getStrategy();
        Set<Long> ids = new HashSet<>();

        if ("sample".equals(strategy)) {
            ids = sampleTable(tableName, table);
        } else if ("fk_closure".equals(strategy)) {
            ids = selectByForeignKeyClosure(tableName, table);
        } else if ("all".equals(strategy)) {
            ids = selectAll(tableName);
        }

        selectedIds.put(tableName, ids);
        System.out.println("     Selected " + ids.size() + " rows");
    }

    private Set<Long> sampleTable(String tableName, TableSpec table) throws SQLException {
        Set<Long> ids = new HashSet<>();

        StringBuilder query = new StringBuilder("SELECT id FROM " + tableName);

        // Add time window filter if specified
        if (table.getSubset().getTimeWindowDays() != null) {
            query.append(" WHERE created_at >= DATE_SUB(NOW(), INTERVAL ")
                    .append(table.getSubset().getTimeWindowDays())
                    .append(" DAY)");
        }

        // Add ordering
        if (table.getSubset().getOrderBy() != null) {
            query.append(" ORDER BY ").append(table.getSubset().getOrderBy());
        }

        // Add limit
        Integer maxRows = table.getSubset().getMaxRows();
        if (maxRows == null && spec.getDefaults() != null && spec.getDefaults().getSubset() != null) {
            maxRows = spec.getDefaults().getSubset().getMaxRows();
        }

        if (maxRows != null) {
            query.append(" LIMIT ").append(maxRows);
        }

        try (PreparedStatement stmt = sourceConn.prepareStatement(query.toString());
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getLong("id"));
            }
        }

        return ids;
    }

    private Set<Long> selectByForeignKeyClosure(String tableName, TableSpec table) throws SQLException {
        Set<Long> ids = new HashSet<>();

        if (table.getFk() == null || table.getFk().getReferences() == null) {
            return ids;
        }

        for (TableSpec.ForeignKeyReference fkRef : table.getFk().getReferences()) {
            String referencedTable = fkRef.getTable();
            Set<Long> referencedIds = selectedIds.get(referencedTable);

            if (referencedIds != null && !referencedIds.isEmpty()) {
                String idList = referencedIds.stream()
                        .map(String::valueOf)
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");

                StringBuilder query = new StringBuilder("SELECT id FROM " + tableName +
                        " WHERE " + fkRef.getColumn() + " IN (" + idList + ")");

                // Add limit
                Integer maxRows = table.getSubset().getMaxRows();
                if (maxRows == null && spec.getDefaults() != null && spec.getDefaults().getSubset() != null) {
                    maxRows = spec.getDefaults().getSubset().getMaxRows();
                }

                if (maxRows != null) {
                    query.append(" LIMIT ").append(maxRows);
                }

                try (PreparedStatement stmt = sourceConn.prepareStatement(query.toString());
                        ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        ids.add(rs.getLong("id"));
                    }
                }
            }
        }

        return ids;
    }

    private Set<Long> selectAll(String tableName) throws SQLException {
        Set<Long> ids = new HashSet<>();

        String query = "SELECT id FROM " + tableName;
        try (PreparedStatement stmt = sourceConn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getLong("id"));
            }
        }

        return ids;
    }

    private void generateSqlDump(Path outputPath) throws Exception {
        String dumpFile = outputPath.resolve("lightweight-dump.sql").toString();

        try (PrintWriter writer = new PrintWriter(new FileWriter(dumpFile))) {
            writer.println("-- Lightweight Database Dump");
            writer.println("-- Generated by DevDB");
            writer.println("-- " + new Date());
            writer.println();

            // Generate data inserts
            generateDataInserts(writer);

            // Generate post-load sequences
            generatePostLoadSequences(writer);
        }

        System.out.println("   Generated SQL dump: " + dumpFile);
    }

    private void generateDataInserts(PrintWriter writer) throws SQLException {
        List<Map.Entry<String, TableSpec>> sortedTables = spec.getTables().entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    Integer order1 = e1.getValue().getProcessingOrder();
                    Integer order2 = e2.getValue().getProcessingOrder();
                    if (order1 == null)
                        order1 = 999;
                    if (order2 == null)
                        order2 = 999;
                    return order1.compareTo(order2);
                })
                .collect(Collectors.toList());

        for (Map.Entry<String, TableSpec> entry : sortedTables) {
            String tableName = entry.getKey();
            TableSpec table = entry.getValue();
            Set<Long> ids = selectedIds.get(tableName);

            if (ids == null || ids.isEmpty()) {
                continue;
            }

            writer.println("-- Data for table: " + tableName);

            // Get column information
            DatabaseMetaData metaData = sourceConn.getMetaData();
            List<String> columnNames = new ArrayList<>();

            try (ResultSet columns = metaData.getColumns(null, "fuel50_db", tableName, null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");

                    // Filter out system columns
                    if (!isSystemColumn(columnName)) {
                        columnNames.add(columnName);
                    }
                }
            }

            // Generate single INSERT statement with multiple values
            String idList = ids.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            String query = "SELECT * FROM " + tableName + " WHERE id IN (" + idList + ")";

            try (PreparedStatement stmt = sourceConn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery()) {

                List<String> valueRows = new ArrayList<>();

                while (rs.next()) {
                    List<String> values = new ArrayList<>();
                    for (String columnName : columnNames) {
                        Object value = rs.getObject(columnName);
                        String processedValue = processColumnValue(tableName, columnName, value, table);
                        values.add(processedValue);
                    }
                    valueRows.add("(" + String.join(", ", values) + ")");
                }

                if (!valueRows.isEmpty()) {
                    StringBuilder insert = new StringBuilder("INSERT INTO " + tableName + " (");
                    insert.append(String.join(", ", columnNames));
                    insert.append(") VALUES ");
                    insert.append(String.join(",\n", valueRows));
                    insert.append(";");

                    writer.println(insert.toString());
                }
            }

            writer.println();
        }
    }

    private boolean isSystemColumn(String columnName) {
        // List of system columns to exclude
        Set<String> systemColumns = Set.of(
                "USER", "CURRENT_CONNECTIONS", "TOTAL_CONNECTIONS",
                "MAX_SESSION_CONTROLLED_MEMORY", "MAX_SESSION_TOTAL_MEMORY");

        return systemColumns.contains(columnName.toUpperCase());
    }

    private String processColumnValue(String tableName, String columnName, Object value, TableSpec table) {
        if (value == null) {
            return "NULL";
        }

        // Get column 'strategy'
        ColumnSpec column = table.getColumns().get(columnName);
        if (column == null) {
            return formatValue(value);
        }

        String strategy = column.getStrategy();

        switch (strategy) {
            case "keep":
                return formatValue(value);
            case "mask":
                return formatValue(maskValue(value, column));
            case "synthesize":
                return formatValue(synthesizeValue(value, column));
            default:
                return formatValue(value);
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        }

        if (value instanceof java.sql.Timestamp) {
            return "'" + value.toString() + "'";
        }

        if (value instanceof java.sql.Date) {
            return "'" + value.toString() + "'";
        }

        if (value instanceof java.sql.Time) {
            return "'" + value.toString() + "'";
        }

        if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        }

        return value.toString();
    }

    private Object maskValue(Object value, ColumnSpec column) {
        if (column.getMask() == null) {
            return value;
        }

        String type = column.getMask().getType();
        String deterministicKey = column.getMask().getDeterministicKey();
        if (deterministicKey == null || deterministicKey.isEmpty()) {
            deterministicKey = spec.getDefaults().getMasking().getDeterministicSalt();
        }
        // Generate deterministic masked value
        String key = deterministicKey + "_" + value.toString();
        String hash = generateHash(key);

        switch (type) {
            case "username":
                return "user_" + hash.substring(0, 8);
            case "numeric_noise":
                if (value instanceof Number) {
                    double original = ((Number) value).doubleValue();
                    double noise = (hash.hashCode() % 100) / 100.0 * (column.getMask().getPercent() / 100.0) * original;
                    return original + noise;
                }
                return value;
            case "category_map":
                return "masked_" + hash.substring(0, 6);
            default:
                return value;
        }
    }

    private Object synthesizeValue(Object value, ColumnSpec column) {
        if (column.getSynth() == null) {
            return value;
        }

        String type = column.getSynth().getType();
        String deterministicKey = column.getSynth().getDeterministicKey();
        if (deterministicKey == null || deterministicKey.isEmpty()) {
            deterministicKey = spec.getDefaults().getMasking().getDeterministicSalt();
        }
        // Generate deterministic synthetic value
        String key = deterministicKey + "_" + value.toString();
        String hash = generateHash(key);

        switch (type) {
            case "email":
                String domain = column.getSynth().getDomain();
                if (domain == null && spec.getDefaults() != null && spec.getDefaults().getMasking() != null) {
                    domain = spec.getDefaults().getMasking().getEmailDomain();
                }
                return "user" + hash.substring(0, 6) + "@" + (domain != null ? domain : "dev.local");

            case "password_hash":
                return "$2b$10$" + hash.substring(0, 22) + "..." + hash.substring(22, 31);

            case "address":
                return hash.substring(0, 8) + " Fake St, Test City, TC 12345";

            case "credit_card":
                if ("last4-only".equals(column.getSynth().getFormat())) {
                    return "XXXX-XXXX-XXXX-" + hash.substring(0, 4);
                }
                return "XXXX-XXXX-XXXX-" + hash.substring(0, 4);

            default:
                return value;
        }
    }

    private String generateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    private void generatePostLoadSequences(PrintWriter writer) {
        writer.println("-- Post-load sequence adjustments");

        List<Map.Entry<String, TableSpec>> sortedTables = spec.getTables().entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    Integer order1 = e1.getValue().getProcessingOrder();
                    Integer order2 = e2.getValue().getProcessingOrder();
                    if (order1 == null)
                        order1 = 999;
                    if (order2 == null)
                        order2 = 999;
                    return order1.compareTo(order2);
                })
                .collect(Collectors.toList());

        for (Map.Entry<String, TableSpec> entry : sortedTables) {
            TableSpec table = entry.getValue();
            if (table.getPostLoad() != null && table.getPostLoad().getSequenceOffset() != null) {
                writer.println("ALTER TABLE " + table.getName() +
                        " AUTO_INCREMENT = " + table.getPostLoad().getSequenceOffset() + ";");
            }
        }
    }

    private void generateManifest(Path outputPath) throws IOException {
        String manifestFile = outputPath.resolve("manifest.json").toString();

        try (PrintWriter writer = new PrintWriter(new FileWriter(manifestFile))) {
            writer.println("{");
            writer.println("  \"generated_at\": \"" + new Date().toString() + "\",");
            writer.println("  \"source_database\": \"fuel50_db\",");
            writer.println("  \"destination_database\": \"fuel50_db\",");
            writer.println("  \"tables\": {");

            boolean first = true;

            List<Map.Entry<String, TableSpec>> sortedTables = spec.getTables().entrySet()
                    .stream()
                    .sorted((e1, e2) -> {
                        Integer order1 = e1.getValue().getProcessingOrder();
                        Integer order2 = e2.getValue().getProcessingOrder();
                        if (order1 == null)
                            order1 = 999;
                        if (order2 == null)
                            order2 = 999;
                        return order1.compareTo(order2);
                    })
                    .collect(Collectors.toList());

            for (Map.Entry<String, TableSpec> entry : sortedTables) {
                String tableName = entry.getKey();
                TableSpec table = entry.getValue();
                Set<Long> ids = selectedIds.get(tableName);

                if (!first) {
                    writer.println(",");
                }
                first = false;

                writer.println("    \"" + tableName + "\": {");
                writer.println("      \"strategy\": \"" + table.getSubset().getStrategy() + "\",");
                writer.println("      \"selected_rows\": " + (ids != null ? ids.size() : 0) + ",");
                writer.println("      \"max_rows\": "
                        + (table.getSubset().getMaxRows() != null ? table.getSubset().getMaxRows() : "null"));
                writer.print("    }");
            }

            writer.println();
            writer.println("  }");
            writer.println("}");
        }

        System.out.println("   Generated manifest: " + manifestFile);
    }
}