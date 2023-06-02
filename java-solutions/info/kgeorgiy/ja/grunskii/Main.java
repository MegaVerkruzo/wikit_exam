package info.kgeorgiy.ja.grunskii;

import java.nio.file.Path;
import java.nio.file.Paths;

// :NOTE: - Lol, kek (not implemented)
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Path path = Paths.get("lol/in.txt");
        System.out.println(path.getFileName().toString());
    }
}
