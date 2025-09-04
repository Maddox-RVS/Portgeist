package util.proxy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.checkerframework.checker.units.qual.t;

import com.fasterxml.jackson.databind.ObjectMapper;

import util.Colors;
import util.loading.Spinner;

public class ProxyData {
    public static void writeProxyJson(List<ProxyData> proxies) {
        Scanner in = new Scanner(System.in);

        File saveDirectory = new File("");
        Boolean validSaveDirectory = false;
        while (!validSaveDirectory) {
            System.out.print(Colors.BRIGHT_BLACK + "Enter save directory or (c) to open a file chooser: " + Colors.RESET);
            String input = in.nextLine();

            if (input.equalsIgnoreCase("c")) {
                JFrame chooserFrame = new JFrame();
                chooserFrame.setAlwaysOnTop(true);
                chooserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                chooserFrame.setUndecorated(true);
                chooserFrame.setLocationRelativeTo(null);
                chooserFrame.setVisible(true);

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(chooserFrame);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                    saveDirectory = fileChooser.getSelectedFile();
                chooserFrame.dispose();
            } else {
                saveDirectory = new File(input);
            }

            validSaveDirectory = saveDirectory.exists() && saveDirectory.isDirectory();
            if (!validSaveDirectory) System.out.println("Directory entered is not a valid path!");
        }

        System.out.print(Colors.BRIGHT_BLACK + "Enter file name (without extension): " + Colors.RESET);
        String fileName = in.nextLine();

        in.close();

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

    public static List<ProxyData> loadProxyJson(File jsonFile) {
        List<ProxyData> proxies = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> rawProxies = mapper.readValue(jsonFile, List.class);
            for (Map<String, Object> rawProxy : rawProxies) {
                String ip = (String) rawProxy.get("ip");
                int port = (int) rawProxy.get("port");
                String country = (String) rawProxy.get("country");
                Anonymity anonymity = Anonymity.valueOf((String) rawProxy.get("anonymity"));

                proxies.add(new ProxyData(ip, port, country, anonymity));
            }
        } catch (IOException e) {
            System.out.println(Colors.BG_RED + Colors.WHITE + "ERROR" 
            + Colors.RESET + Colors.RED + " An issue occurred while reading in the proxy JSON file." + Colors.RESET);
            e.printStackTrace();
            return new ArrayList<>();
        }

        return proxies;
    }
    
    public enum Anonymity {
        TRANSPARENT,
        ANONYMOUS,
        ELITE,
        UNKNOWN
    }

    private String ip;
    private int port;
    private String country;
    private Anonymity anonymity;

    public ProxyData(String ip, int port, String country, Anonymity anonymity) {
        this.ip = ip;
        this.port = port;
        this.country = country;
        this.anonymity = anonymity;
    }

    public ProxyData(String ip, int port, String country) {
        this.ip = ip;
        this.port = port;
        this.country = country;
        this.anonymity = Anonymity.UNKNOWN;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getCountry() {
        return country;
    }

    public Anonymity getAnonymity() {
        return anonymity;
    }

    public void setAnonymity(Anonymity anonymity) {
        this.anonymity = anonymity;
    }

    @Override
    public String toString() {
        return "ProxyData{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            ", country='" + country + '\'' +
            ", anonymity=" + anonymity +
            '}';
    }
}
