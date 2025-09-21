package com.fuel50.devdb.cli;

import com.fuel50.devdb.service.DatabaseRestorer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Command(name = "restore", description = "Restore lightweight database to local MySQL instance")
public class RestoreCommand implements Runnable {

    @Option(names = { "-t",
            "--target" }, description = "Target database connection string", defaultValue = "jdbc:mysql://local-mysql:3306/fuel50_db")
    private String targetDb;

    @Option(names = { "-u", "--username" }, description = "Database username", defaultValue = "root")
    private String username;

    @Option(names = { "-p", "--password" }, description = "Database password", defaultValue = "root")
    private String password;

    @Option(names = { "-d",
            "--dump" }, description = "Path to lightweight dump file", defaultValue = "/app/output/lightweight-dump.sql")
    private String dumpFile;

    @Override
    public void run() {
        try {
            System.out.println("🔄 DevDB Restore - Process Started");
            System.out.println("================================================");

            File dump = new File(dumpFile);
            if (!dump.exists()) {
                System.err.println("❌ Dump file not found: " + dumpFile);
                System.err.println("💡 Tip: Run 'generate' first to create a lightweight database");
                System.exit(1);
            }

            System.out.println("📊 Dump file size: " + formatFileSize(dump.length()));

            // Connect to target database
            Connection targetConn = DriverManager.getConnection(targetDb, username, password);
            System.out.println("✅ Connected to target database");

            // Restore database
            DatabaseRestorer restorer = new DatabaseRestorer(targetConn);
            restorer.restore(dumpFile);

            // Close connection
            targetConn.close();

            System.out.println("🎉 Database restored successfully. You can now connect to: " + targetDb);

            System.out.println("================================================");
            System.out.println("✅ DevDB Restore - Process Completed");
        } catch (SQLException e) {
            System.err.println("❌ Database Error: " + e.getMessage());
            System.err.println("💡 Check your database connection settings");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}