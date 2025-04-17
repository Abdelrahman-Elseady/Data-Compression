import java.io.*;
import java.util.*;


class Node {
    String character;
    int frequency;
    Node left, right;


    public Node(String character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }
}


class HuffmanTree {
    Node root;

    public void buildTree(Map<String, Integer> charFrequency) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(n -> n.frequency));

        for (Map.Entry<String, Integer> entry : charFrequency.entrySet()) {
            priorityQueue.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (priorityQueue.size() > 1) {
            Node left = priorityQueue.poll();
            Node right = priorityQueue.poll();


            Node parent = new Node(null, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;

            priorityQueue.add(parent);
        }

        // The last remaining node is the root
        root = priorityQueue.poll();
    }

    public void FrequencyInLine(String Line,Map<String, Integer> charFrequency) {
        for (int i = 0; i < Line.length(); i++) {
            String Key = String.valueOf(Line.charAt(i));
            if (charFrequency.containsKey(Key)) {
                charFrequency.put(Key, charFrequency.get(Key) + 1);
            }
            else
                charFrequency.put(Key, 1);
        }
    }
    public Map<String,Integer> GetFrequency(String FileName) {
        Map<String, Integer> charFrequency = new HashMap<>();
        try{
            File file = new File(FileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                FrequencyInLine(line,charFrequency);
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return charFrequency;
    }

    private void generateCodes(Node node, String code, Map<String, String> huffmanCodes) {
        if (node == null) return;

        if (node.character != null) {
            huffmanCodes.put(node.character, code);
        }

        generateCodes(node.left, code + "1", huffmanCodes);
        generateCodes(node.right, code + "0", huffmanCodes);
    }
    public Map<String, String> generateCodes() {
        Map<String, String> huffmanCodes = new HashMap<>();
        generateCodes(root, "", huffmanCodes);
        return huffmanCodes;
    }

    public void WriteCodesToFile(String FileName) {
        Map<String, String> huffmanCodes = generateCodes();
        try (Writer writer = new FileWriter(FileName)) {
            for (Map.Entry<String, String> entry : huffmanCodes.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }
            writer.write("----------\n"); // Add separator
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    private String Compress(String Line, Map<String, String> huffmanCodes) {
        StringBuilder CompressedLine = new StringBuilder();
        for (int i = 0; i < Line.length(); i++) {
            CompressedLine.append(huffmanCodes.get(String.valueOf(Line.charAt(i))));
        }
        return CompressedLine.toString();
    }
    public BitSet getBitSet(String line) {
        BitSet bitSet = new BitSet(line.length());
        int bitcounter = 0;
        for (Character c : line.toCharArray()) {
            if (c.equals('1')) {
                bitSet.set(bitcounter);
            }
            bitcounter++;
        }
        return bitSet;
    }

    public void CompressFromFile(String FileName, String OutputFile) {
        Map<String, Integer>charFrequency  = GetFrequency(FileName);
        buildTree(charFrequency);
        WriteCodesToFile(OutputFile);
        try {
            File file = new File(FileName);
            FileWriter writer = new FileWriter(OutputFile, true);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = Compress(line, generateCodes());
                byte[] byteArray = getBitSet(line).toByteArray();
                for (byte b : byteArray) {
                    writer.write(b);
                }
                writer.write("\n");
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private String Decompress(String Line, Map<String, String> huffmanCodes) {
        StringBuilder DecompressedLine = new StringBuilder();
        StringBuilder Code = new StringBuilder();
        for (int i = 0; i < Line.length(); i++)
        {
            Code.append(Line.charAt(i));
            if (huffmanCodes.containsKey(Code.toString()))
            {
                DecompressedLine.append(huffmanCodes.get(Code.toString()));
                Code = new StringBuilder();
            }
        }
        return DecompressedLine.toString();
    }
    Map<String, String> GetOverHeadFromFile(String FileName) {
        Map<String, String> huffmanCodes = new HashMap<>();
        try {
            File file = new File(FileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("-----"))
                    break;
                // swapped the key and val
                huffmanCodes.put( line.substring(3).trim(),String.valueOf(line.charAt(0)));
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return huffmanCodes;
    }

    private byte[] GetArrayOfByte(String line) {
        byte[] byteArray = new byte[line.length()];
        for (int i = 0; i < line.length(); i++) {
            byteArray[i] = (byte) line.charAt(i);
        }
        return byteArray;
    }
    public void DecompressFromFile(String FileName, String OutputFile) {
        Map<String, String> huffmanCodes = GetOverHeadFromFile(FileName);
        boolean Flag=false;
        try{
            File file = new File(FileName);
            File Output=new File(OutputFile);
            FileWriter fileWriter = new FileWriter(Output);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("-----"))
                {
                    Flag=true;
                }
                else if (Flag)
                {
                    byte []byteArray = GetArrayOfByte(line);
                    BitSet bitSet = BitSet.valueOf(byteArray);
                    StringBuilder binaryString = new StringBuilder();
                    for (int i = 0; i <= bitSet.length(); i++) {
                        if (bitSet.get(i)) {
                            binaryString.append('1');
                        } else {
                            binaryString.append('0');
                        }
                    }
                    fileWriter.write(Decompress(binaryString.toString(), huffmanCodes)+'\n');
                }
            }
            fileWriter.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}

public class Main {

    public static void main(String[] args) {

        HuffmanTree huffmanTree = new HuffmanTree();
        huffmanTree.CompressFromFile("Input.txt", "Compressed.txt");
        huffmanTree.DecompressFromFile( "Compressed.txt","Decompressed.txt");
    }
}