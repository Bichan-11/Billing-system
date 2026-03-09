import java.sql.*;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/billing";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static Connection open() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
