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

import banner.BannerProperties.TextDirection;
import banner.Sentence;
import banner.tagging.CRFTagger;
import banner.tagging.TaggedToken.TagFormat;

public class ConceptTagger
{
    public static List<Sentence> sentences = new ArrayList<Sentence>();
    public HashMap<Integer, List<Triplet<Integer, Integer, String>>> conceptMap;

    public void ParseConceptFile(String dirName, String fileName)
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

    public void ParseTextFile(String dirName, String fileName)
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
                    {
                        newString.append("O");
                    }

                    newString.append(" ");
                }

                ++lineNo;
                newString.append("\n");
            }

            PrintWriter out = new PrintWriter(
                    "C:/Users/amit/Desktop/newconcept/" + fileName);
            out.println(newString.toString());
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void ConvertTextToSentences(String dirName, String fileName)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(dirName
                + fileName)))
        {
            String currLine;
            while ((currLine = br.readLine()) != null)
            {
                Sentence s = Sentence.loadFromPiped(null, currLine);
                sentences.add(s);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        File conceptDir = new File("C:/Users/amit/Desktop/concept/");
        File txtDir = new File("C:/Users/amit/Desktop/txt/");
        File newTxtDir = new File("C:/Users/amit/Desktop/newconcept/");
        File[] allConceptFiles = conceptDir.listFiles();

        System.out.println("Converting data into MALLET format");
        ConceptTagger c = new ConceptTagger();
        for (int i = 0; i < allConceptFiles.length; ++i)
        {
            c.ParseConceptFile(conceptDir.getAbsolutePath() + "/",
                    allConceptFiles[i].getName());
            c.ParseTextFile(txtDir.getAbsolutePath() + "/",
                    txtDir.listFiles()[i].getName());
            c.ConvertTextToSentences(newTxtDir.getAbsolutePath() + "/",
                    newTxtDir.listFiles()[i].getName());
        }
        System.out.println("Converting data into MALLET format...done");

        CRFTagger crf = CRFTagger.train(sentences, 1, false, TagFormat.IOB,
                TextDirection.Forward, null, null, false);

        crf.write(new File("C:/Users/amit/Desktop/CRF.txt"));
    }
}
