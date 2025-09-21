package com.fuel50.devdb.service;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class DatabaseRestorer {
    private final Connection targetConn;

    public DatabaseRestorer(Connection targetConn) {
        this.targetConn = targetConn;
    }

    public void restore(String dumpFile) throws Exception {
        System.out.println("�� Reading dump file: " + dumpFile);

        try (FileInputStream fileStream = new FileInputStream(dumpFile);
                Scanner scanner = new Scanner(fileStream, "UTF-8")) {

            StringBuilder currentStatement = new StringBuilder();
            int statementCount = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                currentStatement.append(line).append(" ");

                // Execute statement when we hit a semicolon
                if (line.endsWith(";")) {
                    String sql = currentStatement.toString().trim();
                    if (!sql.isEmpty()) {
                        executeStatement(sql);
                        statementCount++;

                        if (statementCount % 100 == 0) {
                            System.out.println("   Processed " + statementCount + " statements...");
                        }
                    }
                    currentStatement.setLength(0); // Clear the buffer
                }
            }

            System.out.println("✅ Restored " + statementCount + " SQL statements");
        }
    }

    private void executeStatement(String sql) throws SQLException {
        try (Statement stmt = targetConn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Log the error but continue with other statements
            System.err.println("⚠️  Warning: Failed to execute statement: "
                    + sql.substring(0, Math.min(50, sql.length())) + "...");
            System.err.println("   Error: " + e.getMessage());
        }
    }
}