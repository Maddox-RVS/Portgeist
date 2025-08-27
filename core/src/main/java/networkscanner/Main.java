package networkscanner;

import util.proxy.ProxyData;
import util.proxy.proxyscraper.ProxyScraper;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws Exception {
        Logger.getLogger("").setLevel(Level.SEVERE);

        // NetworkScanner.quickPortScan(
        //     "scanme.nmap.org", 
        //     5000, 
        //     NetworkScanner.Protocol.TCP);

        List<ProxyData> proxyList = ProxyScraper.scrapeProxies();
        for (ProxyData proxy : proxyList) {
            System.out.println("Found proxy: " + proxy);
        }
    }
}
