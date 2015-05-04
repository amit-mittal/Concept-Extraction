package sample.i2b2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javatuples.Triplet;

import dragon.nlp.tool.HeppleTagger;
import dragon.nlp.tool.MedPostTagger;
import dragon.nlp.tool.lemmatiser.EngLemmatiser;
import edu.umass.cs.mallet.base.fst.CRF;
import sample.util.Constants;
import banner.BannerProperties.TextDirection;
import banner.Sentence;
import banner.tagging.CRFTagger;
import banner.tagging.Mention;
import banner.tagging.TaggedToken.TagFormat;

public class ConceptTagger
{
    private static List<Sentence> testSentences = new ArrayList<Sentence>();
    private static List<Sentence> sentences = new ArrayList<Sentence>();
    private HashMap<Integer, List<Triplet<Integer, Integer, String>>> conceptMap;
    
    public static CRFTagger crf;
    
    public void startCRFTraining()
    {
        File newTxtDir = new File(Constants.i2b2_MALLET_TRAINING_FOLDER_PATH);
        File[] allConceptFiles = newTxtDir.listFiles();

        System.out.println("Converting data into sentences");
        ConceptTagger c = new ConceptTagger();
        for (int i = 0; i < allConceptFiles.length; ++i)
        {
            c.ConvertTextToSentences(newTxtDir.getAbsolutePath() + "/",
                    newTxtDir.listFiles()[i].getName());
        }
        System.out.println("Converting data into sentences...done");

        crf = CRFTagger.train(sentences, 1, false, TagFormat.IOB,
                TextDirection.Forward, null, null, true);

        crf.write(new File(Constants.CRF_MODEL_FILE_PATH));
    }

