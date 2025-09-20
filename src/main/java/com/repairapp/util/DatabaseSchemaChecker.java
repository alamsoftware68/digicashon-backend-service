package com.repairapp.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * Utility to check database schema on startup
 */
@Component
public class DatabaseSchemaChecker implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔍 CHECKING DATABASE SCHEMA FOR order_items TABLE...");
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            System.out.println("📊 Database: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            System.out.println("🔗 Connection URL: " + connection.getMetaData().getURL());
            
            // Check order_items table columns
            ResultSet columns = metaData.getColumns(null, null, "order_items", null);
            
            System.out.println("\n📋 COLUMNS IN order_items TABLE:");
            System.out.println("--------------------------------------");
            
            boolean hasServiceTitle = false;
            boolean hasServiceItemId = false;
            boolean hasServiceName = false;
            boolean hasServiceCategory = false;
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String nullable = columns.getString("IS_NULLABLE");
                
                System.out.printf("%-20s %-15s (%d) %s%n", 
                    columnName, columnType, columnSize, nullable.equals("YES") ? "NULL" : "NOT NULL");
                
                if ("service_title".equals(columnName)) hasServiceTitle = true;
                if ("service_item_id".equals(columnName)) hasServiceItemId = true;
                if ("service_name".equals(columnName)) hasServiceName = true;
                if ("service_category".equals(columnName)) hasServiceCategory = true;
            }
            
            System.out.println("--------------------------------------");
            System.out.println("✅ COLUMN CHECK RESULTS:");
            System.out.println("service_name: " + (hasServiceName ? "✅ EXISTS" : "❌ MISSING"));
            System.out.println("service_category: " + (hasServiceCategory ? "✅ EXISTS" : "❌ MISSING"));
            System.out.println("service_title: " + (hasServiceTitle ? "✅ EXISTS" : "❌ MISSING - NEW COLUMN"));
            System.out.println("service_item_id: " + (hasServiceItemId ? "✅ EXISTS" : "❌ MISSING - NEW COLUMN"));
            
            if (!hasServiceTitle || !hasServiceItemId) {
                System.out.println("\n⚠️  NEW COLUMNS MISSING! Hibernate ddl-auto=update should have added them.");
                System.out.println("💡 You may need to:");
                System.out.println("   1. Restart the application to trigger schema update");
                System.out.println("   2. Or run the manual SQL script: add_order_items_columns.sql");
                System.out.println("   3. Or check Hibernate logs for schema update errors");
            } else {
                System.out.println("\n🎉 ALL COLUMNS EXIST! Schema is up to date.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR checking database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
