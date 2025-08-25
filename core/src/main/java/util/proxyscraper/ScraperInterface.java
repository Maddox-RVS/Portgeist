package util.proxyscraper;

import networkscanner.ProxyData;
import java.util.List;

import org.openqa.selenium.WebDriver;

public interface ScraperInterface {
    public List<ProxyData> scrapeProxies(WebDriver driver);
} 