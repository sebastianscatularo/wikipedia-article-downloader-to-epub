package ar.com.sebastianscatularo;

import ar.com.sebastianscatularo.downloader.RandomArticleDownloader;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.FileOutputStream;
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
        String file = downloader.title() + ".epub";
        new EpubWriter().write(book, new FileOutputStream(file));
    }

    public static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
}
