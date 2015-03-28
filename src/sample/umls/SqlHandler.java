package sample.umls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlHandler
{
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/2014ab";

    // Database credentials
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection conn = null;

    public void Connect()
    {
        try
        {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        }
        catch (Exception e)
        {
            // Handle errors for JDBC
            e.printStackTrace();
        }
        
        System.out.println("Connected!!");
    }

    public void Disconnect()
    {
        try
        {
            conn.close();
            System.out.println("Disconnected!!");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> GetMedicalProblems()
    {
        List<String> problems = new ArrayList<String>();
        Statement stmt = null;
        try
        {
            // Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SqlQuery.GET_ALL_MEDICAL_PROBLEMS);

            // Extract data from result set
            while (rs.next())
            {
                // Retrieve by column name
                String str = rs.getString("STR");

                // Adding string to list
                problems.add(str);
            }
            // Clean-up environment
            rs.close();
            stmt.close();
        }
        catch (Exception e)
        {
            // Handle errors for Class.forName
            e.printStackTrace();
        }
        finally
        {
            // finally block used to close resources
            try
            {
                if (stmt != null)
                    stmt.close();
            }
            catch (SQLException se2)
            {
                // nothing we can do
            }
        }
        
        System.out.println(problems.size());

        return problems;
    }
}
