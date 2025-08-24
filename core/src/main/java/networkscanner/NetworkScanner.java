package networkscanner;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import util.loading.ManualProgressBar;
import util.loading.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.Port;

import util.Colors;
import util.TermInstructs;

public class NetworkScanner {
    private record ServiceRecord(String serviceName, int port, double frequency, String comment) {}

    private record PortScanResult(ServiceRecord serviceRecord, boolean open) {}

    private static List<ServiceRecord> getNmapServicesTCP() throws IOException, StreamReadException, DatabindException {
        // Load and parse nmap-services.json for TCP services
        List<ServiceRecord> serviceRecords = new ArrayList<>();

        InputStream in = NetworkScanner.class.getResourceAsStream("/nmap-services.json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(in, Map.class);
        List<Map<String, Object>> tcpServices = (List<Map<String, Object>>) data.get("tcp");
        
        for (Map<String, Object> service : tcpServices) {
            String serviceName = (String) service.get("service name");
            int port = (int) service.get("port");
            double frequency = (double) service.get("frequency");
            String comment = (String) service.get("comment");

            serviceRecords.add(new ServiceRecord(serviceName, port, frequency, comment));
        }

        return serviceRecords;
    }

    private static ServiceRecord[] getNmapServicesUDP() {
        // Load and parse nmap-services.json for UDP services
        return new ServiceRecord[0]; // Placeholder
    }

    public static void quickPortScanTCP() {
        // Implement TCP port scanning logic here
    }

    public static void quickPortScanUDP() {
        // Implement UDP port scanning logic here
    }

    public static List<PortScanResult> thoroughPortScanTCP(String target, int timeout) {
        List<PortScanResult> scanResults = new ArrayList<>();
        
        try {
            Spinner spinner = new Spinner("Loading nmap-services data");
            spinner.start();

            List<ServiceRecord> serviceRecords = getNmapServicesTCP();

            spinner.stop();

            ManualProgressBar progressBar = new ManualProgressBar("SCANNING", serviceRecords.size(), 30);
            progressBar.setShowPercent(true);

            System.out.printf(Colors.BLUE + "%-10s %-20s %-10s%n" + Colors.RESET, "Port", "Service", "Status");

            for (ServiceRecord serviceRecord : serviceRecords) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(target, serviceRecord.port()), timeout);
                    scanResults.add(new PortScanResult(serviceRecord, true));

                    TermInstructs.ERASE_LINE();
                    TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                    System.out.printf("%-10d %-20s %-10s%n",
                        serviceRecord.port(),
                        serviceRecord.serviceName(),
                        Colors.GREEN + "Open" + Colors.RESET);
                } catch (IOException e) {
                    scanResults.add(new PortScanResult(serviceRecord, false));

                    TermInstructs.ERASE_LINE();
                    TermInstructs.MOVE_CURSOR_TO_LINE_BEG();
                    System.out.printf("%-10d %-20s %-10s%n",
                        serviceRecord.port(),
                        serviceRecord.serviceName(),
                        Colors.RED + "Closed" + Colors.RESET);

                }

                progressBar.increment();
                System.out.print(progressBar.getDisplay());
            }
        } catch (IOException e) {
            System.out.println(Colors.BG_RED + Colors.WHITE + "ERROR" 
                                + Colors.RESET + Colors.RED + "An issue occurred while reading in nmap-services data.");
            return new ArrayList<>();
        }

        printPortScanResults(scanResults);
        return scanResults;
    }

    public static List<PortScanResult> thoroughPortScanUDP(String target, int timeout) {
        // Implement thorough UDP port scanning logic here
        return new ArrayList<>();
    }

    public static void deepPortScanTCP() {
        // Implement detailed TCP port scanning logic here
    }

    public static void deepPortScanUDP() {
        // Implement detailed UDP port scanning logic here
    }

    public static boolean connectTCP(String target, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(target, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void printPortScanResults(List<PortScanResult> portScanResults) {
        System.out.printf(Colors.BLUE + "%-10s %-20s %-10s %-10s %-30s%n" + Colors.RESET, "Port", "Service", "Status", "Frequency", "Comment");
        
        for (PortScanResult result : portScanResults) {
            String status = result.open() ? Colors.GREEN + "Open" + Colors.RESET : Colors.RED + "Closed" + Colors.RESET;
            System.out.printf("%-10d %-20s %-10s %-10.2f %-30s%n", 
                              result.serviceRecord().port(), 
                              result.serviceRecord().serviceName(), 
                              status, 
                              result.serviceRecord().frequency(), 
                              result.serviceRecord().comment());
        }
    }
}