package networkscanner;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import util.loading.ManualProgressBar;
import util.loading.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.Port;

import util.Colors;
import util.TermInstructs;

public class NetworkScanner {
    private static final int MAX_PORTS = 65535;
    private static final int WAIT_DAYS = 1000;
    private static final int UDP_THREAD_POOL_SIZE = 100;

    public enum Protocol {
        TCP,
        UDP
    }

    private record ServiceRecord(String serviceName, int port, double frequency, String comment) {}

    private record PortScanResult(ServiceRecord serviceRecord, boolean open) {}

    private static List<ServiceRecord> getNmapServicesTCP() throws IOException, StreamReadException, DatabindException {
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

    private static List<ServiceRecord> getNmapServicesUDP() throws IOException, StreamReadException, DatabindException {
        List<ServiceRecord> serviceRecords = new ArrayList<>();

        InputStream in = NetworkScanner.class.getResourceAsStream("/nmap-services.json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(in, Map.class);
        List<Map<String, Object>> tcpServices = (List<Map<String, Object>>) data.get("udp");
        
        for (Map<String, Object> service : tcpServices) {
            String serviceName = (String) service.get("service name");
            int port = (int) service.get("port");
            double frequency = (double) service.get("frequency");
            String comment = (String) service.get("comment");

            serviceRecords.add(new ServiceRecord(serviceName, port, frequency, comment));
        }

        return serviceRecords;
    }

    public static void quickPortScan(String target, int timeout, Protocol protocol) {
        List<PortScanResult> scanResults = new ArrayList<>();

        try {
            Spinner spinner = new Spinner("Loading nmap-services data");
            spinner.start();

            List<ServiceRecord> serviceRecords = protocol == Protocol.TCP ? getNmapServicesTCP() : getNmapServicesUDP();

            spinner.stop();

            serviceRecords.sort((a, b) -> Double.compare(b.frequency(), a.frequency()));
            serviceRecords = serviceRecords.subList(0, Math.min(1000, serviceRecords.size()));

            System.out.printf(Colors.BLUE + "%-10s %-20s %-10s%n" + Colors.RESET, "Port", "Service", "Status");

            ExecutorService executor = protocol == Protocol.TCP ? Executors.newFixedThreadPool(serviceRecords.size()) : Executors.newFixedThreadPool(UDP_THREAD_POOL_SIZE);
            for (ServiceRecord serviceRecord : serviceRecords) {
                executor.submit(() -> {
                    boolean isOpen = protocol == Protocol.TCP ? connectTCP(target, serviceRecord.port(), timeout)
                                                                : connectUDP(target, serviceRecord.port(), timeout);
                    scanResults.add(new PortScanResult(serviceRecord, isOpen));

                    System.out.printf("%-10d %-20s %-10s%n",
                        serviceRecord.port(),
                        serviceRecord.serviceName(),
                        isOpen ? Colors.GREEN + "Open" + Colors.RESET : Colors.RED + "Closed" + Colors.RESET);
                });
            }
            executor.shutdown();

            try {
                executor.awaitTermination(WAIT_DAYS, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            System.out.println(Colors.BG_RED + Colors.WHITE + "ERROR"
                    + Colors.RESET + Colors.RED + " An issue occurred while reading in nmap-services data." + Colors.RESET);
        }

    }

    public static List<PortScanResult> thoroughPortScan(String target, int timeout, Protocol protocol) {
        List<PortScanResult> scanResults = new ArrayList<>();
        
        try {
            Spinner spinner = new Spinner("Loading nmap-services data");
            spinner.start();

            List<ServiceRecord> serviceRecords = protocol == Protocol.TCP ? getNmapServicesTCP() : getNmapServicesUDP();

            spinner.stop();

            System.out.printf(Colors.BLUE + "%-10s %-20s %-10s%n" + Colors.RESET, "Port", "Service", "Status");

            ExecutorService executor = protocol == Protocol.TCP ? Executors.newFixedThreadPool(serviceRecords.size()) : Executors.newFixedThreadPool(UDP_THREAD_POOL_SIZE);
            for (ServiceRecord serviceRecord : serviceRecords) {
                executor.submit(() -> {
                    boolean isOpen = protocol == Protocol.TCP ? connectTCP(target, serviceRecord.port(), timeout) 
                                                                : connectUDP(target, serviceRecord.port(), timeout);
                    scanResults.add(new PortScanResult(serviceRecord, isOpen));

                    System.out.printf("%-10d %-20s %-10s%n",
                        serviceRecord.port(),
                        serviceRecord.serviceName(),
                        isOpen ? Colors.GREEN + "Open" + Colors.RESET : Colors.RED + "Closed" + Colors.RESET);
                });
            }
            executor.shutdown();

            try {
                executor.awaitTermination(WAIT_DAYS, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            System.out.println(Colors.BG_RED + Colors.WHITE + "ERROR" 
                                + Colors.RESET + Colors.RED + " An issue occurred while reading in nmap-services data." + Colors.RESET);
            return new ArrayList<>();
        }

        return scanResults;
    }

    public static List<PortScanResult> deepPortScanTCP(String target, int timeout, Protocol protocol) {
        List<PortScanResult> scanResults = new ArrayList<>();

        System.out.printf(Colors.BLUE + "%-10s %-20s %-10s%n" + Colors.RESET, "Port", "Service", "Status");

        ExecutorService executor = protocol == Protocol.TCP ? Executors.newFixedThreadPool(MAX_PORTS) : Executors.newFixedThreadPool(UDP_THREAD_POOL_SIZE);

        for (int i = 0; i <= MAX_PORTS; i++) {
            final int port = i;
            executor.submit(() -> {
                boolean isOpen = protocol == Protocol.TCP ? connectTCP(target, port, timeout) : connectUDP(target, port, timeout);
                ServiceRecord serviceRecord = new ServiceRecord("Unknown", port, 0.0, "");
                scanResults.add(new PortScanResult(serviceRecord, isOpen));

                System.out.printf("%-10d %-20s %-10s%n",
                    port,
                    serviceRecord.serviceName(),
                    isOpen ? Colors.GREEN + "Open" + Colors.RESET : Colors.RED + "Closed" + Colors.RESET);
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(WAIT_DAYS, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return scanResults;
    }

    public static boolean connectTCP(String target, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(target, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean connectUDP(String target, int port, int timeout) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);

            byte[] sendData = "ping".getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(target), port);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                socket.receive(receivePacket);
                return true;
            } catch (SocketTimeoutException e) {
                return false;
            }
        } catch (IOException e) {
            System.out.println(Colors.BG_RED + Colors.WHITE + "ERROR" 
                                + Colors.RESET + Colors.RED + " An issue occurred while connecting to the target." + Colors.RESET);                                   
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