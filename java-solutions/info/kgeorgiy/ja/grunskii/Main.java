package info.kgeorgiy.ja.grunskii;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class allows to launch memorize from console
 */
public class Main {
    private static void incorrectInput() {
        System.err.println("Incorrect input, example of correct arguments without brackets: \"input.txt [en, ru]\"");
    }

    /**
     * This method accepts name of InputFile, locale and allows to memorize words
     *
     * @param args contains name of InputFile
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            incorrectInput();
            throw new RuntimeException();
        }
        final Path file = Paths.get(args[0]);
        Locale locale;
        try {
            locale = new Locale.Builder().setLanguage(args[1]).build();
        } catch (IllformedLocaleException e) {
            locale = null;
        }

        if (!Files.exists(file) || locale == null) {
            incorrectInput();
            throw new RuntimeException();
        }
        {
            String localeString = args[1].toLowerCase();
            if (!localeString.equals("ru") && !localeString.equals("en")) {
                System.err.println("This locale don't exist in Application, your default locale will be 'en'");
            }
        }
        LeitnerSystem leitnerSystem = new LeitnerSystem(file, locale);
        leitnerSystem.run();
    }
}
