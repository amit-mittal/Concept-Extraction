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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import pitt.search.lucene.*;
import pitt.search.semanticvectors.*;
import pitt.search.semanticvectors.DocVectors.DocIndexingStrategy;

public class RandomIndexer 
{
	static Path INDEX_DIR = FileSystems.getDefault().getPath("positional_index");
	static VectorStore newElementalTermVectors = null;

	  public static String usageMessage =
	    "BuildPositionalIndex class in package pitt.search.semanticvectors"
	    + "\nUsage: java pitt.search.semanticvectors.BuildPositionalIndex -luceneindexpath PATH_TO_LUCENE_INDEX"
	    + "\nBuildPositionalIndex creates file termtermvectors.bin in local directory."
	    + "\nOther parameters that can be changed include"
	    + "\n    windowlength (size of sliding context window),"
	    + "\n    dimension (number of dimensions), vectortype (real, complex, binary)"
	    + "\n    seedlength (number of non-zero entries in basic vectors),"
	    + "\n    minimum term frequency.\n"
	    + "\nTo change these use the command line arguments "
	    + "\n  -vectortype [real, complex, or binary]"
	    + "\n  -dimension [number of dimensions]"
	    + "\n  -seedlength [seed length]"
	    + "\n  -minfrequency [minimum term frequency]"
	    + "\n  -initialtermvectors [name of preexisting vectorstore for term vectors]"
	    + "\n  -windowradius [window size]"
	    + "\n  -positionalmethod [positional indexing method: basic (default), directional (HAL), permutation (Sahlgren 2008)";

	/** Index all text files under a directory. */
	public static void main(String[] args) 
	{
		// Allow for the specification of a directory to write the index to.
		/*INDEX_DIR = FileSystems.getDefault().getPath("C:/Users/amit/Desktop/b");

		if (Files.exists(INDEX_DIR)) 
		{
			throw new IllegalArgumentException(
					"Cannot save index to '" + INDEX_DIR + "' directory, please delete it first");
		}

		try 
		{
			IndexWriter writer;
			// Create IndexWriter using porter stemmer or no stemming. No stopword list.
			//Analyzer analyzer = flagConfig.porterstemmer()
			// ? new PorterAnalyzer() : new StandardAnalyzer(null);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			IndexWriterConfig writerConfig = new IndexWriterConfig(null, analyzer);
			writer = new IndexWriter(FSDirectory.open(INDEX_DIR.toFile()), writerConfig);

			final File docDir = new File("C:/Users/amit/Desktop/a/pubmed_result_001.txt");
			if (!docDir.exists() || !docDir.canRead()) 
			{
				writer.close();
				throw new IOException ("Document directory '" + docDir.getAbsolutePath() +
						"' does not exist or is not readable, please check the path");
			}

			Date start = new Date();

			System.out.println("Indexing to directory '" +INDEX_DIR+ "'...");
			indexDocs(writer, docDir);
			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} 
		catch (IOException e) 
		{
			System.out.println(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}*/
		
		BuildPositionalIndex();
	}

	static void indexDocs(IndexWriter writer, File file) throws IOException 
	{
		// Do not try to index files that cannot be read.
		if (file.canRead()) 
		{
			if (file.isDirectory()) 
			{
				String[] files = file.list();
				// An IO error could occur.
				if (files != null) 
				{
					for (int i = 0; i < files.length; i++) 
					{
						// Skip dot files.
						if (!files[i].startsWith(".")) 
						{
							indexDocs(writer, new File(file, files[i]));
						}
					}
				}
			} 
			else 
			{
				System.out.println("adding " + file);
				try 
				{
					// Use FilePositionDoc rather than FileDoc such that term
					// positions are indexed also.
					writer.addDocument(FilePositionDoc.Document(file));
				}
				// At least on windows, some temporary files raise this
				// exception with an "access denied" message. Checking if the
				// file can be read doesn't help
				catch (FileNotFoundException fnfe) 
				{
					fnfe.printStackTrace();
				}
			}
		}
	}
	
	static void BuildPositionalIndex() throws IllegalArgumentException
	{
		String[] args = new String[16];
		args[0] = "-vectortype";
		args[1] = "real";
		args[2] = "-dimension";
		args[3] = "1000";
		args[4] = "-seedlength";
		args[5] = "5";
		args[6] = "-minfrequency";
		args[7] = "10";
		args[8] = "-windowradius";
		args[9] = "1";
		args[10] = "-positionalmethod";
		args[11] = "directional";
		args[12] = "-luceneindexpath";
		args[13] = "C:/Users/amit/Desktop/b";
		args[14] = "-maxfrequency";
		args[15] = "12";
		
		
		FlagConfig flagConfig;
	    try {
	      flagConfig = FlagConfig.getFlagConfig(args);
	      args = flagConfig.remainingArgs;
	    } catch (IllegalArgumentException e) {
	      System.out.println(usageMessage);
	      throw e;
	    }

	    /*if (flagConfig.luceneindexpath().isEmpty()) {
	      throw (new IllegalArgumentException("-luceneindexpath must be set."));
	    }*/
	    String luceneIndex = "C:/Users/amit/Desktop/b";
	    
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
//	        + ", Fields to index: " + Arrays.toString(flagConfig.contentsfields())
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
	      // TODO: Understand if this is an appropriate requirement, and whether
	      //       the user should be alerted of any potential consequences.
	      if (flagConfig.docindexing() != DocIndexingStrategy.NONE) {
	        IncrementalDocVectors.createIncrementalDocVectors(
	            termTermIndexer.getSemanticTermVectors(), flagConfig, new LuceneUtils(flagConfig));
	      }
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	}
}
