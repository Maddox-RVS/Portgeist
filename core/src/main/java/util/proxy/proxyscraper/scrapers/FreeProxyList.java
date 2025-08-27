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

public class FreeProxyList implements ScraperInterface {

    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver) {
        List<ProxyData> proxies = new ArrayList<>();

        try {
            driver.get(ProxyLists.FREE_PROXY_LIST);

            WebElement table = driver.findElement(By.cssSelector("table.table.table-striped.table-bordered"));
            for (WebElement row : table.findElements(By.cssSelector("tbody tr"))) {
                List<WebElement> cols = row.findElements(By.tagName("td"));

                String ip = cols.get(0).getText();
                String port = cols.get(1).getText();
                String country = cols.get(3).getText();
                String anonymity = cols.get(4).getText();

                if (anonymity.equals("elite proxy") || anonymity.equals("anonymous"))
                    proxies.add(new ProxyData(ip, Integer.parseInt(port), country));
            }

            driver.quit();
        } catch (Exception e) {
            TermInstructs.ERASE_LINE();
            TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
            System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from Free Proxy List." + Colors.RESET);
        }


        return proxies;
    }
    
}
