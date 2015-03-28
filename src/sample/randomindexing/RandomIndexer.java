/**
   Copyright 2008 and ongoing, the SemanticVectors AUTHORS.
   All rights reserved.
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the following disclaimer
   in the documentation and/or other materials provided with the
   distribution.
 * Neither the name of Google Inc. nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

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
import sample.umls.UmlsHandler;

public class RandomIndexer
{
    static VectorStoreReaderLucene storeReader;
    static HashMap<Integer, String> idWordMap;
    static HashMap<String, Integer> wordCategoryMap;

    static Concept problem = new Concept();
    static Concept treatment = new Concept();
    static Concept test = new Concept();

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

        storeReader = new VectorStoreReaderLucene("drxntermvectors.bin",
                flagConfig);

        // add query vector parameter so that can do for custom file
        // also file format should be binary

        // PopulateMaps();
        // FindNearestNeighbors(searchArgs);
        // tryStuff();

        // TODO Load UMLS
        UmlsHandler umlsHandler = new UmlsHandler();
        umlsHandler.getDataFromSql();

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
        
        // TODO Find F-Score and Accuracy to determine the best model
        FindFScores();

        // Then find the similarity matrix
        // GenerateSimilarityMatrix();
    }

    public static void FindFScores()
    {
        int r = 0;
        for (String word : wordCategoryMap.keySet())
        {
            ++r;
            // TODO refactor below code
            int actualCategory = wordCategoryMap.get(word);
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
            }
            else
            {
                if (predictedCategory == 0)
                    problem.false_positive += 1;
                else if (predictedCategory == 1)
                    treatment.false_positive += 1;
                else if (predictedCategory == 2)
                    test.false_positive += 1;

                if (actualCategory == 0)
                    problem.false_negative += 1;
                else if (actualCategory == 1)
                    treatment.false_negative += 1;
                else if (actualCategory == 2)
                    test.false_negative += 1;
            }

            if (r > 60)
                break;
        }

        System.out.println("Problem: " + problem);
        System.out.println("F1 Score for problem: " + problem.findF1Measure());

        System.out.println("Treatment: " + treatment);
        System.out.println("F1 Score for treatment: "
                + treatment.findF1Measure());

        System.out.println("Test: " + test);
        System.out.println("F1 Score for test: " + test.findF1Measure());
    }

    public static int FindNearestNeighbor(String query)
    {
        String[] searchArgs = new String[3];
        searchArgs[0] = "-queryvectorfile";
        searchArgs[1] = "drxntermvectors.bin";
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
                categories[wordCategoryMap.get(w)] += result.getScore();
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

            return index + 1;
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }
    }

    public static void GenerateSimilarityMatrix()
    {
        Enumeration<ObjectVector> vi = storeReader.getAllVectors();
        int size = storeReader.getNumVectors();

        // TODO optimize this
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; ++i)
        {
            ObjectVector m = vi.nextElement();
            Enumeration<ObjectVector> vj = storeReader.getAllVectors();
            for (int j = 0; j < size; ++j)
            {
                ObjectVector n = vj.nextElement();
                matrix[i][j] = m.getVector().measureOverlap(n.getVector());
            }
        }
    }

    public static void tryStuff()
    {
        Enumeration<ObjectVector> v = storeReader.getAllVectors();
        System.out.println(storeReader.getVector("exercise"));

        System.out.println(v.nextElement().getObject()); // this gets the tag
                                                         // value
        System.out.println(v.nextElement().getVector()); // this gets the vector
    }
}