    private void ParseConceptFile(String dirName, String fileName)
    {
        conceptMap = new HashMap<Integer, List<Triplet<Integer, Integer, String>>>();
        try (BufferedReader br = new BufferedReader(new FileReader(dirName
                + fileName)))
        {
            String currLine;
            while ((currLine = br.readLine()) != null)
            {
                String[] arr = currLine.split("\\|+");

                String[] parts0 = arr[0].split("\\s+");
                int totalParts0 = parts0.length;
                String[] start = parts0[totalParts0 - 2].split(":");
                String[] end = parts0[totalParts0 - 1].split(":");

                Integer line = Integer.parseInt(start[0]);
                Integer start_w = Integer.parseInt(start[1]);
                Integer end_w = Integer.parseInt(end[1]);

                String[] parts1 = arr[1].split("=");
                String category = parts1[1]
                        .substring(1, parts1[1].length() - 1);

                List<Triplet<Integer, Integer, String>> list;
                if (conceptMap.containsKey(line))
                {
                    list = conceptMap.get(line);
                }
                else
                {
                    list = new ArrayList<Triplet<Integer, Integer, String>>();
                    conceptMap.put(line, list);
                }

                list.add(new Triplet<Integer, Integer, String>(start_w, end_w,
                        category));
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void ParseTextFile(String dirName, String fileName)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(dirName
                + fileName)))
        {
            StringBuffer newString = new StringBuffer();
            int lineNo = 1;
            String currLine;
            while ((currLine = br.readLine()) != null)
            {
                List<Triplet<Integer, Integer, String>> tags = conceptMap
                        .get(lineNo);
                String[] words = currLine.split("\\s+");

                for (int i = 0; i < words.length; ++i)
                {
                    boolean found = false;
                    newString.append(words[i]);
                    newString.append("|");

                    if (tags != null)
                    {
                        for (Triplet<Integer, Integer, String> tag : tags)
                        {
                            if (tag.getValue0() == i)
                            {
                                found = true;
                                newString.append("B-" + tag.getValue2());
                                break;
                            }
                            else if (tag.getValue0() < i
                                    && tag.getValue1() >= i)
                            {
                                found = true;
                                newString.append("I-" + tag.getValue2());
                                break;
                            }
                        }
                    }

                    if (!found)
                        newString.append("O");

                    newString.append(" ");
                }

                ++lineNo;
                newString.append("\n");
            }

            PrintWriter out = new PrintWriter(
                    Constants.i2b2_MALLET_TRAINING_FOLDER_PATH + fileName);
            out.println(newString.toString().trim());
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void ConvertTextToSentences(String dirName, String fileName)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(dirName
                + fileName)))
        {
            String currLine;
            while ((currLine = br.readLine()) != null)
            {
                currLine = currLine.trim();
                if(currLine.length() == 0)
                    continue;
                Sentence s = Sentence.loadFromPiped(null, currLine);
                sentences.add(s);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void ConvertTestTextToSentences(String dirName, String fileName)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(dirName
                + fileName)))
        {
            String currLine;
            while ((currLine = br.readLine()) != null)
            {
                currLine = currLine.trim();
                if(currLine.length() == 0)
                    continue;
                Sentence s = Sentence.loadFromPiped(null, currLine);
                testSentences.add(s);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void tagSentence() throws IOException
    {
        Sentence s = Sentence.loadFromPiped(null, "There|O was|O no|O appreciable|B-problem cervical|I-problem ,|I-problem supraclavicular|I-problem ,|I-problem axillary|I-problem ,|I-problem or|I-problem inguinal|I-problem adenopathy|I-problem .|O ");
        CRFTagger t = CRFTagger.load(new File(Constants.CRF_MODEL_FILE_PATH), null, null);
        t.tag(s);
        List<Mention> m = s.getMentions();
        for (Mention mention : m)
        {
            System.out.println(mention);
        }
    }
    
    private static void FillTestSentencesList() throws IOException
    {
        File newTxtDir = new File(Constants.i2b2_MALLET_TEST_FOLDER_PATH);
        File[] allConceptFiles = newTxtDir.listFiles();

        System.out.println("Converting test data into MALLET format");
        ConceptTagger c = new ConceptTagger();
        for (int i = 0; i < allConceptFiles.length; ++i)
        {
            c.ConvertTestTextToSentences(newTxtDir.getAbsolutePath() + "/",
                    newTxtDir.listFiles()[i].getName());
        }
        System.out.println("Converting test data into MALLET format...done");
    }
    
    public static void main(String[] args) throws IOException
    {
        File conceptDir = new File(Constants.i2b2_TAGS_TRAINING_FOLDER_PATH);
        File txtDir = new File(Constants.i2b2_DATA_TRAINING_FOLDER_PATH);
        File newTxtDir = new File(Constants.i2b2_MALLET_TRAINING_FOLDER_PATH);
        File[] allConceptFiles = conceptDir.listFiles();

        System.out.println("Converting data into MALLET format");
        ConceptTagger c = new ConceptTagger();
        for (int i = 0; i < allConceptFiles.length; ++i)
        {
//            c.ParseConceptFile(conceptDir.getAbsolutePath() + "/",
//                    allConceptFiles[i].getName());
//            c.ParseTextFile(txtDir.getAbsolutePath() + "/",
//                    txtDir.listFiles()[i].getName());
            c.ConvertTextToSentences(newTxtDir.getAbsolutePath() + "/",
                    newTxtDir.listFiles()[i].getName());
        }
        System.out.println("Converting data into MALLET format...done");

        FillTestSentencesList();
        
        // TODO
        // can change order to 1 or 2
        // can change the ratio on which incremental training done
        // change the pipes - change conjunction or features in window
        // implement new pipe - thesaurus, word vector
        // change the POS tagger
        // change if vector boolean or augmented or none
        crf = CRFTagger.train(sentences, testSentences, 1, false, TagFormat.IOB,
                TextDirection.Forward, new EngLemmatiser(), new HeppleTagger(), true);
        
        crf.write(new File(Constants.CRF_MODEL_FILE_PATH));
        
        // CRFTagger crf = CRFTagger.load(new File(Constants.CRF_MODEL_FILE_PATH), null, null);
        // crf.evaluateModel(testSentences);
    }
}
