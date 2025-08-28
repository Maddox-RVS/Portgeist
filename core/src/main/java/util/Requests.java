package util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Requests {
    public static String get(String url) throws IOException, InterruptedException {
        if (Tor.isEnabled) {
            System.setProperty("socksProxyHost", "127.0.0.1");
            System.setProperty("socksProxyPort", "9050");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
