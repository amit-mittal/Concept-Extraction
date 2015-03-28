package sample.randomindexing;

import java.io.IOException;

import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.IncrementalDocVectors;
import pitt.search.semanticvectors.LuceneUtils;
import pitt.search.semanticvectors.TermTermVectorsFromLucene;
import pitt.search.semanticvectors.VectorStore;
import pitt.search.semanticvectors.VectorStoreWriter;
import pitt.search.semanticvectors.DocVectors.DocIndexingStrategy;

public class PositionalIndexer
{
    public static VectorStore newElementalTermVectors = null;
    public static TermTermVectorsFromLucene termTermIndexer;
    public static String[] args;
    
    public static void InitializeArgs()
    {
        args = new String[18];
        args[0] = "-vectortype";
        args[1] = "real";
        args[2] = "-dimension";
        args[3] = "2000";
        args[4] = "-seedlength";
        args[5] = "20";
        args[6] = "-docindexing";
        args[7] = "incremental";
        args[8] = "-windowradius";
        args[9] = "2";
        args[10] = "-positionalmethod";
        args[11] = "directional";
        args[12] = "-luceneindexpath";
        args[13] = "positional_index/";
        args[14] = "-minfrequency";
        args[15] = "2";
        args[16] = "-maxnonalphabetchars";
        args[17] = "3";
        //args[18] = "-indexfileformat";
        //args[19] = "text";
    }
    
    public static void BuildPositionalIndex() throws IllegalArgumentException
    {
        FlagConfig flagConfig;
        try
        {
            flagConfig = FlagConfig.getFlagConfig(args);
            args = flagConfig.remainingArgs;
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }

        if (flagConfig.luceneindexpath().isEmpty())
        {
            throw (new IllegalArgumentException("-luceneindexpath must be set."));
        }
        String luceneIndex = flagConfig.luceneindexpath();

        String termFile = "";
        switch (flagConfig.positionalmethod())
        {
        case BASIC:
            termFile = flagConfig.termtermvectorsfile();
            break;
        case PROXIMITY:
            termFile = flagConfig.proximityvectorfile();
            break;
        case PERMUTATION:
            termFile = flagConfig.permutedvectorfile();
            break;
        case PERMUTATIONPLUSBASIC:
            termFile = flagConfig.permplustermvectorfile();
            break;
        case DIRECTIONAL:
            termFile = flagConfig.directionalvectorfile();
            break;
        default:
            throw new IllegalArgumentException(
                    "Unrecognized -positionalmethod: "
                            + flagConfig.positionalmethod());
        }

        System.out.println("Building positional index, Lucene index: "
                + luceneIndex + ", Seedlength: " + flagConfig.seedlength()
                + ", Vector length: " + flagConfig.dimension()
                + ", Vector type: " + flagConfig.vectortype()
                + ", Minimum term frequency: " + flagConfig.minfrequency()
                + ", Maximum term frequency: " + flagConfig.maxfrequency()
                + ", Number non-alphabet characters: "
                + flagConfig.maxnonalphabetchars() + ", Window radius: "
                + flagConfig.windowradius() + "\n");

        try
        {
            termTermIndexer = new TermTermVectorsFromLucene(flagConfig,
                    newElementalTermVectors);

            VectorStoreWriter.writeVectors(termFile, flagConfig,
                    termTermIndexer.getSemanticTermVectors());

            for (int i = 1; i < flagConfig.trainingcycles(); ++i)
            {
                newElementalTermVectors = termTermIndexer
                        .getSemanticTermVectors();
                System.out
                        .println("\nRetraining with learned term vectors ...");
                termTermIndexer = new TermTermVectorsFromLucene(flagConfig,
                        newElementalTermVectors);
            }

            // Incremental indexing is hardcoded into BuildPositionalIndex.
            if (flagConfig.docindexing() != DocIndexingStrategy.NONE)
            {
                IncrementalDocVectors.createIncrementalDocVectors(
                        termTermIndexer.getSemanticTermVectors(), flagConfig,
                        new LuceneUtils(flagConfig));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
