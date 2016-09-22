package ar.com.sebastianscatularo;

import ar.com.sebastianscatularo.downloader.RandomArticleDownloader;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * @author sebastianscatularo@gmail.com.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String wiki = getRandom(new String[] {"https://en.wikipedia.org", "https://es.wikipedia.org"});
        RandomArticleDownloader downloader = new RandomArticleDownloader(wiki);
        Book book = new Book();
        Metadata metadata = book.getMetadata();
        metadata.addTitle(downloader.title());
        metadata.addAuthor(new Author("Wikipedia", "https://www.wikipedia.org"));
        book.addSection(downloader.title(), downloader.content());
        book.getResources().addAll(downloader.css());
        book.getResources().addAll(downloader.images());
        OutputStream stream = Files.newOutputStream(file(downloader.title()));
        new EpubWriter().write(book, stream);
    }

    public static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    private static Path file(String title) throws IOException {
        String home = System.getProperty("user.home");
        Path wikipedia = Paths.get(home, "Wikipedia");
        if (Files.notExists(wikipedia)) {
            Files.createDirectory(wikipedia);
        }
        return Paths.get(home, "Wikipedia", title.concat(".epub"));
    }
}
