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
        
        System.out.println("Connected to SQL Database!!");
    }

    public void disconnect()
    {
        try
        {
            conn.close();
            System.out.println("Disconnected from SQL Database!!");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public TreeSet<String> getMedicalProblems()
    {
        System.out.println("Getting Medical Problems");
        TreeSet<String> problems = getSetFromSqlQuery(SqlQuery.GET_ALL_MEDICAL_PROBLEMS);

        return problems;
    }
    
    public TreeSet<String> getMedicalTreatments()
    {
        System.out.println("Getting Medical Treatments");
        TreeSet<String> treatments = getSetFromSqlQuery(SqlQuery.GET_ALL_MEDICAL_TREATMENTS);

        return treatments;
    }
    
    public TreeSet<String> getMedicalTests()
    {
        System.out.println("Getting Medical Tests");
        TreeSet<String> tests = getSetFromSqlQuery(SqlQuery.GET_ALL_MEDICAL_TESTS);

        return tests;
    }
    
    private TreeSet<String> getSetFromSqlQuery(String query)
    {
        TreeSet<String> set = new TreeSet<String>();  
        Statement stmt = null;
        try
        {
            // Execute a query
            System.out.println("Executing query...");
            stmt = conn.createStatement();
            
            ResultSet resultSet = stmt.executeQuery(query);
            
            // Extract data from result set
            while (resultSet.next())
            {
                // Retrieve by column name and converting to lower case
                String str = resultSet.getString("STR").toLowerCase();

                // Adding string to list
                set.add(str);
            }
            
            System.out.println("Found " + set.size() + " rows");
            
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
        
        return set;
    }
}
