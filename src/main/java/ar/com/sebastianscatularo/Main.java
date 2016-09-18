package ar.com.sebastianscatularo;

import ar.com.sebastianscatularo.downloader.RandomArticleDownloader;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
        //book.getResources().addAll(downloader.css());
        book.getResources().addAll(downloader.images());
        book.addSection(downloader.title(), downloader.content());
        String file = downloader.title() + ".epub";
        new EpubWriter().write(book, new FileOutputStream(file));
    }
}
