import mutants.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Scanner;
import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.Statement;
import static org.mockito.Mockito.*;
import java.sql.ResultSet;
import java.sql.Connection;
public class inventorymanagementsystemTest {

    private static final String DB_URL = "jdbc:sqlite::memory:";
    private static Connection conn;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
        inventorymanagementsystem.createTables(conn);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM products");
        }
    }

    @AfterEach
    void tearDownDatabase() throws SQLException {
        // Drop the table after the test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE products");
        }
        conn.close();
    }


    private void mockInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    @Test
    public void testAddProduct() throws SQLException {
        mockInput("Test Product\n10\n19.99\n");
        Scanner scanner = new Scanner(System.in);

        inventorymanagementsystem.addProduct(conn, scanner);


        String query = "SELECT * FROM products WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "Test Product");
            ResultSet rs = pstmt.executeQuery();

            assertTrue(rs.next(), "Product should exist in the database.");
            assertEquals("Test Product", rs.getString("name"), "Product name mismatch.");
            assertEquals(10, rs.getInt("quantity"), "Product quantity mismatch.");
            assertEquals(19.99, rs.getDouble("price"), 0.01, "Product price mismatch.");
        }
    }



    @Test
    public void testViewProducts() throws SQLException {
        // Redirect System.out temporarily
        System.setOut(new PrintStream(outContent));

        try {
            // Add test data
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (1, 'Product A', 5, 10.50)");
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (2, 'Product B', 3, 20.00)");
            }

            // Act: Call the method to test
            inventorymanagementsystem.viewProducts(conn);

            // Assert: Verify the output
            String output = outContent.toString();
            assertTrue(output.contains("Product A"), "Output should contain Product A.");
            assertTrue(output.contains("Product B"), "Output should contain Product B.");
            assertTrue(output.contains("5"), "Output should contain quantity of Product A.");
            assertTrue(output.contains("3"), "Output should contain quantity of Product B.");
            assertTrue(output.contains("10.50"), "Output should contain price of Product A.");
            assertTrue(output.contains("20.00"), "Output should contain price of Product B.");
        } finally {
            // Restore System.out
            System.setOut(originalOut);
        }
    }




    @Test
    public void testViewProductsByPriceRange() throws Exception {
        // Redirect System.out temporarily
        System.setOut(new PrintStream(outContent));
        // Add test data
    try{
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (1, 'Product A', 2, 7.50)");
            stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (2, 'Product B', 3, 10.00)");
            stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (3, 'Product C', 7, 15.00)");
            stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (4, 'Product D', 6, 20.00)");
            stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (5, 'Product E', 10, 25.00)");
        }

        // Mock user input
        mockInput("7.50\n25.0\n");
        Scanner scanner = new Scanner(System.in);

        // Call the method
        inventorymanagementsystem.viewProductsByPriceRange(conn, scanner);

        // Capture the output
        String output = outContent.toString();


        assertTrue(output.contains("Product B"), "Output should contain Product B.");
        assertTrue(output.contains("3"), "Output should contain quantity of Product B.");
        assertTrue(output.contains("10.00"), "Output should contain price of Product B.");

        // Assertions for Product C
        assertTrue(output.contains("Product C"), "Output should contain Product C.");
        assertTrue(output.contains("7"), "Output should contain quantity of Product C.");
        assertTrue(output.contains("15.00"), "Output should contain price of Product C.");

        // Assertions for Product D
        assertTrue(output.contains("Product D"), "Output should contain Product D.");
        assertTrue(output.contains("6"), "Output should contain quantity of Product D.");
        assertTrue(output.contains("20.00"), "Output should contain price of Product D.");

        // Ensure excluded products are not present

    } finally {
        System.setOut(originalOut);
        }
    }


    @Test
    public void testUpdateProduct() throws SQLException {
        // Step 1: Insert a test product and get its ID
        int productId;
        String insertProductQuery = "INSERT INTO products (name, quantity, price) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertProductQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "Original Product");
            pstmt.setInt(2, 10);
            pstmt.setDouble(3, 15.0);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            assertTrue(rs.next(), "Generated key should be available.");
            productId = rs.getInt(1);
        }

        // Step 2: Prepare mock input for updateProduct()
        // Simulate user input: productId \n newName \n newQuantity \n newPrice
        mockInput(productId + "\nUpdated Product\n20\n25.0\n");
        Scanner scanner = new Scanner(System.in);

        // Step 3: Call the method under test
        inventorymanagementsystem.updateProduct(conn, scanner);

        // Step 4: Verify the database was updated
        String query = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement selectPstmt = conn.prepareStatement(query)) {
            selectPstmt.setInt(1, productId);
            ResultSet rs = selectPstmt.executeQuery();

            assertTrue(rs.next(), "Product should exist in the database.");
            assertEquals("Updated Product", rs.getString("name"), "Product name mismatch.");
            assertEquals(20, rs.getInt("quantity"), "Product quantity mismatch.");
            assertEquals(25.0, rs.getDouble("price"), 0.01, "Product price mismatch.");
        }
    }




    @Test
    public void testDeleteProduct() throws SQLException {
        // Simulate user input for deleteProduct
        String userInput = "1\n";
        Scanner scanner = new Scanner(userInput);

        // Call the method to test
        inventorymanagementsystem.deleteProduct(conn, scanner);

        // Verify the product was deleted
        String query = "SELECT * FROM products WHERE id = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            assertFalse(rs.next(), "Product should be deleted.");
        }
    }


    @Test
    public void testSearchProductByName() throws SQLException {
        // Step 1: Insert a product
        mockInput("Searched Product\n5\n15.0\n");
        Scanner scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        // Step 2: Redirect System.out to capture printed output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Step 3: Perform the search
            mockInput("Searched\n");
            scanner = new Scanner(System.in); // Reset scanner with new mock input
            inventorymanagementsystem.searchProductByName(conn, scanner);

            // Step 4: Assert the output contains expected values
            String output = outContent.toString();
            assertTrue(output.contains("Searched Product"), "Output should contain the product name.");
            assertTrue(output.contains("5"), "Output should contain the product quantity.");
            assertTrue(output.contains("15.0") || output.contains("15.00"), "Output should contain the product price.");
        } finally {
            // Step 5: Restore System.out
            System.setOut(originalOut);
        }
    }


    @Test
    public void testViewLowStockProducts() throws SQLException {
        // Step 1: Prepare test data (mock input for two products)

        mockInput("Low Stock Product\n2\n5.0\n");
        Scanner scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        mockInput("Normal Stock Product\n10\n20.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);


        // Step 2: Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Step 3: Mock input for the low stock threshold and run the method
            mockInput("5\n");  // Threshold set to 5
            scanner = new Scanner(System.in);  // Reset scanner with new input
            inventorymanagementsystem.viewLowStockProducts(conn, scanner);

            // Step 4: Assert output
            String output = outContent.toString();
            assertTrue(output.contains("Low Stock Product"), "Output should contain low stock product.");
            assertFalse(output.contains("Normal Stock Product"), "Output should NOT contain normal stock product.");
            assertTrue(output.contains("2"), "Output should contain quantity of Low Stock Product.");
        } finally {
            // Step 5: Restore System.out
            System.setOut(originalOut);
        }
    }


    @Test
    public void testCalculateTotalInventoryValue() throws SQLException {
        Scanner scanner = new Scanner(System.in);

        // First product input
        mockInput("Product A\n5\n10.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        // Second product input
        mockInput("Product B\n3\n20.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        // Now calculate the total value
        inventorymanagementsystem.calculateTotalInventoryValue(conn);

        // Output verification would require capturing System.out
    }




    @Test
    public void testSortProductsByPrice() throws SQLException {
        Scanner scanner = new Scanner(System.in);

        // First product input
        mockInput("Cheap Product\n10\n5.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        // Second product input
        mockInput("Expensive Product\n3\n50.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        // Input for sort option
        mockInput("1\n"); // 1 = ascending
        scanner = new Scanner(System.in);
        inventorymanagementsystem.sortProductsByPrice(conn, scanner);

        // Output verification would require capturing System.out
    }


    @Test
    public void testExportProductsToCSV() throws SQLException {
        Scanner scanner = new Scanner(System.in);

        // First product input
        mockInput("Product A\n10\n15.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        // Second product input
        mockInput("Product B\n5\n25.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        // Export to CSV
        inventorymanagementsystem.exportProductsToCSV(conn);

        // You could now check that products.csv exists and has expected content
    }

    @Test
    public void testAddProductmutant1() throws SQLException {//this mutant1 didnt pass
        mockInput("Test Product\n10\n19.99\n");
        Scanner scanner = new Scanner(System.in);

        mutant1.addProduct(conn, scanner);


        String query = "SELECT * FROM products WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "Test Product");
            ResultSet rs = pstmt.executeQuery();

            assertTrue(rs.next(), "Product should exist in the database.");
            assertEquals("Test Product", rs.getString("name"), "Product name mismatch.");
            assertEquals(10, rs.getInt("quantity"), "Product quantity mismatch.");
            assertEquals(19.99, rs.getDouble("price"), 0.01, "Product price mismatch.");
        }
    }


    @Test
    public void testViewProductsmutant2() throws SQLException {//this mutant2 didnt pass
        // Redirect System.out temporarily
        System.setOut(new PrintStream(outContent));

        try {
            // Add test data
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (1, 'Product A', 5, 10.50)");
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (2, 'Product B', 3, 20.00)");
            }

            // Act: Call the method to test
            mutant2.viewProducts(conn);

            // Assert: Verify the output
            String output = outContent.toString();
            assertTrue(output.contains("Product A"), "Output should contain Product A.");
            assertTrue(output.contains("Product B"), "Output should contain Product B.");
            assertTrue(output.contains("5"), "Output should contain quantity of Product A.");
            assertTrue(output.contains("3"), "Output should contain quantity of Product B.");
            assertTrue(output.contains("10.50"), "Output should contain price of Product A.");
            assertTrue(output.contains("20.00"), "Output should contain price of Product B.");
        } finally {
            // Restore System.out
            System.setOut(originalOut);
        }
    }

    @Test
    public void testViewProductsByPriceRangemutant3() throws Exception {//mutant3 passed (survived)
        // Add test data
        System.setOut(new PrintStream(outContent));
        try{
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (1, 'Product A', 2, 7.50)");
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (2, 'Product B', 3, 10.00)");
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (3, 'Product C', 7, 15.00)");
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (4, 'Product D', 6, 20.00)");
                stmt.execute("INSERT INTO products (id, name, quantity, price) VALUES (5, 'Product E', 10, 25.00)");
            }

            // Mock user input
            mockInput("7.50\n25.0\n");
            Scanner scanner = new Scanner(System.in);

            // Call the method
            mutant3.viewProductsByPriceRange(conn, scanner);

            // Capture the output
            String output = outContent.toString();


            assertTrue(output.contains("Product B"), "Output should contain Product B.");
            assertTrue(output.contains("3"), "Output should contain quantity of Product B.");
            assertTrue(output.contains("10.00"), "Output should contain price of Product B.");

            // Assertions for Product C
            assertTrue(output.contains("Product C"), "Output should contain Product C.");
            assertTrue(output.contains("7"), "Output should contain quantity of Product C.");
            assertTrue(output.contains("15.00"), "Output should contain price of Product C.");

            // Assertions for Product D
            assertTrue(output.contains("Product D"), "Output should contain Product D.");
            assertTrue(output.contains("6"), "Output should contain quantity of Product D.");
            assertTrue(output.contains("20.00"), "Output should contain price of Product D.");

            // Ensure excluded products are not present

        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testViewLowStockProductsmutant4() throws SQLException { //mutant4 passed(survived)
        // Step 1: Prepare test data (mock input for two products)

        mockInput("Low Stock Product\n2\n5.0\n");
        Scanner scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);

        mockInput("Normal Stock Product\n10\n20.0\n");
        scanner = new Scanner(System.in);
        inventorymanagementsystem.addProduct(conn, scanner);


        // Step 2: Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Step 3: Mock input for the low stock threshold and run the method
            mockInput("5\n");  // Threshold set to 5
            scanner = new Scanner(System.in);  // Reset scanner with new input
            mutant4.viewLowStockProducts(conn, scanner);

            // Step 4: Assert output
            String output = outContent.toString();
            assertTrue(output.contains("Low Stock Product"), "Output should contain low stock product.");
            assertFalse(output.contains("Normal Stock Product"), "Output should NOT contain normal stock product.");
            assertTrue(output.contains("2"), "Output should contain quantity of Low Stock Product.");
        } finally {
            // Step 5: Restore System.out
            System.setOut(originalOut);
        }
    }

    @Test
    public void testCalculateTotalInventoryValuemutant5() throws SQLException {//mutant5 passed (survived)
        Scanner scanner = new Scanner(System.in);

        // First product input
        mockInput("Product A\n5\n10.0\n");
        scanner = new Scanner(System.in);
        mutant5.addProduct(conn, scanner);

        // Second product input
        mockInput("Product B\n3\n20.0\n");
        scanner = new Scanner(System.in);
        mutant5.addProduct(conn, scanner);

        // Now calculate the total value
        mutant5.calculateTotalInventoryValue(conn);

        // Output verification would require capturing System.out
    }

}





