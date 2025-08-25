package util.proxyscraper.scrapers;

import java.util.ArrayList;
import java.util.List;

import networkscanner.ProxyData;
import util.proxyscraper.ScraperInterface;
import util.Colors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProxyNova implements ScraperInterface {    
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver) {
        List<ProxyData> proxies = new ArrayList<>();
        
        try {
            driver.get("https://www.proxynova.com/proxy-server-list/");

            WebElement table = driver.findElement(By.id("tbl_proxy_list"));
            for (WebElement row : table.findElements(By.cssSelector("tbody tr"))) {
                List<WebElement> cols = row.findElements(By.tagName("td"));
                
                String ip = cols.get(0).getText();
                String port = cols.get(1).getText();
                String country = cols.get(5).getText();
                String anonymity = cols.get(6).getText();

                if (anonymity.equals("Elite") || anonymity.equals("Anonymous"))
                    proxies.add(new ProxyData(ip, Integer.parseInt(port), country));
            }

            driver.quit();
        } catch (Exception e) {
            System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + "Issue fetching proxies from ProxyNova: " + e.getMessage() + Colors.RESET);
        }

        return proxies;
    }
}
