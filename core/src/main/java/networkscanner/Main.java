package networkscanner;

import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        // InetAddress targetAddress = InetAddress.getByName("scanme.nmap.org");
        // System.out.println("Target Hostname: " + targetAddress.getHostName());
        // System.out.println("Target IP Address: " + targetAddress.getHostAddress());

        // System.out.println("Is Reachable: " + targetAddress.isReachable(5000));

        NetworkScanner.thoroughPortScanTCP("scanme.nmap.org", 5000);
    }
}
