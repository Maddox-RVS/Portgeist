package util.proxy.proxyscraper.scrapers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;

import util.Colors;
import util.Requests;
import util.TermInstructs;
import util.proxy.ProxyData;
import util.proxy.proxyscraper.ProxyLists;
import util.proxy.proxyscraper.ScraperInterface;

public class Proxifly implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver, boolean debug) {
        List<ProxyData> proxies = new ArrayList<>();
        
        try {
            String jsonData = Requests.get(ProxyLists.PROXIFLY, 10000);
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> proxyListData = mapper.readValue(jsonData, List.class);
            for (Map<String, Object> proxyData : proxyListData) {
                String ip = (String) proxyData.get("ip");
                int port = (Integer) proxyData.get("port");

                Map<String, String> geolocation = (Map<String, String>) proxyData.get("geolocation");
                String country = geolocation.get("country") + geolocation.get("city");

                String anonymity = (String) proxyData.get("anonymity");

                if (anonymity.equals("elite"))
                    proxies.add(new ProxyData(ip, port, country));
            }
        } catch (IOException | InterruptedException e) {
            if (debug) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from Proxifly." + Colors.RESET);
                e.printStackTrace();
            }   
        }

        return proxies;
    }
}
