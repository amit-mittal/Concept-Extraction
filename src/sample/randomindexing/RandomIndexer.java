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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.ObjectVector;
import pitt.search.semanticvectors.Search;
import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.TermTermVectorsFromLucene;
import pitt.search.semanticvectors.VectorStore;
import pitt.search.semanticvectors.VectorStoreReaderLucene;

public class RandomIndexer
{
    static VectorStoreReaderLucene storeReader;
    static HashMap<Integer, String> idWordMap;
    static HashMap<String, Integer> wordCategoryMap;

    public static void main(String[] args) throws IOException
    {
        DataIndexer indexer = new DataIndexer();
        // indexer.IndexDirectory();

        PositionalIndexer.InitializeArgs();
        // PositionalIndexer.BuildPositionalIndex();
        FlagConfig flagConfig = FlagConfig.getFlagConfig(PositionalIndexer.args);
        
        storeReader = new VectorStoreReaderLucene("drxntermvectors.bin", flagConfig);
        
        // add query vector parameter so that can do for custom file
        // also file format should be binary
        String[] searchArgs = new String[3];
        searchArgs[0] = "-queryvectorfile";
        searchArgs[1] = "drxntermvectors.bin";

        // PopulateMaps();
        // FindNearestNeighbors(searchArgs);
        tryStuff();

        // TODO Load UMLS
        // TODO Find F-Score and Accuracy to determine the best model

        // Then find the similarity matrix
        // GenerateSimilarityMatrix();
    }

    public static void PopulateMaps()
    {
        idWordMap = new HashMap<Integer, String>();
        // 1. problem 2. treatment 3. test 4. none
        wordCategoryMap = new HashMap<String, Integer>();
        Enumeration<ObjectVector> v = storeReader.getAllVectors();

        // Populating id to word map
        int rank = 0;
        while (v.hasMoreElements())
        {
            String word = v.nextElement().getObject().toString();
            idWordMap.put(rank, word);
            ++rank;

            // if word this then assign this no.
            wordCategoryMap.put(word, 0);
        }
    }

    public static void FindNearestNeighbors(String[] args)
    {
        FlagConfig flagConfig;
        List<SearchResult> results;
        try
        {
            for (int i = 0; i < idWordMap.size(); ++i)
            {
                args[2] = idWordMap.get(i);
                flagConfig = FlagConfig.getFlagConfig(args);
                results = Search.runSearch(flagConfig);

                int actualCategory = wordCategoryMap.get(args[2]);
                double categories[] = new double[]
                { 0.0, 0.0, 0.0, 0.0 };
                for (SearchResult result : results)
                {
                    String w = result.getObjectVector().getObject().toString();
                    categories[wordCategoryMap.get(w)] += result.getScore();
                }

                // find max likelihood category and then calculate mismatches
                // etc.
            }

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
