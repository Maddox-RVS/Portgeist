package util.proxyscraper;

import java.util.ArrayList;
import java.util.List;
import networkscanner.ProxyData;
import util.loading.Spinner;
import util.proxyscraper.scrapers.ProxyNova;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ProxyScraper {
    public static List<ProxyData> scrapeProxies() {
        final String TOR_NETWORK = "127.0.0.1:9050";
        final String TOR_CHECK_URL = "https://check.torproject.org/";

        List<ProxyData> proxies = new ArrayList<>();

        Spinner spinner = new Spinner("Configuring selenium with TOR network");
        spinner.start();

        WebDriverManager.chromedriver().setup();
        Proxy proxy = new Proxy();
        proxy.setSocksProxy(TOR_NETWORK);
        proxy.setSocksVersion(5);
        ChromeOptions options = new ChromeOptions();
        options.setProxy(proxy);
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);

        spinner.stop();

        spinner = new Spinner("Checking TOR connection");
        spinner.start();

        driver.get(TOR_CHECK_URL);
        boolean torConnected = driver.getPageSource().contains("Congratulations. This browser is configured to use Tor");
        
        spinner.stop();

        if (torConnected) {
            System.out.println("TOR connection established successfully.");
        } else {
            System.out.println("Failed to establish TOR connection.");
            return proxies;
        }

        ProxyNova proxyNova = new ProxyNova();
        proxies.addAll(proxyNova.scrapeProxies(driver));

        driver.quit();
        return proxies;
    }
}
