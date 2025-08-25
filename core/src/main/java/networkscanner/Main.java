package networkscanner;

import util.proxyscraper.ProxyScraper;

public class Main {
    public static void main(String[] args) throws Exception {
        // NetworkScanner.quickPortScan(
        //     "scanme.nmap.org", 
        //     5000, 
        //     NetworkScanner.Protocol.TCP);

        ProxyScraper.scrapeProxies();
    }
}
