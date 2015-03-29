package sample.i2b2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

import pitt.search.semanticvectors.ObjectVector;
import pitt.search.semanticvectors.VectorStoreReaderLucene;
import pitt.search.semanticvectors.vectors.Vector;

public class CorpusHandler
{
    public IndexReader reader;
    public List<String> wordsList;
    
    public void loadCorpus() throws IOException
    {
        reader = DirectoryReader.open(FSDirectory.open(new File(
                "C:/Users/amit/workspace/Concept-Extraction/index")));
    }
    
    public void generateIntersectionWithCorpus(Enumeration<ObjectVector> vectors)
    {
        wordsList = new ArrayList<String>();
        
        while(vectors.hasMoreElements())
        {
            String word = vectors.nextElement().getObject().toString();
            if(ifWordPresent(word))
            {
                wordsList.add(word);
            }
        }
    }
    
    public void generateSimilarityMatrix(VectorStoreReaderLucene storeReader)
    {
        generateIntersectionWithCorpus(storeReader.getAllVectors());
        
        int size = wordsList.size();
        System.out.println(size);
        
        // TODO optimize this
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; ++i)
        {
            matrix[i][i] = 1;
            
            String word1 = wordsList.get(i);
            Vector v1 = storeReader.getVector(word1);
            for (int j = 0; j < i; ++j)
            {
                String word2 = wordsList.get(j);
                Vector v2 = storeReader.getVector(word2);
                
                matrix[i][j] = v1.measureOverlap(v2);
                matrix[j][i] = matrix[i][j];
            }
        }
    }
    
    boolean ifWordPresent(String word)
    {
        Term t = new Term("contents", word);
        try
        {
            return (reader.docFreq(t) > 0);    
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
}