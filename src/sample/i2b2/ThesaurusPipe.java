package sample.i2b2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.hamcrest.core.IsSame;

import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.VectorStoreReaderLucene;
import pitt.search.semanticvectors.vectors.Vector;
import sample.randomindexing.PositionalIndexer;
import sample.util.Constants;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;

public class ThesaurusPipe extends Pipe implements Serializable
{
    private static final long serialVersionUID = 79533921516L;
    
    String prefix;
    int leftBoundary;
    int rightBoundary;
    VectorStoreReaderLucene storeReader;
    boolean ifSimilarityMatrixAvailable;
    
    List<String> wordsList;
    double[][] matrix;

    public ThesaurusPipe(String prefix, int leftBoundary, int rightBoundary, boolean ifSimilarityMatrixAvailable) throws IOException
    {
        this.prefix = prefix;
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
        
        FlagConfig flagConfig = FlagConfig
                .getFlagConfig(PositionalIndexer.args);
        
        FileInputStream fileIn = new FileInputStream(Constants.WORD_LIST_FILE_PATH);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        try
        {
            wordsList = (List<String>) in.readObject();
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        in.close();
        fileIn.close();
        
        
        FileInputStream fileIn2 = new FileInputStream(Constants.SIMILARITY_MATRIX_FILE_PATH);
        ObjectInputStream in2 = new ObjectInputStream(fileIn2);
        try
        {
            matrix =  (double[][]) in2.readObject();
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        in2.close();
        fileIn2.close();
        
        
        // TODO implement its use
        this.ifSimilarityMatrixAvailable = ifSimilarityMatrixAvailable;
    }
    
    public ThesaurusPipe(String prefix, int leftBoundary, int rightBoundary) throws IOException
    {
        this.prefix = prefix;
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
        
        FlagConfig flagConfig = FlagConfig
                .getFlagConfig(PositionalIndexer.args);
        storeReader = new VectorStoreReaderLucene(Constants.WORD_VECTORS_FILE_PATH,
                flagConfig);
        
        this.ifSimilarityMatrixAvailable = false;
    }

    @Override
    public Instance pipe(Instance carrier)
    {
        TokenSequence ts = (TokenSequence) carrier.getData();
        int tsSize = ts.size();
        
        if(ifSimilarityMatrixAvailable){
            for (int i = 0; i < tsSize; i++)
            {
                Token t = (Token) ts.get(i);
                
                if(wordsList.contains(t.getText()))
                {
                    int list_i = wordsList.indexOf(t.getText());
                    for (int position = i + leftBoundary; position < i + rightBoundary; position++)
                    {
                        int index = position - i;
                        if (position == i || position < 0 || position >= tsSize)
                            continue;

                        Token e = (Token) ts.get(position);
                        if(wordsList.contains(e.getText()))
                        {
                            int list_i2 = wordsList.indexOf(e.getText());
                            //if(matrix[list_i][list_i2] >= 0.7){
                            t.setFeatureValue(prefix + index, matrix[list_i][list_i2]);
                            //}
                        }
                    }
                }
            }
            
            return carrier;
        }
        
        
        for (int i = 0; i < tsSize; i++)
        {
            Token t = (Token) ts.get(i);
            
            Vector v1 = null;
            if(storeReader.containsVector(t.getText())){
                v1 = storeReader.getVector(t.getText());
            }
            
            if(v1 == null){
                for (int position = i + leftBoundary; position < i + rightBoundary; position++)
                {
                    int index = position - i;
                    if (position == i)
                        continue;

                    t.setNumericProperty(prefix + index, 0.0);
                }
                
                continue;
            }
            
            for (int position = i + leftBoundary; position < i + rightBoundary; position++)
            {
                int index = position - i;
                if (position == i)
                    continue;

                if (position < 0 || position >= tsSize)
                {
                    t.setNumericProperty(prefix + index, 0.0);
                }
                else{
                    Token e = (Token) ts.get(position);
                    if(storeReader.containsVector(e.getText())){
                        Vector v2 = storeReader.getVector(e.getText());
                        t.setNumericProperty(prefix + index, v1.measureOverlap(v2));
                    }
                    else{
                        t.setNumericProperty(prefix + index, 0.0);
                    }
                }
            }
        }
        
        return carrier;
    }

}
