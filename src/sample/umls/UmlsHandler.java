package sample.umls;

import java.util.TreeSet;

public class UmlsHandler
{
    public SqlHandler sqlHandler;
    public TreeSet<String> problems;
    public TreeSet<String> treatments;
    public TreeSet<String> tests;
    
    public UmlsHandler()
    {
        sqlHandler = new SqlHandler();
        sqlHandler.connect();
        
        problems = new TreeSet<String>();
        treatments = new TreeSet<String>();
        tests = new TreeSet<String>();
    }
    
    public void getDataFromSql()
    {
        problems = sqlHandler.getMedicalProblems();
        treatments = sqlHandler.getMedicalTreatments();
        tests = sqlHandler.getMedicalTests();
        
        // TODO see if should write list to some binary format and load from there
        
        sqlHandler.disconnect();
    }
}
