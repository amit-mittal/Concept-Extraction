package sample.randomindexing;

import pitt.search.lucene.IndexFilePositions;
import sample.util.Constants;

public class DataIndexer
{
    public void indexDirectory()
    {
        String[] indexArgs = new String[1];
        indexArgs[0] = Constants.MEDLINE_ABSTRACTS_FOLDER_PATH;

        IndexFilePositions.main(indexArgs);
    }
}
