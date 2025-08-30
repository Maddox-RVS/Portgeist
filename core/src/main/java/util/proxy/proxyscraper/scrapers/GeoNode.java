package util.proxy.proxyscraper.scrapers;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.Colors;
import util.Requests;
import util.TermInstructs;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;

import util.proxy.ProxyData;
import util.proxy.proxyscraper.ProxyLists;
import util.proxy.proxyscraper.ScraperInterface;

public class GeoNode implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver, boolean debug) {
        List<ProxyData> proxies = new ArrayList<>();

        try {
            String proxyJson = Requests.get(ProxyLists.GEONODE, 10000);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<Map<String, Object>>> data = mapper.readValue(proxyJson, Map.class);

            List<Map<String, Object>> proxyListData = data.get("data");
            for (Map<String, Object> proxy : proxyListData) {
                String ip = (String) proxy.get("ip");
                int port = Integer.parseInt((String) proxy.get("port"));
                String country = (String) proxy.get("country") + " - " + (String) proxy.get("city");
                String anonymity = (String) proxy.get("anonymityLevel");

                if (!anonymity.equals("elite")) continue;

                proxies.add(new ProxyData(ip, port, country));
            }
        } catch (IOException | InterruptedException e) {
            if (debug) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from Geonode." + Colors.RESET);
                e.printStackTrace();
            }
        }

        return proxies;
    }
}
