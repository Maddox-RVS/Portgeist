package networkscanner;

import util.loading.AutoProgressBar;
import util.proxy.ProxyData;
import util.proxy.ProxyFilter;
import util.proxy.proxyscraper.ProxyScraper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws Exception {
        Logger.getLogger("").setLevel(Level.SEVERE);

        // NetworkScanner.quickPortScan(
        //     "scanme.nmap.org", 
        //     5000, 
        //     NetworkScanner.Protocol.TCP);

        // ProxyData proxy = new ProxyData("194.152.44.171", 80, "Unknown");
        // boolean supportsTCP = ProxyFilter.proxySupportsTCP(proxy, 15000);
        // System.out.println("Proxy supports TCP: " + supportsTCP);

        List<ProxyData> proxyList = ProxyScraper.scrapeProxies();
        List<ProxyData> filteredProxies = ProxyFilter.filterProxies(proxyList);
        System.out.println("Found " + filteredProxies.size() + " working proxies.");
    }
}
