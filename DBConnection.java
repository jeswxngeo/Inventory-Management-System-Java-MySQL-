package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/sales_inventory";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";


    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Make sure driver is loaded
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // Will throw if JAR is not linked properly
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
