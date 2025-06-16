package mutants;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;


public class mutant3 {





    private static final String DB_URL = "jdbc:sqlite:inventory.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                createTables(conn);
                menu(conn);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    public static void createTables(Connection conn) {
        String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "price REAL NOT NULL);";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createProductsTable);
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public static void menu(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nInventory Management System");
            System.out.println("1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Update Product");
            System.out.println("4. Delete Product");
            System.out.println("5. Search Product by Name");
            System.out.println("6. View Low Stock Products");
            System.out.println("7. Calculate Total Inventory Value");
            System.out.println("8. Sort Products by Price");
            System.out.println("9. Export Products to CSV");
            System.out.println("10. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addProduct(conn, scanner);
                case 2 -> viewProducts(conn);
                case 3 -> updateProduct(conn, scanner);
                case 4 -> deleteProduct(conn, scanner);
                case 5 -> searchProductByName(conn, scanner);
                case 6 -> viewLowStockProducts(conn, scanner);
                case 7 -> calculateTotalInventoryValue(conn);
                case 8 -> sortProductsByPrice(conn, scanner);
                case 9 -> exportProductsToCSV(conn);
                case 10 -> {
                    System.out.println("Exiting the system.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void addProduct(Connection conn, Scanner scanner) {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter price: ");
        double price = scanner.nextDouble();

        String insertProduct = "INSERT INTO products (name, quantity, price) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertProduct)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
            System.out.println("Product added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    public static void viewProducts(Connection conn) {
        String selectProducts = "SELECT name FROM products;";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectProducts)) {
            System.out.printf("%-5s %-20s %-10s %-10s\n", "ID", "Name", "Quantity", "Price");
            System.out.println("-------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d %-20s %-10d %-10.2f\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving products: " + e.getMessage());
        }
    }
    public static void viewProductsByPriceRange(Connection conn, Scanner scanner) {
        System.out.print("Enter minimum price: ");
        double minPrice = scanner.nextDouble();
        System.out.print("Enter maximum price: ");
        double maxPrice = scanner.nextDouble();

        String query = "SELECT * FROM products WHERE price >= ? AND price <= ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, minPrice);
            pstmt.setDouble(2, maxPrice);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-5s %-20s %-10s %-10s\n", "ID", "Name", "Quantity", "Price");
                System.out.println("-------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-5d %-20s %-10d %-10.2f\n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving products by price range: " + e.getMessage());
        }
    }

    public static void updateProduct(Connection conn, Scanner scanner) {
        System.out.print("Enter product ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter new name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter new price: ");
        double price = scanner.nextDouble();

        String updateProduct = "UPDATE products SET name = ?, quantity = ?, price = ? WHERE id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(updateProduct)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            System.out.println("Product updated successfully.");
        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
        }
    }

    public static void deleteProduct(Connection conn, Scanner scanner) {
        System.out.print("Enter product ID to delete: ");
        int id = scanner.nextInt();

        String deleteProduct = "DELETE FROM products WHERE id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteProduct)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Product deleted successfully.");
        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }

    public static void searchProductByName(Connection conn, Scanner scanner) {
        System.out.print("Enter product name to search: ");
        String name = scanner.nextLine();

        String query = "SELECT * FROM products WHERE name LIKE ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-5s %-20s %-10s %-10s\n", "ID", "Name", "Quantity", "Price");
                System.out.println("-------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-5d %-20s %-10d %-10.2f\n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching products: " + e.getMessage());
        }
    }

    public static void viewLowStockProducts(Connection conn, Scanner scanner) {
        System.out.print("Enter low stock threshold: ");
        int threshold = scanner.nextInt();

        String query = "SELECT * FROM products WHERE quantity < ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, threshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-5s %-20s %-10s %-10s\n", "ID", "Name", "Quantity", "Price");
                System.out.println("-------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-5d %-20s %-10d %-10.2f\n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving low stock products: " + e.getMessage());
        }
    }

    public static void calculateTotalInventoryValue(Connection conn) {
        String query = "SELECT SUM(quantity * price) AS total_value FROM products;";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                double totalValue = rs.getDouble("total_value");
                System.out.printf("Total inventory value: %.2f\n", totalValue);
            }
        } catch (SQLException e) {
            System.out.println("Error calculating total inventory value: " + e.getMessage());
        }
    }

    public static void sortProductsByPrice(Connection conn, Scanner scanner) {
        System.out.print("Sort by (1) Ascending or (2) Descending price? ");
        int choice = scanner.nextInt();

        String query = (choice == 1) ?
                "SELECT * FROM products ORDER BY price ASC;" :
                "SELECT * FROM products ORDER BY price DESC;";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.printf("%-5s %-20s %-10s %-10s\n", "ID", "Name", "Quantity", "Price");
            System.out.println("-------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d %-20s %-10d %-10.2f\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"));
            }
        } catch (SQLException e) {
            System.out.println("Error sorting products: " + e.getMessage());
        }
    }

    public static void exportProductsToCSV(Connection conn) {
        String query = "SELECT * FROM products;";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            try (FileWriter writer = new FileWriter("products.csv")) {
                writer.write("ID,Name,Quantity,Price\n");
                while (rs.next()) {
                    writer.write(String.format("%d,%s,%d,%.2f\n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")));
                }
                System.out.println("Products exported to products.csv successfully.");
            } catch (IOException e) {
                System.out.println("Error writing to CSV file: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error exporting products: " + e.getMessage());
        }
    }


}
