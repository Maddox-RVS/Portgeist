package util.proxy.proxyscraper.scrapers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;

import util.Colors;
import util.Requests;
import util.TermInstructs;
import util.proxy.ProxyData;
import util.proxy.proxyscraper.ProxyLists;
import util.proxy.proxyscraper.ScraperInterface;

public class Zaeem implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver, boolean debug) {
        List<ProxyData> proxies = new ArrayList<>();

        try {
            String proxyList = Requests.get(ProxyLists.ZAEEM20, 10000);
            String[] proxiesArray = proxyList.strip().split("\n");
            for (String proxy : proxiesArray) {
                String[] proxyParts = proxy.strip().split(":");
                if (proxyParts.length == 2) {
                    String ip = proxyParts[0];
                    int port = Integer.parseInt(proxyParts[1]);
                    proxies.add(new ProxyData(ip, port, "UNKNOWN"));
                }
            }
        } catch (IOException | InterruptedException e) {
            if (debug) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from Zaeem." + Colors.RESET);
            }
        }

        return proxies;
    }
    
}
