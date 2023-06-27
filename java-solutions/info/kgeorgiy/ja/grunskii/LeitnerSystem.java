package info.kgeorgiy.ja.grunskii;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

// :NOTE: * State not restored on restart
/**
 * This class has main logic about memorizing words
 */
public class LeitnerSystem {
    private final ResourceBundle bundle;
    private final Locale locale;
    private final Data data;

    /**
     * This constructor make class with necessary data.
     *
     * @param file - is location of dictionary

     */
    public LeitnerSystem(Path file, Locale locale) {
        this.locale = locale;
        data = new Data(file);
        // :NOTE: * Platform-dependent path
        bundle = ResourceBundle.getBundle(Paths.get("info", "kgeorgiy", "ja", "grunskii", "LanguageBundle").toString(), locale);
    }

    public void run() {
        printIntroduction();
        startMemorize();
    }

    private void printIntroduction() {
        System.out.println(bundle.getString("Welcome"));
        System.out.println(bundle.getString("InfoHelp"));
    }

    // :NOTE: * Extra "locale" parameter instead of field
    private String getWord() {
        final String result = data.getRandomWord();
        System.out.println(getMessage("Translate", result));
        return result;
    }

    private void startMemorize() {
        while (true) {
            final String word = getWord();
            final String correctAnswer = data.getCorrectTranslate(word);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String quire;
            try {
                quire = reader.readLine().trim();
            } catch (final IOException e) {
                quire = "";
                // :NOTE: - Not localized
                System.err.println("Can't read from console");
                System.exit(1);
            }
            if (quire.toLowerCase(locale).equals(correctAnswer))  {
                System.out.println(getMessage("CorrectAnswer"));
                data.increaseBasket(word);
            } else if (quire.toLowerCase(locale).equals(bundle.getString("Help"))) {
                writeBaskets(data.getBaskets());
            } else {
                System.out.println(getMessage("WrongAnswer", correctAnswer));
                data.setFirstBasket(word);
            }
        }
    }
    public void writeBaskets(final List<Map<String, String>> baskets) {
        for (int i = 0; i < baskets.size(); ++i) {
            System.out.println(i + 1);
            for (final Map.Entry<String, String> entry : baskets.get(i).entrySet()) {
                System.out.println(entry.getKey() + "|" + entry.getValue());
            }
        }
    }

    private String getMessage(final String patternInBundle, final Object... values) {
        // :NOTE: * MessageFormat not reused
        final MessageFormat messageFormat = new MessageFormat(bundle.getString(patternInBundle), locale);
        return messageFormat.format(values);
    }

}
