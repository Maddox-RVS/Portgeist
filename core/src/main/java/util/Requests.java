package util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Requests {
    public static String get(String url, int timeoutMillis) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();

        if (Tor.isEnabled) {
            //TODO Add ability to rotate tor ip
            Proxy torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
            client = new OkHttpClient.Builder()
                .proxy(torProxy)
                .connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .build();
        }

        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful())
                return response.body().string();

            throw new IOException("Unexpected code " + response);
        }
    }
}