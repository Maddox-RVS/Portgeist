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

public class JetKai implements ScraperInterface {
    @Override
    public List<ProxyData> scrapeProxies(WebDriver driver, boolean debug) {
        List<ProxyData> proxies = new ArrayList<>();

        try {
            String jsonData = Requests.get(ProxyLists.JETKAI, 10000);
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> proxyList = mapper.readValue(jsonData, List.class);
            for (Map<String, Object> proxyData : proxyList) {
                String ip = (String) proxyData.get("ip");
                int port = (Integer) proxyData.get("port");
                proxies.add(new ProxyData(ip, port, "UNKNOWN"));
            }
        } catch (IOException | InterruptedException e) {
            if (debug) {
                TermInstructs.ERASE_LINE();
                TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                System.out.println(Colors.BG_RED + "Error" + Colors.RESET + Colors.RED + " Issue fetching proxies from JetKai." + Colors.RESET);
                e.printStackTrace();
            }
        }

        return proxies;
    }
}
