package sample.randomindexing;

import pitt.search.lucene.IndexFilePositions;

public class DataIndexer
{
    public void IndexDirectory()
    {
        String[] indexArgs = new String[1];
        indexArgs[0] = "C:/Users/amit/Desktop/test/pubmed_result_001.txt";

        IndexFilePositions.main(indexArgs);
    }
}
