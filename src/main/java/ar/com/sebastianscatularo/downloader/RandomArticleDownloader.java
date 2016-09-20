package ar.com.sebastianscatularo.downloader;

import nl.siegmann.epublib.domain.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sebastianscatularo@gmail.com.
 */
public class RandomArticleDownloader {
    private static final String RANDOM_ARTICLE_URL = "/wiki/Special:Random";
    private final Document article;
    private final String base;

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
        //article.head().select("link[rel=stylesheet]").remove();
        new ImageProcessor(this.article).execute();
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

    public Collection<Resource> css() throws IOException {
        Elements elements = article.head().select("link[rel=stylesheet]");
        return elements.stream()
                .map(element -> {
                    try{
                        URI uri = URI.create(element.absUrl("href"));
                        URL url = uri.toURL();
                        String file = url.getFile();
                        return new Resource(url.openStream(), file);
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        return null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(element -> element != null)
                .collect(Collectors.toList());
    }
/*
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
*/
    public Resource content() {
        return new Resource(article.html().getBytes(), article.title().concat(".html"));
    }
}
