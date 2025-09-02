package networkscanner;

import util.Colors;
import util.Requests;
import util.Tor;
import util.loading.AutoProgressBar;
import util.loading.Spinner;
import util.proxy.ProxyData;
import util.proxy.ProxyFilter;
import util.proxy.ProxyData.Anonymity;
import util.proxy.proxyscraper.ProxyScraper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.text.StyledEditorKit.BoldAction;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void writeProxyJson(List<ProxyData> proxies) {
        Scanner in = new Scanner(System.in);

        File saveDirectory = new File("");
        Boolean validSaveDirectory = false;
        while (!validSaveDirectory) {
            System.out.print(Colors.BRIGHT_BLACK + "Enter save directory or (c) to open a file chooser: " + Colors.RESET);
            String input = in.nextLine();

            if (input.equalsIgnoreCase("c")) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                    saveDirectory = fileChooser.getSelectedFile();
            } else {
                saveDirectory = new File(input);
            }

            if (!saveDirectory.exists() && !saveDirectory.isDirectory())
                validSaveDirectory = false;

            if (!validSaveDirectory)
                System.out.println("Directory entered is not a valid path!");

            saveDirectory = new File(input);
        }

        System.out.print(Colors.BRIGHT_BLACK + "Enter file name (without extension): " + Colors.RESET);
        String fileName = in.nextLine();

        File saveFile = new File(saveDirectory, fileName + ".json");

        Spinner spinner = new Spinner("Saving proxies to JSON");
        spinner.start();

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(saveFile, proxies);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            spinner.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        Logger.getLogger("").setLevel(Level.SEVERE);

        // NetworkScanner.quickPortScan(
        //     "scanme.nmap.org", 
        //     5000, 
        //     NetworkScanner.Protocol.TCP);

        List<ProxyData> proxyList = ProxyScraper.scrapeProxies();
        List<ProxyData> filteredProxies = proxyList.size() > 0 ? ProxyFilter.filterProxies(proxyList) : new ArrayList<>();
        System.out.println("Found " + filteredProxies.size() + " working proxies.");
        writeProxyJson(filteredProxies);
    }
}
