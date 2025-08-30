package util.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.Authenticator.RequestorType;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.checkerframework.checker.units.qual.h;

import net.bytebuddy.implementation.attribute.AnnotationRetention;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import util.UnsafeOkHttpClient;
import util.loading.AutoProgressBar;
import util.loading.ManualProgressBar;
import util.loading.Spinner;
import util.proxy.ProxyData.Anonymity;

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

    public static boolean proxySupportsTCP(ProxyData proxyData, int timeout) {
        return proxySupportsSocksTCP(proxyData, timeout);
    }

    private static String getHttpbin(Proxy socksProxy, int timeoutMillis) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        OkHttpClient client = UnsafeOkHttpClient.getUnsafeOkHttpClient(socksProxy, timeoutMillis);

        Request request = new Request.Builder()
            .url("https://httpbin.org/get")
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return responseBody;
            }
        }

        return null;
    }

    public static Anonymity discoverProxyAnonymity(ProxyData proxyData, int timeout) {
        try {
            Proxy socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyData.getIp(), proxyData.getPort()));
            String httpbinResults = getHttpbin(socksProxy, timeout);
            if (httpbinResults != null) {
                InetAddress myIpv4Address = InetAddress.getLocalHost();
                if (httpbinResults.contains(myIpv4Address.getHostAddress())) {
                    return Anonymity.TRANSPARENT;
                } else if (httpbinResults.contains("X-Forwarded-For")
                            || httpbinResults.contains("Forwarded")
                            || httpbinResults.contains("Via")
                            || httpbinResults.contains("X-Real-IP")
                            || httpbinResults.contains("X-Forwarded-Host")
                            || httpbinResults.contains("X-Forwarded-Proto")
                            || httpbinResults.contains("X-ProxyUser-Ip")) {
                    return Anonymity.ANONYMOUS;
                } else {
                    return Anonymity.ELITE;
                }
            }

            return Anonymity.UNKNOWN;
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            return Anonymity.UNKNOWN;
        }
    }

    public static List<ProxyData> filterProxies(List<ProxyData> proxies) {
        List<ProxyData> unprocessedProxies = new ArrayList<>();
        for (ProxyData proxy : proxies) unprocessedProxies.add(proxy);
        List<ProxyData> filteredProxies = new ArrayList<>();

        unprocessedProxies = new ArrayList<>(new HashSet<>(unprocessedProxies));

        Function<ProxyData, Boolean> isProxyValid = (proxyData) -> {
            Anonymity anonymityLevel = discoverProxyAnonymity(proxyData, 10000);
            boolean supportsTCP = proxySupportsTCP(proxyData, 10000);
            return anonymityLevel == Anonymity.ELITE && supportsTCP;
        };

        AutoProgressBar progressBar = new AutoProgressBar("Filtering Proxies: ", unprocessedProxies.size(), 20);
        progressBar.setShowPercent(true);
        progressBar.start();

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(unprocessedProxies.size(), 5000));
        for (ProxyData proxy : unprocessedProxies) {
            executor.submit(() -> {
                progressBar.setLoadingMessage("Filtering Proxies, Location[" + proxy.getCountry() + "]" + " Ip[" + proxy.getIp() + "]");
                if (isProxyValid.apply(proxy)) {
                    proxy.setAnonymity(Anonymity.ELITE);
                    filteredProxies.add(proxy);
                }
                progressBar.increment();
            });
        }

        executor.shutdown();
        
        try {
            executor.awaitTermination(WAIT_DAYS, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        progressBar.setLoadingMessage("Filtering Complete.");
        progressBar.stop();

        return filteredProxies;
    }
}
