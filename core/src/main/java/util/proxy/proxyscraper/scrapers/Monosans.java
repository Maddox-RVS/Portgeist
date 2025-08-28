package util.proxy.proxyscraper.scrapers;

import java.lang.reflect.Proxy;
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

public class Monosans implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver, boolean debug) {
        List<ProxyData> proxies = new ArrayList<>();
        
        try {
            String jsonData = Requests.get(ProxyLists.MONOSANS);
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> data = mapper.readValue(jsonData, List.class);
            for (Map<String, Object> entry : data) {
                String ip = (String) entry.get("host");
                int port = (Integer) entry.get("port");

                Map<String, Map<String, Object>> geolocation = (Map<String, Map<String, Object>>) entry.get("geolocation");
                Map<String, String> countryData = (Map<String, String>) geolocation.get("country").get("names");
                String country = countryData.get("en");

                proxies.add(new ProxyData(ip, port, country));
            }
        } catch (Exception e) {
            if (debug) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from Monosans." + Colors.RESET);
                e.printStackTrace();
            }
        }

        return proxies;
    }
    
}
