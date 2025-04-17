import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        ArithmeticFloatCoding a=new ArithmeticFloatCoding();
        a.CompressFromFile("InputFile.txt","CompressedFile.txt");
        a.DecompressFormFile("CompressedFile.txt","DecompressedFile.txt");

    }
}
