import java.io.*;
import java.util.Scanner;

public class Ex1 {

    public static void main(String[] args) {
        try{
            BufferedReader br = new BufferedReader(new FileReader("input.txt"));
            String line=br.readLine();
            Graph_Algo _graph_algo=new Graph_Algo(line);
            PrintStream fileStream = new PrintStream("output.txt");

            while ((line=br.readLine())!=null) {
                fileStream.println(_graph_algo.query(line));
            }
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
