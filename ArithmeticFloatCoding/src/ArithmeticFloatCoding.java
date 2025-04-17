import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.io.*;

    public class ArithmeticFloatCoding {
    BigDecimal CompressionCode;
    int TotalCharacters;
    BigDecimal Lower;
    BigDecimal Upper;

    public ArithmeticFloatCoding() {
        CompressionCode = BigDecimal.ZERO;
        TotalCharacters = 0;
        Lower = BigDecimal.ZERO;
        Upper = BigDecimal.ONE;
    }

    public void FrequencyInLine(String Line, Map<Character, Integer> charFrequency) {
        for (int i = 0; i < Line.length(); i++) {
            char Key = Line.charAt(i);
            charFrequency.put(Key, charFrequency.getOrDefault(Key, 0) + 1);
        }
    }

    public Map<Character, Integer> GetFrequency(String FileName) {
        Map<Character, Integer> charFrequency = new HashMap<>();
        try {
            File file = new File(FileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                FrequencyInLine(line, charFrequency);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return charFrequency;
    }

    private int GetTotalCharactersNumbers(Map<Character, Integer> charFrequency) {
        return charFrequency.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<Character, BigDecimal> GetProbability(String FileName) {
        Map<Character, Integer> Freq = GetFrequency(FileName);
        Map<Character, BigDecimal> Probability = new HashMap<>();
        TotalCharacters = GetTotalCharactersNumbers(Freq);
        for (Map.Entry<Character, Integer> entry : Freq.entrySet()) {
            BigDecimal ProbVal = BigDecimal.valueOf(entry.getValue()).divide(BigDecimal.valueOf(TotalCharacters), 15, RoundingMode.HALF_UP);
            Probability.put(entry.getKey(), ProbVal);
        }
        return Probability;
    }

    public Map<Character, Symbol> BuildCumulativeProbability(Map<Character, BigDecimal> Probability) {
        Map<Character, Symbol> CumulativeProb = new HashMap<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        for (Map.Entry<Character, BigDecimal> entry : Probability.entrySet()) {
            CumulativeProb.put(entry.getKey(), new Symbol(cumulative, cumulative.add(entry.getValue())));
            cumulative = cumulative.add(entry.getValue());
        }
        return CumulativeProb;
    }

    public void Compress(String Line, Map<Character, Symbol> CumulativeProbability) {
        for (char c : Line.toCharArray()) {
            BigDecimal Range = Upper.subtract(Lower);
            Symbol symbol = CumulativeProbability.get(c);
            Upper = Lower.add(Range.multiply(symbol.UpperRange));
            Lower = Lower.add(Range.multiply(symbol.LowerRange));
        }
    }

    public void CompressFromFile(String FileName, String CompressedFileName) {
        try {
            File file = new File(FileName);
            Map<Character, BigDecimal> Prob = GetProbability(FileName);
            Map<Character, Symbol> CumulativeProbability = BuildCumulativeProbability(Prob);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Compress(line, CumulativeProbability);
            }
            CompressionCode = Lower.add(Upper).divide(BigDecimal.valueOf(2), 15, RoundingMode.HALF_UP);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            File OutputFile = new File(CompressedFileName);
            FileWriter fileWriter = new FileWriter(OutputFile);
            fileWriter.write("Total Characters : " + TotalCharacters + '\n');
            fileWriter.write("CompressionCode : " + CompressionCode + '\n');
            Map<Character, BigDecimal> Probability = GetProbability(FileName);
            for (Map.Entry<Character, BigDecimal> entry : Probability.entrySet()) {
                fileWriter.write(entry.getKey() + " : " + entry.getValue() + '\n');
            }
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Map<Character, BigDecimal> GetProbabilityFromCompressedFile(String FileName) {
        Map<Character, BigDecimal> Prob = new HashMap<>();
        try {
            File file = new File(FileName);
            Scanner scanner = new Scanner(file);
            int counter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (counter < 2) {
                    counter++;
                } else {
                    String[] parts = line.split(" : ");
                    Prob.put(parts[0].charAt(0), new BigDecimal(parts[1]));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return Prob;
    }

    private char GetCharUsingCode(BigDecimal Code, Map<Character, Symbol> CumulativeProb) {
        for (Map.Entry<Character, Symbol> entry : CumulativeProb.entrySet()) {
            if (Code.compareTo(entry.getValue().UpperRange) <= 0 && Code.compareTo(entry.getValue().LowerRange) >= 0) {
                return entry.getKey();
            }
        }
        return '\n';
    }

    public void DecompressFormFile(String FileName, String DecompressedFileName) {
        Map<Character, BigDecimal> prob = GetProbabilityFromCompressedFile(FileName);
        if (prob == null || prob.isEmpty()) {
            throw new IllegalStateException("Error: Probabilities could not be retrieved from the compressed file.");
        }
        Map<Character, Symbol> CumulativePro = BuildCumulativeProbability(prob);

        try (Scanner scanner = new Scanner(new File(FileName))) {
            if (!scanner.hasNextLine())
                throw new IllegalArgumentException("Invalid compressed file: Missing data.");
            TotalCharacters = Integer.parseInt(scanner.nextLine().split(": ")[1]);

            if (!scanner.hasNextLine())
                throw new IllegalArgumentException("Invalid compressed file: Missing compression code.");
            CompressionCode = new BigDecimal(scanner.nextLine().split(": ")[1]);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Compressed file not found: " + FileName, e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading compressed file: " + e.getMessage(), e);
        }

        try (FileWriter fileWriter = new FileWriter(new File(DecompressedFileName))) {
            Lower = BigDecimal.ZERO;
            Upper = BigDecimal.ONE;

            for (int i = 0; i < TotalCharacters; i++) {
                BigDecimal Range = Upper.subtract(Lower);
                BigDecimal Code = CompressionCode.subtract(Lower).divide(Range, 100, RoundingMode.HALF_UP);
                char Char = GetCharUsingCode(Code, CumulativePro);
                if (Char == '\0') {
                    throw new IllegalStateException("Decompression error: Unable to decode character at position " + i);
                }
                Symbol symbol = CumulativePro.get(Char);
                Upper = Lower.add(Range.multiply(symbol.UpperRange));
                Lower = Lower.add(Range.multiply(symbol.LowerRange));

                fileWriter.write(Char);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error writing decompressed file: " + e.getMessage(), e);
        }
    }
}