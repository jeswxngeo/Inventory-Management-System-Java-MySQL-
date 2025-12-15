import dao.ProductDAO;
import dao.SaleDAO;
import models.Product;
import models.Sale;
import java.util.*;
import java.sql.SQLException; // Import SQLException explicitly for clearer exception handling

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Inventory Management Menu ---");
            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. View Limited Products");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Register Sale");
            System.out.println("7. View Sales Summary");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume the invalid input
                continue; // Restart the loop
            }
            scanner.nextLine(); // Consume newline left-over

            try {
                switch (choice) {
                    case 1:
                        addProduct(scanner);
                        break;
                    case 2:
                        viewAllProducts();
                        break;
                    case 3:
                        viewLimitedProducts(scanner);
                        break;
                    case 4:
                        updateProduct(scanner);
                        break;
                    case 5:
                        deleteProduct(scanner);
                        break;
                    case 6:
                        registerSale(scanner);
                        break;
                    case 7:
                        viewSalesSummary();
                        break;
                    case 8:
                        System.out.println("Exiting application. Goodbye!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                // For debugging, keep this:
                // e.printStackTrace();
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                // For debugging, keep this:
                // e.printStackTrace();
            }
        }
    }

    private static void addProduct(Scanner scanner) throws SQLException {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();

        int qty = getPositiveIntInput(scanner, "Enter quantity: ");
        if (qty == -1) return; // Error in input

        double price = getPositiveDoubleInput(scanner, "Enter price: ");
        if (price == -1.0) return; // Error in input

        Product p = new Product(0, name, qty, price); // ID is 0, as DB auto-generates
        ProductDAO.addProduct(p);
        System.out.println("Product added successfully!");
    }

    private static void viewAllProducts() throws SQLException {
        List<Product> products = ProductDAO.getAllProducts();
        displayProducts(products, "All Products");
    }

    private static void viewLimitedProducts(Scanner scanner) throws SQLException {
        int limit = getPositiveIntInput(scanner, "Enter limit for products to view: ");
        if (limit == -1) return; // Error in input

        List<Product> products = ProductDAO.getLimitedProducts(limit);
        displayProducts(products, "Limited Products (Top " + limit + ")");
    }

    private static void updateProduct(Scanner scanner) throws SQLException {
        int productId = getPositiveIntInput(scanner, "Enter product ID to update: ");
        if (productId == -1) return;

        Product existingProduct = ProductDAO.getProductById(productId);
        if (existingProduct == null) {
            System.out.println("Product with ID " + productId + " not found.");
            return;
        }

        System.out.println("\nUpdating Product: " + existingProduct.getName());
        System.out.print("Enter new product name (current: " + existingProduct.getName() + "): ");
        String newName = scanner.nextLine();
        newName = newName.isEmpty() ? existingProduct.getName() : newName; // Keep current if empty

        int newQty = getPositiveIntInput(scanner, "Enter new quantity (current: " + existingProduct.getQuantity() + "): ");
        newQty = (newQty == -1) ? existingProduct.getQuantity() : newQty; // Keep current if invalid or skipped

        double newPrice = getPositiveDoubleInput(scanner, "Enter new price (current: " + String.format("%.2f", existingProduct.getPrice()) + "): ");
        newPrice = (newPrice == -1.0) ? existingProduct.getPrice() : newPrice; // Keep current if invalid or skipped

        Product updatedProduct = new Product(productId, newName, newQty, newPrice);
        ProductDAO.updateProduct(updatedProduct);
        System.out.println("Product updated successfully!");
    }

    private static void deleteProduct(Scanner scanner) throws SQLException {
        int productId = getPositiveIntInput(scanner, "Enter product ID to delete: ");
        if (productId == -1) return;

        Product existingProduct = ProductDAO.getProductById(productId);
        if (existingProduct == null) {
            System.out.println("Product with ID " + productId + " not found.");
            return;
        }

        System.out.print("Are you sure you want to delete '" + existingProduct.getName() + "' (ID: " + productId + ")? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("yes")) {
            ProductDAO.deleteProduct(productId);
            System.out.println("Product deleted successfully!");
        } else {
            System.out.println("Product deletion cancelled.");
        }
    }

    private static void registerSale(Scanner scanner) throws SQLException {
        int productId = getPositiveIntInput(scanner, "Enter Product ID for sale: ");
        if (productId == -1) return;

        Product productToSell = ProductDAO.getProductById(productId);
        if (productToSell == null) {
            System.out.println("Product with ID " + productId + " not found.");
            return;
        }

        System.out.println("Product selected: " + productToSell.getName() + " (Available: " + productToSell.getQuantity() + ")");
        int quantityToSell = getPositiveIntInput(scanner, "Enter quantity to sell: ");
        if (quantityToSell == -1) return;

        if (quantityToSell > productToSell.getQuantity()) {
            System.out.println("Error: Not enough stock. Only " + productToSell.getQuantity() + " available.");
            return;
        }

        // Create Sale object
        Sale newSale = new Sale(productId, quantityToSell);
        SaleDAO.addSale(newSale);

        // Update product quantity
        int updatedStock = productToSell.getQuantity() - quantityToSell;
        ProductDAO.updateProductQuantity(productId, updatedStock);

        System.out.println("Sale registered successfully! Remaining stock for " + productToSell.getName() + ": " + updatedStock);
    }

    private static void viewSalesSummary() throws SQLException {
        List<SaleDAO.SaleSummaryDTO> summary = SaleDAO.getSalesSummary();
        System.out.println("\n--- Sales Summary ---");
        if (summary.isEmpty()) {
            System.out.println("No sales recorded yet.");
            return;
        }
        System.out.printf("%-20s %-15s %-15s\n", "Product Name", "Total Qty Sold", "Total Revenue");
        System.out.println("--------------------------------------------------");
        for (SaleDAO.SaleSummaryDTO item : summary) {
            System.out.printf("%-20s %-15d %-15.2f\n",
                    item.productName, item.totalQuantitySold, item.totalRevenue);
        }
        System.out.println("--------------------------------------------------");
    }

    // --- Helper Methods for Input ---
    private static int getPositiveIntInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine(); // Consume bad input
            System.out.print(prompt);
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (value < 0) {
            System.out.println("Input must be a positive number.");
            return -1; // Indicate error
        }
        return value;
    }

    private static double getPositiveDoubleInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine(); // Consume bad input
            System.out.print(prompt);
        }
        double value = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        if (value < 0) {
            System.out.println("Input must be a positive number.");
            return -1.0; // Indicate error
        }
        return value;
    }

    private static void displayProducts(List<Product> products, String title) {
        System.out.println("\n--- " + title + " ---");
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.printf("%-5s | %-20s | %-10s | %-10s\n", "ID", "Name", "Quantity", "Price");
        System.out.println("----------------------------------------------------");
        for (Product prod : products) {
            System.out.printf("%-5d | %-20s | %-10d | %-10.2f\n",
                    prod.getId(), prod.getName(), prod.getQuantity(), prod.getPrice());
        }
        System.out.println("----------------------------------------------------");
    }
}
