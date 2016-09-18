package ar.com.sebastianscatularo.downloader;

import nl.siegmann.epublib.domain.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sebastianscatularo@gmail.com.
 */
public class RandomArticleDownloader {
    private static final String PRINTABLE = "/w/index.php?title=%s&printable=yes";
    private static final String RANDOM_ARTICLE_URL = "/wiki/Special:Random";
    private final URI printableArticleUri;
    private final Document article;
    private final String base;

    public RandomArticleDownloader(String base) throws IOException {
        this.base = base;
        String title = Jsoup.connect(randomArticle()).get().location().replace("https://es.wikipedia.org/wiki/", "");
        this.article = Jsoup.connect(printableArticle(title)).get();
        this.printableArticleUri = URI.create(printableArticle(title));
    }

    private String printableArticle(String title) {
        return base.concat(String.format(PRINTABLE, title));
    }

    private String randomArticle() {
        return base.concat(RANDOM_ARTICLE_URL);
    }

    public String title() {
        return article.title();
    }

    public Collection<Resource> css() throws IOException {
        Elements elements = article.head().select("link[rel=stylesheet]");
        List<Resource> resources = new ArrayList<>();
        int inc = 0;
        for (Element element : elements) {
            InputStream stream = newInputStream(URI.create(base + element.attr("href")));
            String name = String.valueOf(inc++).concat(".css");
            element.replaceWith(new Element(Tag.valueOf(String.format("<link rel=\"stylesheet\" href=\"%s\">", name)), ""));
            resources.add(new Resource(stream, name));
        }
        return resources;
    }

    private InputStream newInputStream(URI href) throws IOException {
        return href.toURL().openConnection().getInputStream();
    }

    public Collection<Resource> images() throws IOException {
        Elements elements = article.body().select("img");
        List<Resource> resources = new ArrayList<>();
        int id = 0;
        try {
            for (Element e : elements) {
                String src = e.absUrl("src");
                if (src.contains("Special:CentralAutoLogin")) {
                    continue;
                }
                Path path = Paths.get(e.attr("src"));
                InputStream inputStream = URI.create(src).toURL().openConnection().getInputStream();
                Resource resource = new Resource(inputStream, path.toString());
                resource.setId(String.valueOf(id++));
                resources.add(resource);
            }
            article.select("img").forEach(element -> {
                if (element.attr("src").startsWith("//")) {
                    element.attr("src", element.attr("src").replaceFirst("//", ""));
                } else if (element.attr("src").startsWith("/")) {
                    element.attr("src", element.attr("src").replaceFirst("/", ""));
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return resources;
    }

    public Resource content() {
        return new Resource(article.html().getBytes(), article.title().concat(".html"));
    }
}
