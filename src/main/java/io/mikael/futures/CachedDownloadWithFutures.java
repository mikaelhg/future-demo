package io.mikael.futures;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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

        final Optional<Document> doc = f.get(1500, TimeUnit.MILLISECONDS);

        System.out.println(doc.orElseThrow(() -> new RuntimeException("TOO SLOW")));
    }

}