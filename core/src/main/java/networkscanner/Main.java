package networkscanner;

import util.Colors;
import util.Requests;
import util.Tor;
import util.loading.AutoProgressBar;
import util.loading.Spinner;
import util.proxy.ProxyChain;
import util.proxy.ProxyData;
import util.proxy.ProxyFilter;
import util.proxy.ProxyData.Anonymity;
import util.proxy.proxyscraper.ProxyScraper;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.text.StyledEditorKit.BoldAction;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger.getLogger("").setLevel(Level.SEVERE);

        // NetworkScanner.quickPortScan(
        //     "scanme.nmap.org", 
        //     5000, 
        //     NetworkScanner.Protocol.TCP);

        // List<ProxyData> proxyList = ProxyScraper.scrapeProxies();
        // List<ProxyData> filteredProxies = proxyList.size() > 0 ? ProxyFilter.filterProxies(proxyList) : new ArrayList<>();
        // System.out.println("Found " + filteredProxies.size() + " working proxies.");
        // NetworkScanner.writeProxyJson(filteredProxies);

        // List<ProxyData> loadedProxies = ProxyData.loadProxyJson(new File("C:\\Users\\rynvn\\Downloads\\proxylist.json"));
        // System.out.println("Loaded " + loadedProxies.size() + " proxies from file.");
        // List<ProxyData> filteredProxies = ProxyFilter.filterProxies(loadedProxies);
        // System.out.println("Found " + filteredProxies.size() + " working proxies.");
        // for (ProxyData proxy : filteredProxies) {
        //     System.out.println(proxy);
        // }
    }
}
