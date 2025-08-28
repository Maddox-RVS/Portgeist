package util.proxy.proxyscraper.scrapers;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import util.Colors;
import util.Requests;
import util.TermInstructs;
import util.proxy.ProxyData;
import util.proxy.proxyscraper.ProxyLists;
import util.proxy.proxyscraper.ScraperInterface;

public class ErinDedeoglu implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver, boolean debug) {
        List<ProxyData> proxies = new ArrayList<>();

        try {
            String socks5list = Requests.get(ProxyLists.ERCIN_DEDEOGLU.SOCKS5).strip();
            String socks4list = Requests.get(ProxyLists.ERCIN_DEDEOGLU.SOCKS4).strip();

            String[] socks5arr = socks5list.split("\n");
            String[] socks4arr = socks4list.split("\n");

            for (String proxy : socks5arr) {
                String[] parts = proxy.split(":");
                if (parts.length == 2) {
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    proxies.add(new ProxyData(ip, port, "UNKNOWN"));
                }
            }

            for (String proxy : socks4arr) {
                String[] parts = proxy.split(":");
                if (parts.length == 2) {
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    proxies.add(new ProxyData(ip, port, "UNKNOWN"));
                }
            }
        } catch (Exception e) {
            if (debug) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from ErinDedeoglu." + Colors.RESET);
            }
        }

        return proxies;
    }
    
}
