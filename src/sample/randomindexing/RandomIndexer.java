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

import pitt.search.lucene.IndexFilePositions;
import pitt.search.semanticvectors.DocVectors.DocIndexingStrategy;
import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.IncrementalDocVectors;
import pitt.search.semanticvectors.LuceneUtils;
import pitt.search.semanticvectors.ObjectVector;
import pitt.search.semanticvectors.Search;
import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.TermTermVectorsFromLucene;
import pitt.search.semanticvectors.VectorStore;
import pitt.search.semanticvectors.VectorStoreWriter;

public class RandomIndexer
{
	static Path INDEX_DIR = FileSystems.getDefault()
			.getPath("positional_index");
	static VectorStore newElementalTermVectors = null;
	static TermTermVectorsFromLucene termTermIndexer;
	static HashMap<Integer, String> idWordMap;
	static HashMap<String, Integer> wordCategoryMap;

	public static void main(String[] args)
	{
		String[] indexArgs = new String[5];
		indexArgs[0] = "C:/Users/amit/Desktop/test/pubmed_result_001.txt";
		indexArgs[1] = "-minfrequency";
		indexArgs[2] = "2";
		indexArgs[3] = "-maxnonalphabetchars";
		indexArgs[4] = "3";

		//IndexFilePositions.main(indexArgs);

		String[] svArgs = new String[14];
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
		svArgs[11] = "directional";
		svArgs[12] = "-luceneindexpath";
		svArgs[13] = "positional_index/";
		// svArgs[14] = "-indexfileformat";
		// svArgs[15] = "text";

		// BuildPositionalIndex.main(svArgs);

		BuildPositionalIndex(svArgs);

		// add query vector parameter so that can do for custom file
		// also file format should be binary
		String[] searchArgs = new String[3];
		searchArgs[0] = "-queryvectorfile";
		searchArgs[1] = "drxntermvectors.bin";
		
		// TODO Find F-Score and Accuracy to determine the best model
		PopulateMaps();
		FindNearestNeighbors(searchArgs);
		tryStuff();
		
		// Then find the similarity matrix
		GenerateSimilarityMatrix();
	}

	static void BuildPositionalIndex(String[] args)
			throws IllegalArgumentException
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
			termTermIndexer = new TermTermVectorsFromLucene(
					flagConfig, newElementalTermVectors);

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
	
	public static void PopulateMaps()
	{
		idWordMap = new HashMap<Integer, String>();
		// 1. problem 2. treatment 3. test 4. none
		wordCategoryMap = new HashMap<String, Integer>(); 
		Enumeration<ObjectVector> v = termTermIndexer.getSemanticTermVectors()
				.getAllVectors();
		
		// Populating id to word map
		int rank = 0;
		while(v.hasMoreElements())
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
			for(int i = 0 ; i < idWordMap.size() ; ++i)
			{
				args[2] = idWordMap.get(i);
				flagConfig = FlagConfig.getFlagConfig(args);
				results = Search.runSearch(flagConfig);
				
				int actualCategory = wordCategoryMap.get(args[2]);
				double categories[] = new double[]{0.0, 0.0, 0.0, 0.0};
				for (SearchResult result : results)
				{
					String w = result.getObjectVector().getObject().toString();
					categories[wordCategoryMap.get(w)] += result.getScore();
				}
				
				// find max likelihood category and then calculate mismatches etc.
			}
			
		}
		catch (IllegalArgumentException e)
		{
			throw e;
		}

		// Print out results.
		/*int ranking = 0;
		if (results.size() > 0)
		{
			System.out.println("Search output follows ...\n");
			for (SearchResult result : results)
			{
				++ranking;
				System.out.println(result.getObjectVector().getObject()
						.toString());

				
				 * if (flagConfig.boundvectorfile().isEmpty() &&
				 * flagConfig.elementalvectorfile().isEmpty()) {
				 * PsiUtils.printNearestPredicate(flagConfig); }
				 
			}

			
			 * if (!flagConfig.jsonfile().isEmpty()) {
			 * PathFinder.pathfinderWriterWrapper(flagConfig, results); }
			 
		}
		else
		{
			System.out.println("No search output.\n");
		}*/
	}
	
	public static void GenerateSimilarityMatrix()
	{
		Enumeration<ObjectVector> vi = termTermIndexer.getSemanticTermVectors()
				.getAllVectors();
		int size = termTermIndexer.getSemanticTermVectors().getNumVectors();
		
		// TODO optimize this
		double[][] matrix = new double[size][size];
		for(int i = 0 ; i < size ; ++i)
		{
			ObjectVector m = vi.nextElement();
			Enumeration<ObjectVector> vj = termTermIndexer.getSemanticTermVectors()
					.getAllVectors();
			for(int j = 0 ; j < size ; ++j)
			{
				ObjectVector n = vj.nextElement();
				matrix[i][j] = m.getVector().measureOverlap(n.getVector());
			}
		}
	}

	public static void tryStuff()
	{
		Enumeration<ObjectVector> v = termTermIndexer.getSemanticTermVectors()
				.getAllVectors();
		System.out.println(termTermIndexer.getSemanticTermVectors().getVector(
				"methodological"));
		
		System.out.println(v.nextElement().getObject()); // this gets the tag value
		System.out.println(v.nextElement().getVector()); // this gets the vector
	}
}
