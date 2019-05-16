package lib1;

import java.sql.*;

public class Javaconnect {
    
    Connection conn = null;
    public static Connection dbConnector()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("111");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tempProfile1?useSSL=false","root","utpal1997");  
            
            return conn;
        }
        catch(Exception e)
        {
            System.out.println("Error Bro" + e);
            return null;
        }
    }
}
