package sample.i2b2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
    public double[][] matrix;
    
    public void loadCorpus() throws IOException
    {
        reader = DirectoryReader.open(FSDirectory.open(new File(
                "C:/Users/amit/workspace/Concept-Extraction/index")));
    }
    
    public void generateSimilarityMatrix(VectorStoreReaderLucene storeReader) throws IOException
    {
        generateIntersectionWithCorpus(storeReader.getAllVectors());
        
        System.out.println("Generating similarity matrix...");
        int size = wordsList.size();
        
        matrix = new double[size][size];
        for (int i = 0; i < 50; ++i)
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
        
        System.out.println("Generating similarity matrix...done!!");
        
        System.out.println("Writing data structure to files");
        FileOutputStream fos = new FileOutputStream("wordlist");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(wordsList);
        oos.close();
        
        FileOutputStream fos2 = new FileOutputStream("similarity_matrix");
        ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
        oos2.writeObject(matrix);
        oos2.close();
        System.out.println("Writing data structure to files...done");
    }
    
    private void generateIntersectionWithCorpus(Enumeration<ObjectVector> vectors)
    {
        System.out.println("Taking intersection of medline with i2b2 corpus...");
        
        wordsList = new ArrayList<String>();
        
        while(vectors.hasMoreElements())
        {
            String word = vectors.nextElement().getObject().toString();
            if(ifWordPresent(word))
            {
                wordsList.add(word);
            }
        }
        
        System.out.println("Found " + wordsList.size() + " common strings");
    }
    
    private boolean ifWordPresent(String word)
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
