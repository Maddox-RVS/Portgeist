package util.proxy.proxyscraper;

import java.util.List;

import org.openqa.selenium.WebDriver;

import util.proxy.ProxyData;

public interface ScraperInterface {
    public List<ProxyData> scrapeProxies(WebDriver driver);
} 