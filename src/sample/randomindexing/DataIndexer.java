package sample.randomindexing;

import pitt.search.lucene.IndexFilePositions;

public class DataIndexer
{
    public void indexDirectory()
    {
        String[] indexArgs = new String[1];
        indexArgs[0] = "C:/Users/amit/Desktop/b/pubmed_result_001.txt";

        IndexFilePositions.main(indexArgs);
    }
}
