package ar.com.sebastianscatularo.downloader;

import nl.siegmann.epublib.domain.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author sebastianscatularo@gmail.com.
 */
public class RandomArticleDownloader {
    private static final Resource EMPTY = new Resource("empty");
    private static final String RANDOM_ARTICLE_URL = "/wiki/Special:Random";
    private final Document article;
    private final Collection<Resource> css;
    private final Collection<Resource> images;
    private final InputStream contentStream;
    private final String base;
    private final String fileName;

    public RandomArticleDownloader(String base) throws IOException {
        this.base = base;
        String title = Jsoup.connect(randomArticle()).get().location().replace("https://es.wikipedia.org/wiki/", "");
        article = Jsoup.connect(printableArticle(title)).get();
        List<Element> elements = new ArrayList<>();
        elements.addAll(article.select(".noprint"));
        elements.addAll(article.select(".suggestions"));
        elements.addAll(article.select("script"));
        elements.add(article.getElementById("jump-to-nav"));
        elements.add(article.getElementById("mw-navigation"));
        elements.add(article.getElementById("mw-normal-catlinks"));
        elements.add(article.getElementById("mw-hidden-catlinks"));
        elements.add(article.getElementById("footer-places-mobileview"));
        elements.stream().filter(element -> element != null).forEach(Node::remove);

        css = article.head().select("link")
                .stream()
                .map(element -> {
                    try {
                        if (!"stylesheet".equals(element.attr("rel"))) {
                            element.remove();
                            return EMPTY;
                        } else {
                            String src = element.absUrl("href");
                            String uuid = UUID.randomUUID().toString().concat(".css");
                            InputStream stream = URI.create(src).toURL().openStream();
                            Resource resource = new Resource(stream, uuid);
                            resource.setId(uuid);
                            element.attr("href", uuid);
                            return resource;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return EMPTY;
                    }
                })
                .filter(element -> !EMPTY.equals(element))
                .collect(Collectors.toList());

        images = article.body().select("img")
                .stream()
                .map(element -> {
                    try {
                        String src = element.absUrl("src");
                        String uuid = UUID.randomUUID().toString().concat(".png");
                        InputStream stream = URI.create(src).toURL().openStream();
                        Resource resource = new Resource(stream, uuid);
                        resource.setId(uuid);
                        element.attr("src", uuid);
                        return resource;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return EMPTY;
                    }
                })
                .filter(element -> !EMPTY.equals(element))
                .collect(Collectors.toList());

        contentStream = new ByteArrayInputStream(article.html().getBytes());
        fileName = article.title().concat(".html");
    }

    private String printableArticle(String title) {
        return base.concat("/w/index.php?title=").concat(title).concat("&printable=yes");
    }

    private String randomArticle() {
        return base.concat(RANDOM_ARTICLE_URL);
    }

    public String title() {
        return article.title();
    }

    public Resource content() throws IOException {
        return newResource(contentStream, fileName);
    }

    public Collection<Resource> images() {
        return images;
    }

    public Collection<Resource> css() {
        return css;
    }

    private Resource newResource(InputStream stream, String href) throws IOException {
        Resource resource = new Resource(stream, href);
        resource.setId(UUID.randomUUID().toString());
        return resource;
    }
}
