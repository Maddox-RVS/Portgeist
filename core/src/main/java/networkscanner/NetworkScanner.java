package networkscanner;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import util.loading.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import util.Colors;

public class NetworkScanner {
    private static final int MAX_PORTS = 65535;
    private static final int WAIT_DAYS = 1000;
    private static final int UDP_THREAD_POOL_SIZE = 100;

    public enum Protocol {
        TCP,
        UDP
    }

    public enum PortStatus {
        OPEN,
        CLOSED,
        FILTERED,
        FILTERED_OR_CLOSED;

        @Override
        public String toString() {
            return switch (this) {
                case OPEN -> "Open";
                case CLOSED -> "Closed";
                case FILTERED -> "Filtered";
                case FILTERED_OR_CLOSED -> "Filtered/Closed";
            };
        }

        public String toStringColored() {
            return switch (this) {
                case OPEN -> Colors.GREEN + "Open" + Colors.RESET;
                case CLOSED -> Colors.RED + "Closed" + Colors.RESET;
                case FILTERED -> Colors.BRIGHT_BLUE + "Filtered" + Colors.RESET;
                case FILTERED_OR_CLOSED -> Colors.BRIGHT_BLUE + "Filtered" + Colors.RESET + "/" + Colors.RED + "Closed" + Colors.RESET;
            };
        }
    }

    private record ServiceRecord(String serviceName, int port, double frequency, String comment) {}

    private record PortScanResult(ServiceRecord serviceRecord, PortStatus status, Protocol protocol) {}

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

            ExecutorService executor = protocol == Protocol.TCP ? Executors.newFixedThreadPool(serviceRecords.size()) : Executors.newFixedThreadPool(UDP_THREAD_POOL_SIZE);
            
            for (ServiceRecord serviceRecord : serviceRecords) {
                executor.submit(() -> {
                    PortStatus status = protocol == Protocol.TCP ? connectTCP(target, serviceRecord.port(), timeout)
                                                                : connectUDP(target, serviceRecord.port(), timeout);
                    scanResults.add(new PortScanResult(serviceRecord, status, protocol));

                    System.out.printf("%-10d %-20s %-5s %-10s%n",
                        serviceRecord.port(),
                        serviceRecord.serviceName(),
                        protocol == Protocol.TCP ? "TCP" : "UDP",
                        status.toStringColored());
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

            ExecutorService executor = protocol == Protocol.TCP ? Executors.newFixedThreadPool(serviceRecords.size()) : Executors.newFixedThreadPool(UDP_THREAD_POOL_SIZE);
            
            for (ServiceRecord serviceRecord : serviceRecords) {
                executor.submit(() -> {
                    PortStatus status = protocol == Protocol.TCP ? connectTCP(target, serviceRecord.port(), timeout)
                                                                : connectUDP(target, serviceRecord.port(), timeout);
                    scanResults.add(new PortScanResult(serviceRecord, status, protocol));

                    System.out.printf("%-10d %-20s %-5s %-10s%n",
                        serviceRecord.port(),
                        serviceRecord.serviceName(),
                        protocol == Protocol.TCP ? "TCP" : "UDP",
                        status.toStringColored());
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

    public static List<PortScanResult> deepPortScan(String target, int timeout, Protocol protocol) {
        List<PortScanResult> scanResults = new ArrayList<>();

        ExecutorService executor = protocol == Protocol.TCP ? Executors.newFixedThreadPool(MAX_PORTS) : Executors.newFixedThreadPool(UDP_THREAD_POOL_SIZE);

        for (int i = 0; i <= MAX_PORTS; i++) {
            final int port = i;
            executor.submit(() -> {
                PortStatus status = protocol == Protocol.TCP ? connectTCP(target, port, timeout) : connectUDP(target, port, timeout);
                ServiceRecord serviceRecord = new ServiceRecord("Unknown", port, 0.0, "");
                scanResults.add(new PortScanResult(serviceRecord, status, protocol));

                System.out.printf("%-10d %-20s %-5s %-10s%n",
                    port,
                    serviceRecord.serviceName(),
                    protocol == Protocol.TCP ? "TCP" : "UDP",
                    status.toStringColored());
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

    public static PortStatus connectTCP(String target, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(target, port), timeout);
            return PortStatus.OPEN;
        } catch (SocketTimeoutException e) {
            return PortStatus.FILTERED;
        } catch (ConnectException e) {
            return PortStatus.CLOSED;
        } catch (IOException e) {
            return PortStatus.FILTERED;
        }
    }

    public static PortStatus connectUDP(String target, int port, int timeout) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeout);

            byte[] sendData = "ping".getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(target), port);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                socket.receive(receivePacket);
                return PortStatus.OPEN;
            } catch (SocketTimeoutException e) {
                return PortStatus.FILTERED_OR_CLOSED;
            }
        } catch (PortUnreachableException e) {
            return PortStatus.CLOSED;
        } catch (IOException e) {
            System.out.println(Colors.BG_RED + Colors.WHITE + "ERROR" 
                                + Colors.RESET + Colors.RED + " An issue occurred while connecting to the target." + Colors.RESET);                                   
            return PortStatus.FILTERED;
        }
    }

    public static void printScanResultsHeader() {
        System.out.printf(Colors.BLUE + "%-10s %-20s %-10s %-10s%n" + Colors.RESET, "Port", "Service", "Protocol", "Status");
    }

    public static void printPortScanResults(List<PortScanResult> portScanResults) {
        printScanResultsHeader();

        for (PortScanResult result : portScanResults) {
            System.out.printf("%-10d %-20s %-10s %-10s%n",
              result.serviceRecord().port(),
              result.serviceRecord().serviceName(),
              result.protocol().toString(),
              result.status().toStringColored());
        }
    }
}