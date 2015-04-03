package sample.i2b2;

import java.io.IOException;
import java.io.Serializable;

import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.VectorStoreReaderLucene;
import pitt.search.semanticvectors.vectors.Vector;
import sample.randomindexing.PositionalIndexer;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;
import edu.umass.cs.mallet.base.util.PropertyList;

public class ThesaurusPipe extends Pipe implements Serializable
{
    String prefix;
    int leftBoundary;
    int rightBoundary;
    VectorStoreReaderLucene storeReader;

    public ThesaurusPipe(String prefix, int leftBoundary, int rightBoundary) throws IOException
    {
        this.prefix = prefix;
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
        
        FlagConfig flagConfig = FlagConfig
                .getFlagConfig(PositionalIndexer.args);
        storeReader = new VectorStoreReaderLucene("drxntermvectors.bin",
                flagConfig);
    }

    @Override
    public Instance pipe(Instance carrier)
    {
        TokenSequence ts = (TokenSequence) carrier.getData();
        int tsSize = ts.size();
        PropertyList[] newFeatures = new PropertyList[tsSize];
        
        for (int i = 0; i < tsSize; i++)
        {
            Token t = (Token) ts.get(i);
            PropertyList pl = t.getFeatures();
            newFeatures[i] = pl;
            
            Vector v1 = null;
            if(storeReader.containsVector(t.getText())){
                v1 = storeReader.getVector(t.getText());
            }
            
            for (int position = i + leftBoundary; position < i + rightBoundary; position++)
            {
                int index = position - i;
                if (position == i)
                    continue;

                if (position < 0 || position >= tsSize)
                {
                    newFeatures[i] = PropertyList
                            .add(prefix + index + "=", 0.0, newFeatures[i]);
                }
                else{
                    Token e = (Token) ts.get(position);
                    if(storeReader.containsVector(e.getText())){
                        Vector v2 = storeReader.getVector(e.getText());
                        newFeatures[i] = PropertyList
                                .add(prefix + index + "=", v1.measureOverlap(v2), newFeatures[i]);
                    }
                    else{
                        newFeatures[i] = PropertyList
                                .add(prefix + index + "=", 0.0, newFeatures[i]);
                    }
                }
            }
        }
        
        for (int i = 0; i < tsSize; i++)
        {
            // Put the new PropertyLists in place
            ((Token) ts.get(i)).setFeatures(newFeatures[i]);
        }
        
        return carrier;
    }

}
