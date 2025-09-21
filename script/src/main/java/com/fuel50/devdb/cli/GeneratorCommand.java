package com.fuel50.devdb.cli;

import com.fuel50.devdb.config.DatabaseSpecLoader;
import com.fuel50.devdb.model.DatabaseSpec;
import com.fuel50.devdb.model.TableSpec;
import com.fuel50.devdb.model.ColumnSpec;
import com.fuel50.devdb.service.LightweightGenerator;
import picocli.CommandLine.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Command(name = "generate", description = "Generate lightweight database from production data")
public class GeneratorCommand implements Runnable {

    @Option(names = { "-s",
            "--source" }, description = "Source database connection string", defaultValue = "jdbc:mysql://prod-mysql:3306/fuel50_db")
    private String sourceDb;

    @Option(names = { "-u", "--username" }, description = "Source database username", defaultValue = "root")
    private String username;

    @Option(names = { "-p", "--password" }, description = "Source database password", defaultValue = "root")
    private String password;

    @Option(names = { "-c", "--config" }, description = "Path to spec.yml file", defaultValue = "spec.yml")
    private String configFile;

    @Option(names = { "-o",
            "--output" }, description = "Output directory for lightweight database", defaultValue = "/app/output")
    private String outputDir;

    @Option(names = { "-d",
            "--dry-run" }, description = "Show what would be generated without actually generating it", defaultValue = "false")
    private boolean dryRun;

    @Override
    public void run() {
        try {
            // Load configuration
            DatabaseSpec spec = DatabaseSpecLoader.load(new File(configFile));
            System.out.println("✅ Loaded configuration from: " + configFile);

            if (dryRun) {
                analyzeDatabase(spec);
                return;
            }

            System.out.println("Lightweight Database Generator - Process Started");
            System.out.println("================================================");

            // Connect to source database
            Connection sourceConn = DriverManager.getConnection(sourceDb, username, password);
            System.out.println("✅ Connected to source database");

            // Generate lightweight database
            LightweightGenerator generator = new LightweightGenerator(spec, sourceConn);
            generator.generate(outputDir);

            // Close connection
            sourceConn.close();

            System.out.println("================================================");
            System.out.println("✅ Lightweight Database Generator - Process Completed");
            System.out.println("📁 Output directory: " + outputDir);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void analyzeDatabase(DatabaseSpec spec) {
        System.out.println("🔍 DRY RUN - Process Started");
        System.out.println("================================================");

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

        for (Map.Entry<String, TableSpec> table : sortedTables) {
            System.out.println("📋 Table: " + table.getKey());

            System.out.println("📋 Processing Order: " + table.getValue().getProcessingOrder());

            System.out.println("   Strategy: " + table.getValue().getSubset().getStrategy());

            if (table.getValue().getSubset().getMaxRows() != null) {
                System.out.println("   Max Rows: " + table.getValue().getSubset().getMaxRows());
            } else {
                System.out.println("   Max Rows: " + spec.getDefaults().getSubset().getMaxRows());
            }

            System.out.println("   Columns:");
            for (ColumnSpec column : table.getValue().getColumns().values()) {
                System.out.println("     - " + column.getName() +
                        " (" + column.getSensitivity() + ") -> " + column.getStrategy());
            }
        }

        System.out.println("================================================");
        System.out.println("✅ DRY RUN - Process Completed");
    }
}