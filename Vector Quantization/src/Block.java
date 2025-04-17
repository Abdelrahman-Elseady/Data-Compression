import java.util.*;

public class Block {
    int row;
    int column;
    ArrayList<List<Integer>> Pixels = new ArrayList<>();
    public Block(int row, int column, ArrayList<List<Integer>> Pixels) {
        this.row = row;
        this.column = column;
        this.Pixels = Pixels;
    }
    public void AddPixels(ArrayList<Integer> arr) {
        int Start=0;
        for (int i=0;i<row;i++)
        {
            Pixels.add(arr.subList(Start,Start+column));
            Start+=column;
        }

    }
    public Block(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
