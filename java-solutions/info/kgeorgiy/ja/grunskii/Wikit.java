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
    private final String prefixQuire;
    private final String host;
    private final Locale wiki;
    private final Locale user;
    private final ResourceBundle bundle;

    /**
     * This method launch class
     *
     * @param args contains locale_for_wikipedia - any locale, locale_for_user - [ru, en]
     */
    public static void main(final String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Incorrect input - example of correct: Wikit [ru, en, uk...] [ru, en]");
            return;
        }

        try {
            final Locale wiki = new Locale.Builder().setLanguage(args[0]).build();
            final Locale user = new Locale.Builder().setLanguage(args[1]).build();

            new Wikit(wiki, user).run();
        } catch (final IllformedLocaleException e) {
            System.err.println("Incorrect input - example of correct: Wikit [ru, en, uk...] [ru, en]");
            return;
        }
    }

    /**
     * It's constructor of this class
     *
     * @param wiki is locale of wikipedia page
     * @param user is locale of user interface
     */
    public Wikit(final Locale wiki, final Locale user) {
        this.wiki = wiki;
        this.host  = wiki.getLanguage() + ".wikipedia.org";
        this.prefixQuire = "https://" + wiki.getLanguage() + ".wikipedia.org/w/index.php?";
        this.user = user;
        this.bundle = ResourceBundle.getBundle("WikitBundle", user);
    }

    /**
     * This method runs console application
     */
    @Override
    public void run() {
        System.out.println(bundle.getString("Help"));
        while (true) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            final String query;
            try {
                query = reader.readLine().trim();
                final List<String> arguments = List.of(query.split(" ", 2));
                if (arguments.size() != 2) {
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
