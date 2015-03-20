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

import pitt.search.lucene.*;
import pitt.search.semanticvectors.*;
import pitt.search.semanticvectors.DocVectors.DocIndexingStrategy;

public class RandomIndexer 
{
	static Path INDEX_DIR = FileSystems.getDefault().getPath("positional_index");
	static VectorStore newElementalTermVectors = null;
	
	public static void main(String[] args) 
	{
		String[] indexArgs = new String[5];
		indexArgs[0] = "C:/Users/amit/Desktop/test/pubmed_result_001.txt";
		indexArgs[1] = "-minfrequency";
		indexArgs[2] = "2";
		indexArgs[3] = "-maxnonalphabetchars";
		indexArgs[4] = "3";
		
		IndexFilePositions.main(indexArgs);
		
		String[] svArgs = new String[16];
		svArgs[0] = "-vectortype";
		svArgs[1] = "real";
		svArgs[2] = "-dimension";
		svArgs[3] = "100";
		svArgs[4] = "-seedlength";
		svArgs[5] = "15";
		svArgs[6] = "-docindexing";
		svArgs[7] = "incremental";
		svArgs[8] = "-windowradius";
		svArgs[9] = "2";
		svArgs[10] = "-positionalmethod";
		svArgs[11] = "basic";
		svArgs[12] = "-luceneindexpath";
		svArgs[13] = "positional_index/";
		svArgs[14] = "-indexfileformat";
		svArgs[15] = "text";
		
		//BuildPositionalIndex.main(svArgs);
		
		BuildPositionalIndex(svArgs);
	}
	
	static void BuildPositionalIndex(String[] args) throws IllegalArgumentException
	{
		FlagConfig flagConfig;
	    try {
	      flagConfig = FlagConfig.getFlagConfig(args);
	      args = flagConfig.remainingArgs;
	    } catch (IllegalArgumentException e) {
	      throw e;
	    }

	    if (flagConfig.luceneindexpath().isEmpty()) {
	      throw (new IllegalArgumentException("-luceneindexpath must be set."));
	    }
	    String luceneIndex = flagConfig.luceneindexpath();
	    
	    String termFile = "";
	    switch (flagConfig.positionalmethod()) {
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
	          "Unrecognized -positionalmethod: " + flagConfig.positionalmethod());
	    }

	    System.out.println("Building positional index, Lucene index: " + luceneIndex
	        + ", Seedlength: " + flagConfig.seedlength()
	        + ", Vector length: " + flagConfig.dimension()
	        + ", Vector type: " + flagConfig.vectortype()
	        + ", Minimum term frequency: " + flagConfig.minfrequency()
	        + ", Maximum term frequency: " + flagConfig.maxfrequency()
	        + ", Number non-alphabet characters: " + flagConfig.maxnonalphabetchars()
	        + ", Window radius: " + flagConfig.windowradius()
	        + "\n");

	    try {
	      TermTermVectorsFromLucene termTermIndexer = new TermTermVectorsFromLucene(
	          flagConfig, newElementalTermVectors);
	      
	      VectorStoreWriter.writeVectors(
	          termFile, flagConfig, termTermIndexer.getSemanticTermVectors());

	      for (int i = 1; i < flagConfig.trainingcycles(); ++i) {
	        newElementalTermVectors = termTermIndexer.getSemanticTermVectors();
	        System.out.println("\nRetraining with learned term vectors ...");
	        termTermIndexer = new TermTermVectorsFromLucene(
	            flagConfig,
	            newElementalTermVectors);
	      }

	      // Incremental indexing is hardcoded into BuildPositionalIndex.
	      if (flagConfig.docindexing() != DocIndexingStrategy.NONE) {
	        IncrementalDocVectors.createIncrementalDocVectors(
	            termTermIndexer.getSemanticTermVectors(), flagConfig, new LuceneUtils(flagConfig));
	      }
	      
	      tryStuff(termTermIndexer);
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	}
	
	public static void tryStuff(TermTermVectorsFromLucene termTermIndexer)
	{
		Enumeration<ObjectVector> v = termTermIndexer.getSemanticTermVectors().getAllVectors();
		System.out.println(termTermIndexer.getSemanticTermVectors().getVector("methodological"));
	}
}
