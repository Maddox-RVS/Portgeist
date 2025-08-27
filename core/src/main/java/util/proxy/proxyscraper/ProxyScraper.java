package util.proxy.proxyscraper;

import util.selector.Inputer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.Colors;
import util.Tor;
import util.loading.Spinner;
import util.proxy.ProxyData;
import util.proxy.proxyscraper.scrapers.FreeProxyList;
import util.proxy.proxyscraper.scrapers.GeoNode;
import util.proxy.proxyscraper.scrapers.ProxyNova;
import util.proxy.proxyscraper.scrapers.ProxyScrapeDotCom;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.Color;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ProxyScraper {
    public static List<ProxyData> scrapeProxies() {
        final String TOR_NETWORK = "127.0.0.1:9050";
        final String TOR_CHECK_URL = "https://check.torproject.org/";

        List<ProxyData> proxies = new ArrayList<>();

        Tor tor = new Tor();
        tor.startTorProcess();

        Spinner spinner = new Spinner("Configuring selenium with TOR network");
        spinner.start();

        WebDriverManager.chromedriver().setup();
        Proxy proxy = new Proxy();
        proxy.setSocksProxy(TOR_NETWORK);
        proxy.setSocksVersion(5);
        ChromeOptions options = new ChromeOptions();
        options.setProxy(proxy);
        options.addArguments("--ignore-certificate-errors");
        // options.addArguments("--headless");
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
            boolean continueWithoutTor = Inputer.askYesOrNo("Would you like to continue scraping proxy list from the web without a tor connection?" +
                            " Note, your IP address may be exposed to the proxy list sites that are scraped.");
            if (!continueWithoutTor) return proxies;
        }

        spinner = new Spinner("Scraping proxies");
        spinner.start();

        ProxyNova proxyNova = new ProxyNova();
        proxies.addAll(proxyNova.scrapeProxies(driver));

        GeoNode geoNode = new GeoNode();
        proxies.addAll(geoNode.scrapeProxies(driver));

        FreeProxyList freeProxyList = new FreeProxyList();
        proxies.addAll(freeProxyList.scrapeProxies(driver));

        ProxyScrapeDotCom proxyScrapeDotCom = new ProxyScrapeDotCom();
        proxies.addAll(proxyScrapeDotCom.scrapeProxies(driver));

        spinner.stop();

        driver.quit();
        return proxies;
    }
}
