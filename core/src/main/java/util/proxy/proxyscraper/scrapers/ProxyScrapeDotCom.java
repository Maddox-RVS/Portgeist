package util.proxy.proxyscraper.scrapers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import util.Colors;
import util.TermInstructs;
import util.proxy.ProxyData;
import util.proxy.proxyscraper.ProxyLists;
import util.proxy.proxyscraper.ScraperInterface;

public class ProxyScrapeDotCom implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver) {
        List<ProxyData> proxies = new ArrayList<>();

        try {
            driver.get(ProxyLists.PROXY_SCRAPE_DOT_COM);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("CybotCookiebotDialogBodyButtonAccept")));
            acceptCookies.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.Pagination_root__SEr0m")));

            for (int i = 0; i < 100; i++) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table tbody")));

                WebElement table = driver.findElement(By.cssSelector("div.mx-4.overflow-x-auto table"));
                for (WebElement row : table.findElements(By.cssSelector("tbody tr"))) {
                    List<WebElement> cols = row.findElements(By.tagName("td"));

                    String ip = cols.get(1).getText();
                    String port = cols.get(2).getText();
                    String country = cols.get(4).getText();
                    String anonymity = cols.get(5).getText();

                    if (anonymity.equals("Elite"))
                        proxies.add(new ProxyData(ip, Integer.parseInt(port), country));
                }
    
                List<WebElement> nextButtons = driver.findElements(By.xpath("//a[text()='next']"));
                
                if (nextButtons.isEmpty()) {
                    System.out.println("No NEXT button found. Probably last page reached.");
                    break;
                }

                WebElement nextButton = nextButtons.get(0);

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", nextButton);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);
                Thread.sleep(1200);
            }

            driver.quit();
        } catch (Exception e) {
            TermInstructs.ERASE_LINE();
            TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
            System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from ProxyScrape.com." + Colors.RESET);
        }

        return proxies;
    }
    
}
