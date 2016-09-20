package ar.com.sebastianscatularo.downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by s.scatularo on 20/09/16.
 */
public class ImageProcessorTest {
    private static final String HTML = "<!DOCTYPE html>" + "<html>" + "<head>" + "<title>Example</title>" + "</head>" + "<body>" + "<table><tr><td><h1>HelloWorld</h1></tr>" + "</table>" + "<img src=\"http://ichef.bbci.co.uk/news/660/cpsprodpb/025B/production/_85730600_monkey2.jpg\">" + "</body>" + "</html>";

    @Test
    public void execute() throws Exception {
        Document doc = Jsoup.parse(HTML, "UTF-8");
        new ImageProcessor(doc).execute();
        assertTrue(doc.html().contains("data"));
        Files.write(Paths.get(doc.title().concat(".html")), doc.html().getBytes());
    }
}