package util.proxy.proxyscraper.scrapers;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import util.Colors;
import util.TermInstructs;
import util.proxy.ProxyData;
import util.proxy.proxyscraper.ProxyLists;
import util.proxy.proxyscraper.ScraperInterface;

public class SpysDotOne implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver, boolean debug) {
        List<ProxyData> proxies = new ArrayList<>();
        
        try {
            driver.get(ProxyLists.PROXY_NOVA);

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
        } catch (Exception e) {
            if (debug) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from ProxyNova." + Colors.RESET);
            }
        }
        
        return proxies;
    }
}
