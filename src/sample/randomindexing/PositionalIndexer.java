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

import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.IncrementalDocVectors;
import pitt.search.semanticvectors.LuceneUtils;
import pitt.search.semanticvectors.TermTermVectorsFromLucene;
import pitt.search.semanticvectors.VectorStore;
import pitt.search.semanticvectors.VectorStoreWriter;
import pitt.search.semanticvectors.DocVectors.DocIndexingStrategy;
import sample.util.Constants;

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
        args[9] = "4";
        args[10] = "-positionalmethod";
        args[11] = "directional";
        args[12] = "-luceneindexpath";
        args[13] = Constants.POSITIONAL_INDEX_FOLDER_PATH;
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
