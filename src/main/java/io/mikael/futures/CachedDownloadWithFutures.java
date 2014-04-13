package io.mikael.futures;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * In an actual production use case, you'd probably want to retry failed downloads, which would be
 * easy to achieve by squeezing the {@code CompletableFuture<Optional<Document>>} object into a more
 * easily handleable facade holder class which can retry when needed.
 */
public class CachedDownloadWithFutures {

    private final static ConcurrentMap<String, CompletableFuture<Optional<Document>>> CACHE = new ConcurrentHashMap<>();

    private static Optional<Document> download(final String url) {
        try {
            return Optional.of(Jsoup.connect(url).get());
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    public static void main(final String ... args) throws Exception {

        final CompletableFuture<Optional<Document>> f = CACHE.computeIfAbsent(
                "http://github.com/",
                (key) -> CompletableFuture.supplyAsync(() -> download(key)));

        final Optional<Document> doc;
        try {
            doc = f.get(1500, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            throw new RuntimeException("TOO SLOW", e);
        }

        System.out.println(doc.orElseThrow(() -> new RuntimeException("IOEXCEPTION")));
    }

}
