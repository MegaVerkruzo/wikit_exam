package info.kgeorgiy.ja.grunskii;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

/**
 * This class has main logic about memorizing words
 */
public class LeitnerSystem {
    private ResourceBundle bundle;
    /**
     * This method accepts name of InputFile, locale and allows to memorize words
     *
     * @param args contains name of InputFile
     */
    public static void main(String[] args) {
        // launch
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Incorrect input");
        }
        new LeitnerSystem().run(args[0], args[1]);
    }

    // This thing run
    private void run(String fileNameString, String localeString) {
        Path filePath = Paths.get(fileNameString);
        Locale locale = new Locale.Builder().setLanguage(localeString).build();
        bundle = ResourceBundle.getBundle(Paths.get("info", "kgeorgiy", "ja", "grunskii", "LanguageBundle").toString(), locale);
        Data data = loadData(filePath, locale);
        printIntroduction();
        startMemorize(data, locale);
    }

    // loadData
    private Data loadData(Path filePath, Locale locale) {
        return new Data(filePath, locale);
    }

    private void printIntroduction() {
        System.out.println(bundle.getString("Welcome"));
        System.out.println(bundle.getString("InfoHelp"));
    }

    private String getWord(Data data, Locale locale) {
        String result = data.getRandomWord();
        System.out.println(getMessage(locale, "Translate", result));
        return result;
    }

    private void startMemorize(Data data, Locale locale) {
        while (true) {
            String word = getWord(data, locale);
            String correctAnswer = data.getCorrectTranslate(word);
            final BufferedReader reader =new BufferedReader(new InputStreamReader(System.in));
            String quire;
            try {
                quire = reader.readLine().trim();
            } catch (IOException e) {
                quire = "";
                System.err.println("Can't read from console");
                System.exit(1);
            }
            if (quire.toLowerCase(locale).equals(correctAnswer))  {
                System.out.println(getMessage(locale, "CorrectAnswer"));
                data.increaseBasket(word);
            } else if (quire.toLowerCase(locale).equals(bundle.getString("Help"))) {
                writeBaskets(data.baskets);
            } else {
                System.out.println(getMessage(locale, "WrongAnswer", correctAnswer));
                data.setFirstBasket(word);
            }
        }
    }
    public void writeBaskets(List<Map<String, String>> baskets) {
        for (int i = 0; i < baskets.size(); ++i) {
            System.out.println(i + 1);
            for (Map.Entry<String, String> entry : baskets.get(i).entrySet()) {
                System.out.println(entry.getKey() + "|" + entry.getValue());
            }
        }
    }

    private String getMessage(Locale locale, String patternInBundle, Object... values) {
        MessageFormat messageFormat = new MessageFormat(bundle.getString(patternInBundle), locale);
        return messageFormat.format(values);
    }

}
