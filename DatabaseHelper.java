import java.sql.*;

public class DatabaseHelper {
    public static Connection open() throws Exception{
        String url = "jdbc:mysql://localhost:3306/billing";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }
}
