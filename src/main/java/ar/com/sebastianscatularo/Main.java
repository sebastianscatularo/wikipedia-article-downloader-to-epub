package ar.com.sebastianscatularo;

import ar.com.sebastianscatularo.downloader.RandomArticleDownloader;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.FileOutputStream;

/**
 * @author sebastianscatularo@gmail.com.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        RandomArticleDownloader downloader = new RandomArticleDownloader("https://es.wikipedia.org");
        Book book = new Book();
        Metadata metadata = book.getMetadata();
        metadata.addTitle(downloader.title());
        metadata.addAuthor(new Author("Wikipedia", "http://www.wikipedia.org"));
        book.addSection(downloader.title(), downloader.content());
        book.getResources().addAll(downloader.css());
        book.getResources().addAll(downloader.images());
        String file = downloader.title() + ".epub";
        new EpubWriter().write(book, new FileOutputStream(file));
    }
}
