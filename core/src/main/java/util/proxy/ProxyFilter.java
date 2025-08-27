package util.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import util.loading.Spinner;

import java.net.Proxy;
import java.net.ProxySelector;

public class ProxyFilter {
    private static final int WAIT_DAYS = 1000;

    public static boolean proxySupportsSocksTCP(ProxyData proxyData, int timeout) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyData.getIp(), proxyData.getPort()));
            Socket socket = new Socket(proxy);
            socket.connect(new InetSocketAddress("example.com", 80), timeout);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean proxySupportsHttpTCP(ProxyData proxyData, int timeout) {
        try {
            // yeah idk man, eisther i cant get this working, or the http proxies i scraped are dead proxies
            return false; // placeholder
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean proxySupportsTCP(ProxyData proxyData, int timeout) {
        return proxySupportsSocksTCP(proxyData, timeout)
            || proxySupportsHttpTCP(proxyData, timeout);
    }

    public static List<ProxyData> filterProxies(List<ProxyData> proxies) {
        List<ProxyData> filteredProxies = new ArrayList<>();

        Spinner spinner = new Spinner("Filtering proxies");
        spinner.start();

        ExecutorService executor = Executors.newFixedThreadPool(proxies.size());
        for (ProxyData proxy : proxies) {
            executor.submit(() -> {
                if (proxySupportsTCP(proxy, 5000))
                    filteredProxies.add(proxy);
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(WAIT_DAYS, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        spinner.stop();

        return filteredProxies;
    }
}
