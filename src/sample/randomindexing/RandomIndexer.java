package sample.randomindexing;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.ObjectVector;
import pitt.search.semanticvectors.Search;
import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.VectorStoreReaderLucene;
import sample.criterias.Concept;
import sample.criterias.Criteria;
import sample.i2b2.CorpusHandler;
import sample.umls.UmlsHandler;
import sample.util.Constants;

public class RandomIndexer
{
    static VectorStoreReaderLucene storeReader;
    static HashMap<Integer, String> idWordMap;
    static HashMap<String, Integer> wordCategoryMap;

    static Concept problem = new Concept();
    static Concept treatment = new Concept();
    static Concept test = new Concept();
    static Concept none = new Concept();

    public static void main(String[] args) throws IOException
    {
        System.err.close();
        
        /*
         * DataIndexer indexer = new DataIndexer(); indexer.IndexDirectory();
         * 
         * PositionalIndexer.InitializeArgs();
         * PositionalIndexer.BuildPositionalIndex();
         */
        FlagConfig flagConfig = FlagConfig
                .getFlagConfig(PositionalIndexer.args);

        storeReader = new VectorStoreReaderLucene(Constants.WORD_VECTORS_FILE_PATH,
                flagConfig);

        // Load UMLS
        UmlsHandler umlsHandler = new UmlsHandler();
        umlsHandler.getDataFromSql();

        System.out.println("Making word category map");
        // 0. problem 1. treatment 2. test 3. none
        wordCategoryMap = new HashMap<String, Integer>();
        Enumeration<ObjectVector> v = storeReader.getAllVectors();
        
        // Populating word to category map
        while (v.hasMoreElements())
        {
            String word = v.nextElement().getObject().toString().toLowerCase();

            if (umlsHandler.problems.contains(word))
            {
                wordCategoryMap.put(word, 0);
            }
            else if (umlsHandler.treatments.contains(word))
            {
                wordCategoryMap.put(word, 1);
            }
            else if (umlsHandler.tests.contains(word))
            {
                wordCategoryMap.put(word, 2);
            }
            else
            {
                wordCategoryMap.put(word, 3);
            }
        }
        System.out.println("Making word category map...done");
        
        // Finding F-Score to determine the best model
        System.out.println("Finding F1-score");
        // FindFScores(); TODO
        System.out.println("Finding F1-score...done");

        // Loading i2b2 corpus
        CorpusHandler corpusHandler = new CorpusHandler();
        corpusHandler.loadCorpus();
        
        // Calculating the similarity matrix
        corpusHandler.generateSimilarityMatrix(storeReader);
    }

    public static void FindFScores()
    {
        int r = 0;
        for (String word : wordCategoryMap.keySet())
        {
            // TODO refactor below code
            int actualCategory = wordCategoryMap.get(word);
            // if(actualCategory == 3)
            //    continue;
            int predictedCategory = FindNearestNeighbor(word);
            if (predictedCategory == actualCategory)
            {
                // increment correct concept
                if (predictedCategory == 0)
                    problem.true_positive += 1;
                else if (predictedCategory == 1)
                    treatment.true_positive += 1;
                else if (predictedCategory == 2)
                    test.true_positive += 1;
                else
                    none.true_positive += 1;
            }
            else
            {
                if (predictedCategory == 0)
                    problem.false_positive += 1;
                else if (predictedCategory == 1)
                    treatment.false_positive += 1;
                else if (predictedCategory == 2)
                    test.false_positive += 1;
                else
                    none.false_positive += 1;

                if (actualCategory == 0)
                    problem.false_negative += 1;
                else if (actualCategory == 1)
                    treatment.false_negative += 1;
                else if (actualCategory == 2)
                    test.false_negative += 1;
                else
                    none.false_negative += 1;
            }

            ++r;
            if (r > 999)
                break;
        }
        
        // TODO confirm if finding f-measure in a right way
        System.out.println("Problem: " + problem);
        System.out.println("F1 Score for problem: " + problem.findF1Measure());

        System.out.println("Treatment: " + treatment);
        System.out.println("F1 Score for treatment: "
                + treatment.findF1Measure());

        System.out.println("Test: " + test);
        System.out.println("F1 Score for test: " + test.findF1Measure());
        
        System.out.println("None: " + none);
        System.out.println("F1 Score for none: " + none.findF1Measure());
        
        int total_true_positive = problem.true_positive + treatment.true_positive + test.true_positive + none.true_positive;
        int total_false_positive = problem.false_positive + treatment.false_positive + test.false_positive + none.false_positive;
        int total_false_negative = problem.false_negative + treatment.false_negative + test.false_negative + none.false_negative;
        
        float precision = Criteria.FindPrecision(total_true_positive, total_false_positive);
        float recall = Criteria.FindRecall(total_true_positive, total_false_negative);
        
        System.out.println("Combined: ");
        System.out.println("F1 Score for combined: " + Criteria.FindF1Measure(precision, recall));
    }
    

    public static int FindNearestNeighbor(String query)
    {
        String[] searchArgs = new String[3];
        searchArgs[0] = "-queryvectorfile";
        searchArgs[1] = Constants.WORD_VECTORS_FILE_PATH;
        searchArgs[2] = query;

        FlagConfig flagConfig;
        List<SearchResult> results;
        try
        {
            flagConfig = FlagConfig.getFlagConfig(searchArgs);
            results = Search.runSearch(flagConfig);

            double categories[] = new double[]
            { 0.0, 0.0, 0.0, 0.0 };
            for (int i = 1; i < results.size(); ++i)
            {
                SearchResult result = results.get(i);
                String w = result.getObjectVector().getObject().toString()
                        .toLowerCase();
                
                if(result.getScore() > 0)
                {
                    categories[wordCategoryMap.get(w)] += result.getScore();
                }
            }

            // TODO refactor below function
            double largest = categories[0];
            int index = 0;
            for (int i = 1; i < categories.length; i++)
            {
                if (categories[i] > largest)
                {
                    largest = categories[i];
                    index = i;
                }
            }

            return index;
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            throw e;
        }
    }
}
