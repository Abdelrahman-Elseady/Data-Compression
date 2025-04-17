import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args)
    {
        String inputImagePath = "C:\\Users\\DELL\\OneDrive\\Desktop\\New folder\\photographer.bmp";
        String compressedFilePath = "compressed.txt";
        String decompressedImagePath = "output.jpg";

        int blockRowSize = 4;
        int blockColumnSize = 4;
        int codebookSize = 500;

        VectorQuantizer v = new VectorQuantizer(inputImagePath);

        v.Compress(compressedFilePath, blockRowSize, blockColumnSize, codebookSize);

        v.Decompress(compressedFilePath, decompressedImagePath);

    }
}