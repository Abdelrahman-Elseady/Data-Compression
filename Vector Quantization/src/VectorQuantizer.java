import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.ArrayList;
import java.util.Vector;

public class VectorQuantizer {
    BufferedImage Image;
    int Row;
    int Column;
    public VectorQuantizer(String ImagePath) {
        try {
            File ImageFile = new File(ImagePath);
            Image = ImageIO.read(ImageFile);
            Row = Image.getWidth();
            Column = Image.getHeight();
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }
    }
    public ArrayList<Block> Split(int RowsOfBlock, int ColumnsOfBlock) {
        ArrayList<Block> Blocks = new ArrayList<>();

        for (int i = 0; i < Row; i += RowsOfBlock) {
            for (int j = 0; j < Column; j += ColumnsOfBlock) {
                ArrayList<Integer> pixels = new ArrayList<>();
                for (int k = 0; k < RowsOfBlock; k++) {
                    for (int l = 0; l < ColumnsOfBlock; l++) {
                        int x = i + k;
                        int y = j + l;
                        if (x < Row && y < Column)
                        {
                            int rgb = Image.getRGB(x, y);
                            int R = (rgb >> 16) & 0xFF;
                            int G = (rgb >> 8) & 0xFF;
                            int B = rgb & 0xFF;
                            int grey = (int) (0.21 *R + 0.72* G + 0.07 *B);
                            pixels.add(grey);
                        }
                        else
                            pixels.add(0);
                    }
                }
                Block block = new Block(RowsOfBlock, ColumnsOfBlock);
                block.AddPixels(pixels);
                Blocks.add(block);
            }
        }

        return Blocks;
    }
    public ArrayList<Block> BuildCodebook(ArrayList<Block> Blocks, int numOfBlocks) {
        ArrayList<Block> codebook = new ArrayList<>();
        Queue<ArrayList<Block>> blockQueue = new LinkedList<>();
        blockQueue.add(Blocks);

        while (codebook.size() < numOfBlocks) {
            if (blockQueue.isEmpty()) break;

            ArrayList<Block> currentGroup = blockQueue.poll();
            Block averageBlock = GetAverageOfBlocks(currentGroup);

            ArrayList<Block> Group1 = new ArrayList<>();
            ArrayList<Block> Group2 = new ArrayList<>();

            for (Block block : currentGroup) {
                double Error1 = CalculateMeanSquareError(block, averageBlock, 1);
                double Error2 = CalculateMeanSquareError(block, averageBlock, -1);

                if (Error1 < Error2) {
                    Group1.add(block);
                } else {
                    Group2.add(block);
                }
            }

            if (!Group1.isEmpty()) blockQueue.add(Group1);
            if (!Group2.isEmpty()) blockQueue.add(Group2);

            if (Group1.isEmpty() || Group2.isEmpty()) {
                codebook.add(averageBlock);
            }
        }

        while (!blockQueue.isEmpty() && codebook.size() < numOfBlocks) {
            ArrayList<Block> remainingGroup = blockQueue.poll();
            codebook.add(GetAverageOfBlocks(remainingGroup));
        }

        return codebook;
    }
    public ArrayList<Integer> LabelBlocks(ArrayList<Block> Blocks, ArrayList<Block> Codebook) {
        ArrayList<Integer> labels = new ArrayList<>();

        for (Block block : Blocks) {
            double minError = Double.MAX_VALUE;
            int label = -1;

            for (int i = 0; i < Codebook.size(); i++) {
                Block codebookBlock = Codebook.get(i);
                double error = CalculateMeanSquareError(block, codebookBlock, 0);

                if (error < minError) {
                    minError = error;
                    label = i;
                }
            }
            labels.add(label);
        }

        return labels;
    }
    private double CalculateMeanSquareError(Block block1, Block block2, int offset) {
        double MeanSquareError = 0;
        for (int i = 0; i < block1.row; i++) {
            for (int j = 0; j < block1.column; j++) {
                int value1 = block1.Pixels.get(i).get(j);
                int value2 = block2.Pixels.get(i).get(j) + offset;
                MeanSquareError += Math.pow(value1 - value2, 2);
            }
        }
        return MeanSquareError/2;
    }
    public void WriteCodeBookToFile(ArrayList<Block> Blocks, String fileName) {
        if (Blocks.isEmpty()) return;

        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            for (Block block : Blocks) {
                for (List<Integer> row : block.Pixels) {
                    for (int value : row) {
                        fileWriter.write(value + " ");
                    }
                    fileWriter.write("\n");
                }
                fileWriter.write("\n");
            }
            fileWriter.write("-------------------------------------\n");
        } catch (Exception e) {
            System.out.println("Error writing codebook: " + e.getMessage());
        }
    }
    public void WriteLabelsToFile(ArrayList<Integer> Labels, String fileName, int RowOfBlock, int ColumnOfBlock) {
        double row = Math.ceil((double) Row / RowOfBlock);
        double col = Math.ceil((double) Column / ColumnOfBlock);

        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            int index = 0;
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    fileWriter.write(Labels.get(index++) + " ");
                }
                fileWriter.write("\n");
            }
        } catch (Exception e) {
            System.out.println("Error writing labels: " + e.getMessage());
        }
    }

    public void Compress(String FileName, int RowOfBlock, int ColumnOfBlock, int BlockNumInCodeBook) {
        try (FileWriter fileWriter = new FileWriter(FileName)) {

            fileWriter.write(Row + " " + Column + "\n");
            fileWriter.write(RowOfBlock + " " + ColumnOfBlock + "\n");
            fileWriter.write(BlockNumInCodeBook + "\n");
        } catch (Exception e) {
            System.out.println("Error writing metadata: " + e.getMessage());
        }

        ArrayList<Block> Blocks = Split(RowOfBlock, ColumnOfBlock);
        ArrayList<Block> CodeBook = BuildCodebook(Blocks, BlockNumInCodeBook);
        WriteCodeBookToFile(CodeBook, FileName);
        ArrayList<Integer> Labels = LabelBlocks(Blocks, CodeBook);
        WriteLabelsToFile(Labels, FileName, RowOfBlock, ColumnOfBlock);
    }
    public int[] ReadData(String FileName) {
        int[] metadata = new int[5];
        try (Scanner scanner = new Scanner(new File(FileName))) {
            metadata[0] = scanner.nextInt();
            metadata[1] = scanner.nextInt();
            metadata[2] = scanner.nextInt();
            metadata[3] = scanner.nextInt();
            metadata[4] = scanner.nextInt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return metadata;
    }
    public ArrayList<Block> ReadCodeBookFromFile(String FileName, int RowOfBlock, int ColumnOfBlock, int BlockNumInCodeBook) {
        ArrayList<Block> CodeBook = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(FileName))) {
            // Skip metadata
            for (int i = 0; i < 3; i++) {
                scanner.nextLine();
            }

            // Read codebook blocks
            for (int k = 0; k < BlockNumInCodeBook; k++) {
                Block block = new Block(RowOfBlock, ColumnOfBlock);
                ArrayList<Integer> pixels = new ArrayList<>();
                for (int i = 0; i < RowOfBlock; i++) {
                    for (int j = 0; j < ColumnOfBlock; j++) {
                        pixels.add(scanner.nextInt());
                    }
                }
                block.AddPixels(pixels);
                CodeBook.add(block);

                // Skip the separator line if present
                if (scanner.hasNextLine()) scanner.nextLine();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return CodeBook;
    }
    public ArrayList<Integer> ReadLabelsFromFile(String FileName) {
        ArrayList<Integer> Labels = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(FileName))) {
            // Skip metadata and codebook
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("-------------------------------------")) break;
            }

            // Read labels
            while (scanner.hasNextInt()) {
                Labels.add(scanner.nextInt());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Labels;
    }
    public Block GetAverageOfBlocks(ArrayList<Block> Blocks) {
        if (Blocks.isEmpty()) return null;
        int row = Blocks.get(0).row;
        int col = Blocks.get(0).column;
        Block averageBlock = new Block(row, col);
        ArrayList<Integer> averagePixels = new ArrayList<>(Collections.nCopies(row * col, 0));
        averageBlock.AddPixels(averagePixels);
        for (Block block : Blocks) {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    int currentValue = averageBlock.Pixels.get(i).get(j);
                    int newValue = currentValue + block.Pixels.get(i).get(j);
                    averageBlock.Pixels.get(i).set(j, newValue);
                }
            }
        }

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int sumValue = averageBlock.Pixels.get(i).get(j);
                averageBlock.Pixels.get(i).set(j, sumValue / Blocks.size());
            }
        }

        return averageBlock;
    }
    public void Decompress(String FileName, String OutputImagePath) {
        try {
            int[] Data = ReadData(FileName);
            Row = Data[0];
            Column = Data[1];
            int RowOfBlock = Data[2];
            int ColumnOfBlock = Data[3];
            int BlockNumInCodeBook = Data[4];

            ArrayList<Block> CodeBook = ReadCodeBookFromFile(FileName, RowOfBlock, ColumnOfBlock, BlockNumInCodeBook);
            ArrayList<Integer> Labels = ReadLabelsFromFile(FileName);

            BufferedImage reconstructedImage = new BufferedImage(Row, Column, BufferedImage.TYPE_INT_RGB);

            int blockIndex = 0;
            for (int i = 0; i < Row; i += RowOfBlock)
            {
                for (int j = 0; j < Column; j += ColumnOfBlock)
                {
                    Block codebookBlock = CodeBook.get(Labels.get(blockIndex++));
                    int blockRow = 0, blockCol = 0;
                    for (int x = i; x < i + RowOfBlock && x < Row; x++)
                    {
                        for (int y = j; y < j + ColumnOfBlock && y < Column; y++)
                        {
                            int gray = codebookBlock.Pixels.get(blockRow).get(blockCol++);
                            int rgb = (gray << 16) | (gray << 8) | gray;
                            reconstructedImage.setRGB(x, y, rgb);
                        }
                        blockRow++;
                        blockCol = 0;
                    }
                }
            }

            File outputFile = new File(OutputImagePath);
            ImageIO.write(reconstructedImage, "png", outputFile);
        } catch (Exception e) {
            System.out.println("Error during decompression: " + e.getMessage());
        }
    }
}
