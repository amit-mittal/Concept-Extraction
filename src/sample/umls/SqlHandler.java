package sample.umls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;

public class SqlHandler
{
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/2014ab";

    // Database credentials
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection conn = null;

    public void connect()
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

    public void disconnect()
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

    public TreeSet<String> getMedicalProblems()
    {
        TreeSet<String> problems = getListFromSqlQuery(SqlQuery.GET_ALL_MEDICAL_PROBLEMS);

        return problems;
    }
    
    public TreeSet<String> getMedicalTreatments()
    {
        TreeSet<String> treatments = getListFromSqlQuery(SqlQuery.GET_ALL_MEDICAL_TREATMENTS);

        return treatments;
    }
    
    public TreeSet<String> getMedicalTests()
    {
        TreeSet<String> tests = getListFromSqlQuery(SqlQuery.GET_ALL_MEDICAL_TESTS);

        return tests;
    }
    
    private TreeSet<String> getListFromSqlQuery(String query)
    {
        TreeSet<String> set = new TreeSet<String>();  
        Statement stmt = null;
        try
        {
         // Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            
            ResultSet resultSet = stmt.executeQuery(query);
            
            // Extract data from result set
            while (resultSet.next())
            {
                // Retrieve by column name
                String str = resultSet.getString("STR");

                // Adding string to list
                set.add(str);
            }
            // Clean-up environment
            resultSet.close();
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
                {
                    stmt.close();
                }
            }
            catch (SQLException se2)
            {
                // nothing we can do
            }
        }
        
        System.out.println(set.size());

        return set;
    }
}
