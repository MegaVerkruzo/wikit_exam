package info.kgeorgiy.ja.grunskii;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Math.*;

/**
 * This class parse *_save.txt files and getting parsed data.
 */
public final class Data {
    private final Random random = new Random();

    protected final List<Map<String, String>> baskets;
    private final Map<String, WordInfo> words = new HashMap<>();

    private record WordInfo(String translated, int basket) {
    }

    private Path getSaveFileName(final Path fileName) {
        final Path parent = fileName.getParent() == null ? Paths.get("") : fileName.getParent();
        return parent.resolve(".save_" + fileName.getFileName());
    }

    public Data(final Path fileDictionary, final Locale locale) {
        // :NOTE: - Naming
        List<Map<String, String>> baskets1;
        Map<String, String> words1;
        try {
            final List<String> lines = Files.readAllLines(fileDictionary, StandardCharsets.UTF_8);

            words1 = getWords(lines);

        } catch (final IOException e) {
            words1 = null;
            System.err.println("Can't read input dictionary");
            System.exit(1);
        }

        try {
            final List<String> lines = Files.readAllLines(getSaveFileName(fileDictionary), StandardCharsets.UTF_8);

            baskets1 = getBuckets(lines);
        } catch (final IOException e) {
            baskets1 = null;
            System.err.println("Can't read input dictionary");
            System.exit(1);
        }

        {
            final Map<String, String> check = new HashMap<>();
            for (final Map<String, String> currentMap : baskets1) {
                check.putAll(currentMap);
            }

            if (!check.equals(words1)) {
                baskets = new ArrayList<>(Collections.nCopies(10, new HashMap<>()));
                baskets.get(0).putAll(words1);
            } else {
                baskets = baskets1;
            }
        }
        for (int i = 0; i < baskets.size(); ++i) {
            final int finalI = i;
            baskets.get(i).entrySet().forEach(entry -> words.put(entry.getKey(), new WordInfo(entry.getValue(), finalI)));
        }
    }

    private List<Map<String, String>> getBuckets(final List<String> lines) {
        final List<Map<String, String>> buckets = new ArrayList<>();

        int bucket = 0;
        int nextLineBucket = 0;
        int firstIndex = 0;
        while (bucket < 10) {
            for (int j = firstIndex + 1; j < lines.size(); ++j) {
                final String line = lines.get(j);
                if (line.length() == 1 && Character.isDigit(line.charAt(0)) || line.equals("words:")) {
                    nextLineBucket = j;
                    break;
                }
            }
            buckets.add(getWords(lines.subList(firstIndex + 1, nextLineBucket)));
            firstIndex = nextLineBucket;
            bucket++;
        }
        return buckets;
    }

    private Map<String, String> getWords(final List<String> lines) {
        final Map<String, String> words = new HashMap<>();
        for (final String str : lines) {
            // :NOTE: * No input validation
            final int indexDelimiter = str.indexOf('|');
            final String prefix = str.substring(0, indexDelimiter).toLowerCase().trim();
            final String suffix = str.substring(indexDelimiter + 1).toLowerCase().trim();

            words.put(prefix, suffix);
        }
        return words;
    }

    public String getCorrectTranslate(final String expression) {
        return words.getOrDefault(expression, new WordInfo("---", 0)).translated;
    }

    public String getRandomWord() {
        final List<Map.Entry<String, WordInfo>> entryList = words.entrySet().stream().toList();

        // :NOTE: * O(N)
        // :NOTE: * List<Double>
        final List<Double> results = entryList.stream().map(entry -> pow(1.5,- entry.getValue().basket)).toList();
        final Double sum = results.stream().mapToDouble(Double::doubleValue).sum();
        double rand = random.nextDouble() * sum;

        for (int i = 0; i < results.size(); ++i) {
            if (rand > results.get(i)) {
                rand -= results.get(i);
            } else {
                return entryList.get(i).getKey();
            }
        }
        return "";
    }

    public void increaseBasket(final String word) {
        changeBasket(word, min(words.get(word).basket + 1, 9));
    }

    private void changeBasket(final String word, final int newBasket) {
        final WordInfo wordInfo = words.get(word);
        baskets.get(wordInfo.basket).remove(word);

        words.remove(word);
        baskets.get(newBasket).put(word, wordInfo.translated);
        words.put(word, new WordInfo(wordInfo.translated, newBasket));
    }

    public void setFirstBasket(final String word) {
        changeBasket(word, 0);
    }
}
