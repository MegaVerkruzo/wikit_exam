package info.kgeorgiy.ja.grunskii;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * This class allows to send quires to wikipedia and getting answers
 */
public class Wikit implements Runnable {
    private static final String LAUNCH_ERROR_MESSAGE = """
            Incorrect Program arguments
            Usage: Wikit [Wikipedia language] [Interface language]
                    
            Wikipedia language - Language code for the content you want to search on Wikipedia (e.g., 'en' for English, 'ru' for Russian, etc.)
            Interface language - Language code for the output messages in the console, available only [ru, en]
            """;

    private final String prefixQuire;
    private final ResourceBundle bundle;

    /**
     * This method launch class
     *
     * @param args contains locale_for_wikipedia - any locale, locale_for_user - [ru, en]
     */
    public static void main(final String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println(LAUNCH_ERROR_MESSAGE);
            return;
        }

        try {
            new Wikit(getLocale(args[0]), getLocale(args[1])).run();
        } catch (final IllformedLocaleException e) {
            System.err.println(LAUNCH_ERROR_MESSAGE);
        }
    }

    private static Locale getLocale(String language) throws IllformedLocaleException {
        return new Locale.Builder().setLanguage(language).build();
    }

    /**
     * It's constructor of this class
     *
     * @param wiki is locale of wikipedia page
     * @param userLocale is locale of user interface
     */
    public Wikit(final Locale wiki, final Locale userLocale) {
        this.bundle = ResourceBundle.getBundle("WikitBundle", userLocale);
        this.prefixQuire = "https://" + wiki.getLanguage() + ".wikipedia.org/w/index.php?";
    }

    private void printTranslatedText(String textName) {
        System.out.println(bundle.getString(textName));
    }

    private void printTranslatedErrorText(String textName) {
        System.err.println(bundle.getString(textName));
    }

    /**
     * This method runs console application
     */
    @Override
    public void run() {
        printTranslatedText("Help");
        while (true) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            final String query;
            try {
                query = reader.readLine().trim();
                final List<String> arguments = List.of(query.split(" ", 2));
                if (arguments.size() != 2) {
                    printTranslatedErrorText("");
                    printTranslatedText("Help");
                    System.err.println("Wrong amount of arguments, write correct commands!");
                    continue;
                }

                final String command = arguments.get(0);
                final String article = arguments.get(1);
                if (command.equals(bundle.getString("Search"))) {
                    System.out.println(String.join("\n", findArticle(article)));
                } else if (command.equals(bundle.getString("Find"))) {
                } else if (command.equals(bundle.getString("Download"))) {
                    final List<String> lines = findArticle(arguments.get(1));
                    final Path file = Paths.get("files", article);
                    Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> findArticle(final String article) {
        final Document doc;
        try {
            doc = Jsoup.connect(prefixQuire + "search=" + article).get();
        } catch (final IOException e) {
            System.err.println(bundle.getString("Exception_Article"));
            return List.of();
        }

        final Element parent = doc
                .getElementById("bodyContent")
                .getElementById("mw-content-text")
                .getElementsByClass("mw-parser-output")
                .first();

        final List<String> lines = new ArrayList<>();
        for (final Element elem : parent.children()) {
            if (elem.tag().equals(Tag.valueOf("p"))) {
                lines.add(elem.text());
            } else if (elem.tag().equals(Tag.valueOf("h2"))) {
                break;
            }
        }
        return lines;
    }
}
