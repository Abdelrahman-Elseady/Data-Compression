import  java.io.*;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

class Lzw {
    private List<Integer> compressed;
    private List<String> dictionary;
    private void SetForNewLine()
    {
        dictionary = new ArrayList<>();
        compressed = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            dictionary.add(String.valueOf((char) i));
        }
    }
    public String compress(String Line) {
        String CompressedLine = "";
        String currentWord = "";
        for (int i = 0; i < Line.length(); i++) {
            char nextChar = Line.charAt(i);
            String combinedWord = currentWord + nextChar;

            if (dictionary.contains(combinedWord))
            {
                currentWord = combinedWord;
            }
            else
            {
                compressed.add(dictionary.indexOf(currentWord));
                CompressedLine =CompressedLine + dictionary.indexOf(currentWord)+' ';
                dictionary.add(combinedWord);
                currentWord = String.valueOf(nextChar);
            }
        }

        if (!currentWord.isEmpty()) {
            compressed.add(dictionary.indexOf(currentWord));
            CompressedLine =CompressedLine + dictionary.indexOf(currentWord)+' ';
        }
        return CompressedLine;
    }
    public String deCompress(String CompressedLine)
    {
        StringBuilder decompressedLine = new StringBuilder();
        String[] arr = CompressedLine.trim().split(" ");
        for (String s : arr) {
            compressed.add(Integer.parseInt(s));
        }
        if (compressed.isEmpty())
        {
            return "";
        }
        String previousWord = dictionary.get(compressed.get(0));
        decompressedLine.append(previousWord);

        for (int i = 1; i < compressed.size(); i++) {
            int code = compressed.get(i);
            String currentWord;

            if (code < dictionary.size()) {
                currentWord = dictionary.get(code);
            } else {
                currentWord = previousWord + previousWord.charAt(0);
            }
            decompressedLine.append(currentWord);
            dictionary.add(previousWord + currentWord.charAt(0));
            previousWord = currentWord;
        }

        return decompressedLine.toString();
    }

    public  void DeCompressFromFile(String FileName,String OutputFile)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(FileName))) {
            String Line;
            String DeCompressedLine;
            while ((Line = reader.readLine()) != null)
            {
                SetForNewLine();
                DeCompressedLine = "";
                DeCompressedLine = deCompress(Line);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(OutputFile, true)))
                {
                    writer.write(DeCompressedLine);
                    writer.newLine();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CompressFromFile(String FileName,String OutputFile)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(FileName))) {
            String Line;
            String CompressedLine;
            while ((Line = reader.readLine()) != null)
            {
                SetForNewLine();
                CompressedLine = "";
                CompressedLine = compress(Line);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(OutputFile, true)))
                {
                    writer.write(CompressedLine);
                    writer.newLine();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printCompressed() {
        System.out.println("Compressed Output:");
        for (int code : compressed) {
            System.out.print(code + " ");
        }
        System.out.println();
    }

    public void printDictionary() {
        System.out.println("Dictionary Contents:");
        for (int i = 128; i < dictionary.size(); i++) {
            System.out.println(i + ": " + dictionary.get(i));
        }
    }
}

public class Main {
    public static void main(String[] args)
    {
        Lzw lzw = new Lzw();
        lzw.CompressFromFile("InputFile.txt","CompressedFile.txt");
        lzw.DeCompressFromFile("CompressedFile.txt","DecompressedFile.txt");
    }
}