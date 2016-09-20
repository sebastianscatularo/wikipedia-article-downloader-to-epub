package ar.com.sebastianscatularo.downloader;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;

/**
 * @author Sebastian Scatularo
 */
public class ImageProcessor {
    private static final Logger log = LoggerFactory.getLogger(ImageProcessor.class);

    private final Document document;

    public ImageProcessor(Document document) {
        this.document = document;
    }

    public void execute() {
        document.select("img").forEach(element -> {
            String src = Optional.ofNullable(element.absUrl("src")).orElse("");
            if (!"".equals(src) && !src.startsWith("data:")) {
                try {
                    URI uri = URI.create(src);
                    /*if (!uri.isAbsolute()) {
                        uri = URI.create("https://es.wikipedia.org".concat(uri.toString()));
                    }*/
                    URL url = uri.toURL();
                    try (InputStream stream = url.openStream()) {
                        String contentType = getContentType(url);
                        byte[] base64EncodeContent = getBase64EncodeContent(stream);
                        String data = "data:" + contentType + ";base64," + new String(base64EncodeContent);
                        element.attr("src", data);
                    } catch (IOException ex) {
                        log.info("Error in Stream", ex);
                    }
                } catch (MalformedURLException ex) {
                    log.info("Invalid URL ", ex);
                }
            }
        });
    }

    private String getContentType(URL url) throws IOException {
        return url.openConnection().getContentType();
    }

    private byte[] getBase64EncodeContent(InputStream stream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int bytesRead;
        while ((bytesRead = stream.read(chunk)) > 0) {
            outputStream.write(chunk, 0, bytesRead);
        }
        stream.close();
        return Base64.getEncoder().encode(outputStream.toByteArray());
    }
}
