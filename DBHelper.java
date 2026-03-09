import java.sql.*;

public class DBHelper {
    public static Connection connect() throws Exception{
        String url = "jdbc:mysql://localhost:3306/billing";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }
}
