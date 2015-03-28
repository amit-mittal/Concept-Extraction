package sample.umls;

public class SqlQuery
{
    public static String GET_ALL_MEDICAL_PROBLEMS1 = "SELECT DISTINCT(c.STR) FROM mrconso c, mrsty s "
            + "WHERE (s.TUI = 'T005' OR s.TUI = 'T007' OR s.TUI = 'T019' OR s.TUI = 'T020' "
            + "OR s.TUI = 'T037' OR s.TUI = 'T046' OR s.TUI = 'T047' OR s.TUI = 'T048'  "
            + "OR s.TUI = 'T049' OR s.TUI = 'T148' OR s.TUI = 'T190' OR s.TUI = 'T191') "
            + "AND c.CUI = s.CUI";
    
    public static String GET_ALL_MEDICAL_TREATMENTS1 = "SELECT DISTINCT(c.STR) FROM mrconso c, mrsty s "
            + "WHERE (s.TUI = 'T061' OR s.TUI = 'T074' OR s.TUI = 'T110' OR s.TUI = 'T121' "
            + "OR s.TUI = 'T122' OR s.TUI = 'T195' OR s.TUI = 'T200' OR s.TUI = 'T203') "
            + "AND c.CUI = s.CUI";
    
    public static String GET_ALL_MEDICAL_TESTS1 = "SELECT DISTINCT(c.STR) FROM mrconso c, mrsty s "
            + "WHERE (s.TUI = 'T059' OR s.TUI = 'T060') "
            + "AND c.CUI = s.CUI";
    
    public static String GET_ALL_MEDICAL_PROBLEMS = "SELECT STR FROM problems";
    
    public static String GET_ALL_MEDICAL_TREATMENTS = "SELECT STR FROM treatments LIMIT 100000";
    
    public static String GET_ALL_MEDICAL_TESTS = "SELECT STR FROM tests";
}
