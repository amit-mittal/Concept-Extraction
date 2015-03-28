package sample.umls;

public class SqlQuery
{
    public static String GET_ALL_MEDICAL_PROBLEMS = "SELECT DISTINCT(c.STR) FROM mrconso c, mrsty s "
            + "WHERE s.TUI = 'T005' AND c.CUI = s.CUI LIMIT 10";
}
